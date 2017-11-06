package com.sm.charge.raft.server.storage.snapshot.file;


import com.sm.charge.raft.server.storage.snapshot.Snapshot;
import com.sm.charge.raft.server.storage.snapshot.SnapshotManager;
import com.sm.charge.raft.server.storage.snapshot.SnapshotReader;
import com.sm.charge.raft.server.storage.snapshot.SnapshotWriter;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.common.utils.IoUtil;
import com.sm.finance.charge.serializer.api.Serializer;
import com.sm.finance.charge.storage.api.exceptions.BadDataException;
import com.sm.finance.charge.storage.api.exceptions.StorageException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.CRC32;

/**
 * @author shifeng.luo
 * @version created on 2017/10/1 下午5:05
 */
public class FileSnapshot extends LoggerSupport implements Snapshot {
    private final File file;
    private final long index;
    private final String createTime;
    private final Serializer serializer;
    private final SnapshotManager snapshotManager;
    private SnapshotWriter writer;

    FileSnapshot(File file, long index, String createTime, Serializer serializer, SnapshotManager snapshotManager) {
        this.index = index;
        this.createTime = createTime;
        this.file = file;
        this.serializer = serializer;
        this.snapshotManager = snapshotManager;
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public long index() {
        return index;
    }

    @Override
    public String createTime() {
        return createTime;
    }

    @Override
    public Snapshot complete() {
        writer.trimToValidSize();
        writer.flush();
        writer.close();

        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw")) {
            long expect = calculateChecksum(accessFile);
            accessFile.seek(0);
            accessFile.writeLong(expect);
        } catch (IOException e) {
            logger.error("check snapshot:{} file caught exception", file, e);
            throw new StorageException(e);
        }
        snapshotManager.addSnapshot(this);
        return this;
    }

    @Override
    public void check() {
        RandomAccessFile accessFile = null;
        try {
            accessFile = new RandomAccessFile(file, "r");
            long expect = calculateChecksum(accessFile);
            accessFile.seek(0);
            long real = accessFile.readLong();
            if (expect != real) {
                logger.error("snapshot:{} file has bad,expect checksum:{},real:{}", file, expect, real);
                throw new BadDataException();
            }
        } catch (IOException e) {
            logger.error("check snapshot:{} file caught exception", file, e);
            throw new StorageException(e);
        } finally {
            IoUtil.close(accessFile);
        }
    }

    private long calculateChecksum(RandomAccessFile accessFile) {
        CRC32 crc32 = new CRC32();
        try {
            if (accessFile.length() < 8) {
                logger.error("snapshot:{} file has bad", file);
                throw new BadDataException();
            }
            accessFile.seek(8);

            byte[] bytes = new byte[1024 * 64];
            int readCount;
            while ((readCount = accessFile.read(bytes)) > 0) {
                crc32.update(bytes, 0, readCount);
            }

            return crc32.getValue();
        } catch (IOException e) {
            logger.error("check snapshot:{} file caught exception", file, e);
            throw new StorageException(e);
        }
    }

    @Override
    public void delete() {
        if (writer != null) {
            writer.close();
        }

        if (file.exists()) {
            if (!file.delete()) {
                logger.error("delete snapshot:{} fail", file);
                throw new IllegalStateException("delete snapshot:" + file + " fail!");
            }
        }
    }

    @Override
    public SnapshotReader reader() {
        return new FileSnapshotReader(serializer, file);
    }

    @Override
    public SnapshotWriter writer() {
        synchronized (this) {
            if (writer == null) {
                writer = new FileSnapshotWriter(file, serializer);
            }
        }
        return writer;
    }
}
