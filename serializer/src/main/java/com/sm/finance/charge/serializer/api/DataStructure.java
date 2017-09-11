package com.sm.finance.charge.serializer.api;

/**
 * 需要序列化和反序列号的数据结构类型接口
 *
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:21
 */
public interface DataStructure {
    /**
     * 根据对象类型获取对应的编码
     *
     * @param clazz 对象类型
     * @return 编码
     */
    byte getStructType(Class<?> clazz);

    /**
     * 根据编码获取对应的对象类型
     *
     * @param type 编码
     * @return 对象类型
     */
    Class<?> getStruct(byte type);
}
