package com.sm.charge.raft.server.storage.logs.segment;

/**
 * @author shifeng.luo
 * @version created on 2017/11/5 下午12:09
 */
public interface FileNameRule {

    /**
     * 根据startIndex生成文件名称
     *
     * @param startIndex 文件存储的第一条log的index
     * @return 文件名
     */
    String generate(long startIndex);

    /**
     * 根据文件名反解析出文件存储的第一条log的index
     *
     * @param fileName 文件名
     * @return 如果是合法的文件名则返回对应的index，否则返回-1
     */
    long parse(String fileName);
}
