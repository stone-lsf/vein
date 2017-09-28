package com.sm.finance.charge.storage.sequential.index;

import com.sm.finance.charge.common.FileUtil;
import com.sm.finance.charge.common.LogSupport;
import com.sm.finance.charge.common.exceptions.BadDiskException;
import com.sm.finance.charge.storage.api.exceptions.BadDataException;
import com.sm.finance.charge.storage.api.exceptions.StorageException;
import com.sm.finance.charge.storage.api.index.IndexFile;
import com.sm.finance.charge.storage.api.index.OffsetIndex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:54
 */
public class SequentialIndexFile extends LogSupport implements IndexFile {
    private final File file;
    private final long firstSequence;
    private final ConcurrentNavigableMap<Long, OffsetIndex> sequenceIndexMap = new ConcurrentSkipListMap<>();
    private final ConcurrentNavigableMap<Long, OffsetIndex> offsetIndexMap = new ConcurrentSkipListMap<>();

    SequentialIndexFile(File file, long firstSequence) {
        this.file = file;
        this.firstSequence = firstSequence;
    }

    @Override
    public long firstSequence() {
        return firstSequence;
    }

    @Override
    public OffsetIndex lastIndex() {
        return null;
    }

    @Override
    public OffsetIndex floorOffset(long entryOffset) {
        return null;
    }

    @Override
    public OffsetIndex floorSequence(long sequence) {
        return null;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public long check() throws IOException {
        long offset = 0;
        OffsetIndex index;
        try {
            while ((index = readIndex()) != null) {
                index.check();
                offset += index.size();
                sequenceIndexMap.put(index.sequence(), index);
                offsetIndexMap.put(index.offset(), index);
            }
        } catch (BadDataException e) {
            logger.warn("segment:{} has bad data", file.getName());
        }
        return offset;
    }

    @Override
    public IndexFile truncate(long offset) {
        try {
            FileUtil.truncate(offset, file);
        } catch (FileNotFoundException e) {
            logger.error("truncate index file caught exception", e);
            throw new StorageException(e);
        } catch (BadDiskException e) {
            logger.error("truncate index file caught bad disk exception", e);
            System.exit(-1);
        }
        return this;
    }

    @Override
    public IndexFile truncate(OffsetIndex index) {
        return null;
    }

    @Override
    public OffsetIndex readIndex() {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> write(OffsetIndex index) {
        return null;
    }

    @Override
    public IndexFile flush() {
        return null;
    }


    @Override
    public void close() throws Exception {

    }
}
