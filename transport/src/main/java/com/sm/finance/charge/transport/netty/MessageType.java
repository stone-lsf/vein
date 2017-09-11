package com.sm.finance.charge.transport.netty;

import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.serializer.api.DataStructure;
import com.sm.finance.charge.serializer.api.UnknownDataStructureException;
import com.sm.finance.charge.transport.api.Request;
import com.sm.finance.charge.transport.api.Response;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午5:40
 */
public class MessageType extends LogSupport implements DataStructure {
    @Override
    public byte getStructType(Class<?> clazz) {
        Structure[] structures = Structure.values();
        for (Structure structure : structures) {
            if (clazz == structure.clazz) {
                return structure.type;
            }
        }
        logger.error("未知的Class类型:{}", clazz);
        throw new UnknownDataStructureException("未知的Class类型:" + clazz);
    }

    @Override
    public Class<?> getStruct(byte type) {
        Structure[] structures = Structure.values();
        for (Structure structure : structures) {
            if (type == structure.type) {
                return structure.clazz;
            }
        }
        logger.error("未知的类型type:{}", type);
        throw new UnknownDataStructureException("未知的类型key:" + type);
    }

    public enum Structure {
        REQUEST((byte) 1, Request.class),
        RESPONSE((byte) 2, Response.class);


        public byte type;
        public Class<?> clazz;

        Structure(byte type, Class<?> clazz) {
            this.type = type;
            this.clazz = clazz;
        }
    }
}
