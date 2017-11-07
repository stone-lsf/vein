package com.sm.finance.charge.serializer.api;

import com.sm.finance.charge.common.base.LoggerSupport;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author shifeng.luo
 * @version created on 2017/11/6 下午10:29
 */
public abstract class SerializableManager extends LoggerSupport implements SerializableTypes {

    private ConcurrentMap<Byte, Class<? extends Serializable>> typeClass = new ConcurrentHashMap<>();
    private ConcurrentMap<Class<? extends Serializable>, Byte> classType = new ConcurrentHashMap<>();

    protected void register(byte type, Class<? extends Serializable> clazz) {
        Class<?> exist = typeClass.putIfAbsent(type, clazz);
        if (exist != null && exist != clazz) {
            logger.error("type:{} has exist Serializable class:{}", type, exist);
            throw new IllegalStateException("type:" + type + " has exist Serializable class:" + exist);
        }

        classType.put(clazz, type);
    }

    /**
     * 根据对象类型获取对应的编码
     *
     * @param clazz 对象类型
     * @return 编码
     */
    public byte getType(Class<? extends Serializable> clazz) {
        Byte type = classType.get(clazz);
        if (type == null) {
            logger.error("未知的Class类型:{}", clazz);
            throw new UnknownDataStructureException("未知的Class类型:" + clazz);
        }
        return type;
    }

    /**
     * 根据编码获取对应的对象类型
     *
     * @param type 编码
     * @return 对象类型
     */
    public Class<? extends Serializable> getSerializable(byte type) {
        Class<? extends Serializable> clazz = typeClass.get(type);
        if (clazz == null) {
            logger.error("未知的类型type:{}", type);
            throw new UnknownDataStructureException("未知的类型key:" + type);
        }
        return clazz;
    }
}
