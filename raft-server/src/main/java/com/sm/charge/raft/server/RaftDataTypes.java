package com.sm.charge.raft.server;

import com.sm.charge.raft.client.ConfigureCommand;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.serializer.api.DataStructure;
import com.sm.finance.charge.serializer.api.UnknownDataStructureException;

/**
 * @author shifeng.luo
 * @version created on 2017/11/5 下午6:11
 */
public class RaftDataTypes extends LoggerSupport implements DataStructure {

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
        configure((byte) 1, ConfigureCommand.class);


        public byte type;
        public Class<?> clazz;

        Structure(byte type, Class<?> clazz) {
            this.type = type;
            this.clazz = clazz;
        }
    }
}
