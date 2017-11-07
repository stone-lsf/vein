package com.sm.finance.charge.serializer.protostuff;

import com.sm.finance.charge.serializer.api.Serializable;
import com.sm.finance.charge.serializer.api.SerializableTypes;
import com.sm.finance.charge.serializer.api.Serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:26
 */
public class ProtoStuffSerializer implements Serializer {
    private static final Logger logger = LoggerFactory.getLogger(ProtoStuffSerializer.class);

    private static Map<Class<?>, Schema<?>> schemaMap = new ConcurrentHashMap<>();

    private final SerializableTypes types;

    public ProtoStuffSerializer(SerializableTypes types) {
        this.types = types;
    }

    @Override
    public byte[] serialize(Serializable obj) {
        return toArrayByte(obj);

    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T deserialize(byte[] bytes) {
        return (T) mergeFrom(bytes, types.getSerializable(bytes[0]));
    }

    private <T extends Serializable> T mergeFrom(byte[] bytes, Class<T> type) {
        Schema<T> schema = getSchema(type);
        T instance = schema.newMessage();
        byte[] to = new byte[bytes.length - 1];
        System.arraycopy(bytes, 1, to, 0, bytes.length - 1);
        ProtobufIOUtil.mergeFrom(to, instance, schema);
        return instance;
    }

    @SuppressWarnings("unchecked")
    private <T extends Serializable> byte[] toArrayByte(Object obj) {
        T instance = (T) obj;
        Class<T> cType = (Class<T>) obj.getClass();
        Schema<T> schema = getSchema(cType);
        LinkedBuffer buffer = LinkedBufferPool.getLinkedBuffer();
        try {
            byte[] value = appendHead(ProtobufIOUtil.toByteArray(instance, schema, buffer), types.getType(cType));
            if (logger.isDebugEnabled()) {
                logger.debug("protobuf serialize " + obj.getClass() + " instance length:" + value.length);
            }
            return value;
        } finally {
            LinkedBufferPool.recycle(buffer);
        }
    }

    /**
     * 增加数组头，用于标识对象类型
     *
     * @param array 字节数组
     * @param type  对象类型
     * @return 最终序列化出来的数据
     */
    private byte[] appendHead(byte[] array, byte type) {
        byte[] value = new byte[array.length + 1];
        value[0] = type;
        System.arraycopy(array, 0, value, 1, array.length);
        return value;
    }

    @SuppressWarnings("unchecked")
    private <T> Schema<T> getSchema(Class<T> cType) {
        Schema<T> schema = (Schema<T>) schemaMap.get(cType);
        if (schema == null) {
            schema = RuntimeSchema.getSchema(cType);
            schemaMap.put(cType, schema);
        }
        return schema;
    }
}
