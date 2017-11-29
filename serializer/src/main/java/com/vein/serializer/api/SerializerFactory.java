package com.vein.serializer.api;

import com.vein.common.base.ServiceLoader;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:32
 */
public abstract class SerializerFactory {

    public static Serializer create(String serializeType, SerializableTypes types) {
        SerializerFactory serializerFactory = ServiceLoader.findService(serializeType, SerializerFactory.class);

        return serializerFactory.doCreate(types);
    }

    protected abstract Serializer doCreate(SerializableTypes dataStructure);
}
