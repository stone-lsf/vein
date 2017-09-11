package com.sm.finance.charge.serializer.api;

import com.sm.finance.charge.common.ServiceLoader;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:32
 */
public abstract class AbstractSerializerFactory {

    public static Serializer create(String serializeType, DataStructure dataStructure) {
        AbstractSerializerFactory serializerFactory = ServiceLoader.findService(serializeType, AbstractSerializerFactory.class);

        return serializerFactory.doCreate(dataStructure);
    }

    protected abstract Serializer doCreate(DataStructure dataStructure);
}
