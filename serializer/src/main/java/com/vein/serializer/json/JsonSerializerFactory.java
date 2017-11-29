package com.vein.serializer.json;

import com.vein.serializer.api.SerializerFactory;
import com.vein.serializer.api.SerializableTypes;
import com.vein.serializer.api.Serializer;

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
