package com.sm.finance.charge.serializer.api;

/**
 * 序列化和反序列化接口
 *
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:16
 */
public interface Serializer {
    /**
     * 序列化
     *
     * @param obj 待序列化对象
     * @return 字节数组
     */
    byte[] serialize(Serializable obj);

    /**
     * 反序列化
     *
     * @param bytes 字节数组
     * @return 反序列化后的对象
     */
    <T extends Serializable> T deserialize(byte[] bytes);
}
