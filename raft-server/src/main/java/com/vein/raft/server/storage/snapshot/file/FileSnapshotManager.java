package com.vein.raft.server.storage.snapshot.file;

import com.google.common.collect.Lists;

import com.vein.raft.server.storage.FileNameRule;
import com.vein.raft.server.storage.snapshot.Snapshot;
import com.vein.raft.server.storage.snapshot.SnapshotManager;
import com.vein.common.AbstractService;
import com.vein.common.utils.FileUtil;
import com.vein.serializer.api.Serializer;
import com.vein.storage.api.exceptions.BadDataException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author shifeng.luo
 * @version created on 2017/10/1 下午5:18
 */
public class FileSnapshotManager extends AbstractService implements SnapshotManager, FileNameRule<Pair<Long, String>> {
    private static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmss";
    private static final char PART_SEPARATOR = '_';
    private static final String EXTENSION = ".snapshot";

    private final File directory;
    private final String name;
    private final Serializer serializer;
    private final ConcurrentNavigableMap<Long, Snapshot> snapshots = new ConcurrentSkipListMap<>();
    private volatile Snapshot currentSnapshot;

    public FileSnapshotManager(String directory, String snapshotName, Serializer serializer) {
        this.directory = new File(directory);
        this.serializer = serializer;
        try {
            FileUtil.mkDirIfAbsent(directory);
        } catch (NotDirectoryException e) {
            logger.error("path:{} is not a directory", directory);
            throw new IllegalStateException(e);
        }
        this.name = snapshotName;
    }

    @Override
    protected void doStart() throws Exception {
        loadSnapshots();
        Map.Entry<Long, Snapshot> entry = snapshots.lastEntry();
        if (entry == null) {
            return;
        }
        currentSnapshot = entry.getValue();
        try {
            currentSnapshot.check();
        } catch (BadDataException e) {
            currentSnapshot.delete();
            snapshots.remove(currentSnapshot.index());
            entry = snapshots.lastEntry();
            if (entry != null) {
                currentSnapshot = entry.getValue();
            }
        }
    }

    private void loadSnapshots() throws IOException {
        Collection<File> files = FileUtil.listAllFile(directory, File::isFile);

        for (File file : files) {
            Pair<Long, String> indexTime = parse(file.getName());
            if (indexTime != null) {
                Snapshot snapshot = new FileSnapshot(file, indexTime.getLeft(), indexTime.getRight(), serializer, this);
                snapshots.put(snapshot.index(), snapshot);
            }
        }
    }

    @Override
    public Snapshot create(long index) {
        String createTime = new DateTime().toString(TIMESTAMP_FORMAT);
        Pair<Long, String> indexTime = new ImmutablePair<>(index, createTime);

        String fileName = generate(indexTime);
        File file = new File(fileName);
        return new FileSnapshot(file, index, createTime, serializer, this);
    }

    @Override
    public void addSnapshot(Snapshot snapshot) {
        snapshots.put(snapshot.index(), snapshot);
        currentSnapshot = snapshot;
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

    @Override
    public String generate(Pair<Long, String> indexTime) {
        return String.format("%s_%d_%s.snapshot", name, indexTime.getLeft(), indexTime.getRight());
    }

    @Override
    public Pair<Long, String> parse(String fileName) {
        int lastSeparator = fileName.lastIndexOf(PART_SEPARATOR);
        if (!fileName.endsWith(EXTENSION) || !fileName.startsWith(name) || lastSeparator == -1) {
            logger.error("{} is not snapshot", fileName);
            return null;
        }

        String createTime = fileName.substring(lastSeparator + 1, fileName.lastIndexOf(EXTENSION));

        try {
            DateTime.parse(createTime, DateTimeFormat.forPattern(TIMESTAMP_FORMAT));
        } catch (Exception e) {
            logger.error("{} is not snapshot, don't contain legal timestamp", fileName);
            return null;
        }

        int firstSeparator = fileName.lastIndexOf(PART_SEPARATOR, lastSeparator - 1);
        if (firstSeparator == -1) {
            logger.error("{} is not snapshot, don't contain two '-'", fileName);
            return null;
        }

        String indexStr = fileName.substring(firstSeparator + 1, lastSeparator);
        try {
            long index = Long.valueOf(indexStr);
            return index <= 0 ? null : new ImmutablePair<>(index, createTime);
        } catch (Exception e) {
            logger.error("{} is not snapshot, don't contain legal index", fileName);
            return null;
        }
    }
}
