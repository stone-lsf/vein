package com.sm.charge.raft.server.storage.snapshot.file;

import com.google.common.collect.Lists;

import com.sm.charge.raft.server.storage.snapshot.Snapshot;
import com.sm.charge.raft.server.storage.snapshot.SnapshotDescriptor;
import com.sm.charge.raft.server.storage.snapshot.SnapshotManager;
import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.utils.FileUtil;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author shifeng.luo
 * @version created on 2017/10/1 下午5:18
 */
public class FileSnapshotManager extends AbstractService implements SnapshotManager {
    private static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmss";
    private static final char PART_SEPARATOR = '-';
    private static final String EXTENSION = ".snapshot";

    private final File directory;
    private final String name;
    private final ConcurrentNavigableMap<Long, Snapshot> snapshots = new ConcurrentSkipListMap<>();
    private Snapshot currentSnapshot;

    public FileSnapshotManager(String directory, String snapshotName) {
        this.directory = new File(directory);
        //todo check directory
        this.name = snapshotName;
    }

    @Override
    protected void doStart() throws Exception {
        loadSnapshots();
        if (!snapshots.isEmpty()) {
            currentSnapshot = snapshots.lastEntry().getValue();
        }
    }

    private void loadSnapshots() throws IOException {
        Collection<File> files = FileUtil.listAllFile(directory, File::isFile);

        for (File file : files) {
            SnapshotDescriptor descriptor = parse(file);
            if (descriptor != null) {
                Snapshot snapshot = new FileSnapshot(descriptor, file);
                snapshots.put(snapshot.index(), snapshot);
            }
        }
    }


    @Override
    public SnapshotDescriptor parse(File file) {
        if (file == null) {
            logger.error("file is null");
            return null;
        }

        if (!file.isFile()) {
            logger.error("{} is not file", file);
            return null;
        }

        String fileName = file.getName();
        int lastSeparator = fileName.lastIndexOf(PART_SEPARATOR);
        if (!fileName.endsWith(EXTENSION) || !fileName.startsWith(name) || lastSeparator == -1) {
            logger.error("{} is not snapshot", file);
            return null;
        }

        String timestamp = fileName.substring(lastSeparator + 1, fileName.lastIndexOf(EXTENSION));

        try {
            DateTime.parse(timestamp, DateTimeFormat.forPattern(TIMESTAMP_FORMAT)).getMillis();
        } catch (Exception e) {
            logger.error("{} is not snapshot, don't contain legal timestamp", file);
            return null;
        }

        int firstSeparator = fileName.lastIndexOf(PART_SEPARATOR, lastSeparator - 1);
        if (firstSeparator == -1) {
            logger.error("{} is not snapshot, don't contain two '-'", file);
            return null;
        }

        String indexStr = fileName.substring(firstSeparator + 1, lastSeparator);
        try {
            long index = Long.valueOf(indexStr);
            return new SnapshotDescriptor(index, Long.valueOf(timestamp));
        } catch (Exception e) {
            logger.error("{} is not snapshot, don't contain legal index", file);
            return null;
        }
    }

    @Override
    public Snapshot create(long index, long timestamp) {
        String fileName = String.format("%s-%d-%s.snapshot", name, index, new DateTime(timestamp).toString(TIMESTAMP_FORMAT));
        File file = new File(fileName);
        SnapshotDescriptor descriptor = new SnapshotDescriptor(index, timestamp);
        return new FileSnapshot(descriptor, file);
    }


    @Override
    public Snapshot currentSnapshot() {
        return currentSnapshot;
    }

    @Override
    public Collection<Snapshot> snapshots() {
        return Lists.newArrayList(snapshots.values());
    }

    @Override
    public Snapshot snapshot(long index) {
        return snapshots.get(index);
    }



    @Override
    protected void doClose() {

    }
}
