package com.sm.finance.charge.serializer.protostuff;

import com.sm.finance.charge.serializer.api.SerializerFactory;
import com.sm.finance.charge.serializer.api.SerializableTypes;
import com.sm.finance.charge.serializer.api.Serializer;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:33
 */
public class ProtoSerializerFactory extends SerializerFactory {


    @Override
    protected Serializer doCreate(SerializableTypes dataStructure) {
        return new ProtoStuffSerializer(dataStructure);
    }
}
