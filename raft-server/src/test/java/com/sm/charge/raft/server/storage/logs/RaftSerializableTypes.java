package com.sm.charge.raft.server.storage.logs;

import com.sm.charge.raft.client.CommandTypes;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.serializer.api.SerializableManager;
import com.sm.finance.charge.serializer.api.SerializableTypes;
import com.sm.finance.charge.serializer.api.UnknownDataStructureException;

/**
 * @author shifeng.luo
 * @version created on 2017/11/5 下午3:43
 */
public class RaftSerializableTypes extends SerializableManager {

    public RaftSerializableTypes() {
        CommandTypes.SerializableType[] types = CommandTypes.SerializableType.values();
        for (CommandTypes.SerializableType type : types) {
            register(type.code, type.clazz);
        }
    }

    public enum Structure {
        REQUEST((byte) 1, TestCommand.class);

        public byte type;
        public Class<?> clazz;

        Structure(byte type, Class<?> clazz) {
            this.type = type;
            this.clazz = clazz;
        }
    }
}
