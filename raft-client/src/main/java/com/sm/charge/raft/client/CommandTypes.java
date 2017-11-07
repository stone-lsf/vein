package com.sm.charge.raft.client;

import com.sm.finance.charge.serializer.api.Serializable;
import com.sm.finance.charge.serializer.api.SerializableManager;

/**
 * @author shifeng.luo
 * @version created on 2017/11/5 下午6:11
 */
public class CommandTypes extends SerializableManager {

    public CommandTypes() {
        SerializableType[] types = SerializableType.values();
        for (SerializableType type : types) {
            register(type.code, type.clazz);
        }
    }

    public enum SerializableType {
        configure((byte) 1, Configure.class);


        public byte code;
        public Class<? extends Serializable> clazz;

        SerializableType(byte code, Class<? extends Serializable> clazz) {
            this.code = code;
            this.clazz = clazz;
        }
    }
}
