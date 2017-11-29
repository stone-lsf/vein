package com.vein.raft.client;

import com.vein.serializer.api.Serializable;
import com.vein.serializer.api.AbstractSerializableManager;

/**
 * @author shifeng.luo
 * @version created on 2017/11/5 下午6:11
 */
public class CommandTypes extends AbstractSerializableManager {

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
