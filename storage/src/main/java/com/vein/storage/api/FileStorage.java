package com.vein.storage.api;

import com.vein.common.base.Startable;
import com.vein.storage.api.exceptions.BadDataException;
import com.vein.storage.api.exceptions.StorageException;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:19
 */
public interface FileStorage extends Startable {

    /**
     * 返回存储目录
     *
     * @return 目录
     */
    File directory();

    /**
     * 最大segment大小(字节)
     *
     * @return 字节数
     */
    int maxSegmentSize();

    /**
     * 每条记录的最大大小
     *
     * @return 字节数
     */
    int maxMessageSize();

    /**
     * appender
     *
     * @return appender
     */
    StorageAppender appender();

    /**
     * rolling reader
     *
     * @return rolling reader
     */
    StorageReader reader(long startSequence) throws StorageException, BadDataException;
}

