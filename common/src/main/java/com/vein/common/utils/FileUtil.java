package com.vein.common.utils;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.nio.ch.FileChannelImpl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.NotDirectoryException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author shifeng.luo
 * @version created on 2017/9/26 下午9:58
 */
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 列出指定目录下的所有文件
     *
     * @param directory 目录
     * @return 文件列表
     */
    public static Collection<File> listAllFile(String directory) throws IOException {
        File file = new File(directory);
        if (!file.exists()) {
            throw new FileNotFoundException("目录:" + directory + "不存在");
        }

        if (!file.isDirectory()) {
            throw new NotDirectoryException("文件:" + directory + "不是一个目录");
        }

        File[] files = file.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        return Lists.newArrayList(files);
    }

    /**
     * 列出指定目录下的所有满足过滤条件的文件
     *
     * @param directory 目录
     * @param filter    文件过滤器
     * @return 文件列表
     */
    public static Collection<File> listAllFile(String directory, FileFilter filter) throws IOException {
        File file = new File(directory);
        return listAllFile(file, filter);
    }


    /**
     * 列出指定目录下的所有满足过滤条件的文件
     *
     * @param directory 目录
     * @param filter    文件过滤器
     * @return 文件列表
     */
    public static Collection<File> listAllFile(File directory, FileFilter filter) throws IOException {
        if (!directory.exists()) {
            throw new FileNotFoundException("目录:" + directory + "不存在");
        }

        if (!directory.isDirectory()) {
            throw new NotDirectoryException("文件:" + directory + "不是一个目录");
        }

        File[] files = directory.listFiles(filter);
        if (files == null) {
            return Collections.emptyList();
        }

        return Lists.newArrayList(files);
    }


    /**
     * 返回FileChannel的可读取长度
     *
     * @author shifengluo
     * @date 2017/3/14
     */
    public static long readableSize(FileChannel channel) throws IOException {
        return channel.size() - channel.position();
    }

    /**
     * 如果目录不存在则创建目录
     * 如果父目录不存在，则递归创建
     *
     * @param path 目录路径
     * @return 目录
     */
    public static File mkDirIfAbsent(String path) throws NotDirectoryException {
        File directory = new File(path);
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                throw new NotDirectoryException(path + " must be a directory");
            }
        } else {
            boolean success = directory.mkdirs();
            if (!success) {
                throw new RuntimeException("创建目录:{}" + path + "失败");
            }
        }
        return directory;
    }


    /**
     * 删除指定文件内偏移处之后的数据
     *
     * @param offset 文件内偏移
     */
    public static void truncate(long offset, File file) throws IOException {
        RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
        FileChannel channel = accessFile.getChannel();

        try {
            channel.truncate(offset);
        } finally {
            IoUtil.close(accessFile);
        }
    }

    public static void close(MappedByteBuffer mapBuffer) throws ReflectiveOperationException {
        try {
            Method m = FileChannelImpl.class.getDeclaredMethod("unmap", MappedByteBuffer.class);
            m.setAccessible(true);
            m.invoke(FileChannelImpl.class, mapBuffer);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("close mapped byte buffer caught exception ", e);
            throw e;
        }
    }
}
