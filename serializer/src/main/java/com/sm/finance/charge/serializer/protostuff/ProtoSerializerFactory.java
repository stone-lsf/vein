package com.sm.finance.charge.serializer.protostuff;

import com.sm.finance.charge.serializer.api.AbstractSerializerFactory;
import com.sm.finance.charge.serializer.api.DataStructure;
import com.sm.finance.charge.serializer.api.Serializer;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:33
 */
public class ProtoSerializerFactory extends AbstractSerializerFactory {


    @Override
    protected Serializer doCreate(DataStructure dataStructure) {
        return new ProtoStuffSerializer(dataStructure);
    }
}
