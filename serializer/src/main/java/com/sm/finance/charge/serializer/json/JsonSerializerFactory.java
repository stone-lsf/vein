package com.sm.finance.charge.serializer.json;

import com.sm.finance.charge.serializer.api.SerializerFactory;
import com.sm.finance.charge.serializer.api.SerializableTypes;
import com.sm.finance.charge.serializer.api.Serializer;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:39
 */
public class JsonSerializerFactory extends SerializerFactory {
    @Override
    protected Serializer doCreate(SerializableTypes dataStructure) {
        return new JsonSerializer(dataStructure);
    }
}
