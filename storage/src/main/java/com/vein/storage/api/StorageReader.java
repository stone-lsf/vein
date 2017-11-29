package com.vein.storage.api;

import com.vein.common.base.Closable;
import com.vein.common.base.Startable;
import com.vein.storage.api.exceptions.BadDataException;
import com.vein.storage.api.exceptions.StorageException;

import java.io.IOException;
import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:41
 */
public interface StorageReader extends Startable, Closable {

    /**
     * 设置读取记录的起始序列号
     *
     * @param sequence 序列号
     */
    void readFrom(long sequence) throws IOException, BadDataException;

    /**
     * 读取一条数据
     *
     * @return 数据
     */
    <T> T read() throws BadDataException, StorageException;

    /**
     * 批量读取数据
     *
     * @param expectCount 期望读取的数量，实际可能并没有这么多数据
     * @return 数据列表
     */
    <T> List<T> read(int expectCount) throws BadDataException, StorageException;
}
