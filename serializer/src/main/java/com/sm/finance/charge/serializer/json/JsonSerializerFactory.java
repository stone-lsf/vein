package com.sm.finance.charge.serializer.json;

import com.sm.finance.charge.serializer.api.AbstractSerializerFactory;
import com.sm.finance.charge.serializer.api.DataStructure;
import com.sm.finance.charge.serializer.api.Serializer;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:39
 */
public class JsonSerializerFactory extends AbstractSerializerFactory {
    @Override
    protected Serializer doCreate(DataStructure dataStructure) {
        return new JsonSerializer(dataStructure);
    }
}
