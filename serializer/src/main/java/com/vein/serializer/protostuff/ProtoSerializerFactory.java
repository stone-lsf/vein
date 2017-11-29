package com.vein.serializer.protostuff;

import com.vein.serializer.api.SerializerFactory;
import com.vein.serializer.api.SerializableTypes;
import com.vein.serializer.api.Serializer;

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
