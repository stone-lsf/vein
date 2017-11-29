package com.vein.raft.server.storage.logs.segment;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/11/5 上午11:50
 */
public class SegmentFile {

    private final File file;
    private final long baseIndex;


    public SegmentFile(File file, long baseIndex) {
        this.file = file;
        this.baseIndex = baseIndex;
    }


    public File file() {
        return file;
    }

    public long baseIndex() {
        return baseIndex;
    }
}
