package com.sm.finance.charge.storage.sequential.index;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.FileUtil;
import com.sm.finance.charge.storage.api.index.IndexFile;
import com.sm.finance.charge.storage.api.index.IndexManager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author shifeng.luo
 * @version created on 2017/9/26 上午12:08
 */
public class SequentialIndexFileManager extends AbstractService implements IndexManager {
    private static final String EXTENSION = "index";
    private static final char EXTENSION_SEPARATOR = '.';

    private final File directory;
    private ConcurrentNavigableMap<Long, IndexFile> indexMap = new ConcurrentSkipListMap<>();

    public SequentialIndexFileManager(File directory) {
        this.directory = directory;
    }

    @Override
    public IndexFile create(long sequence) {
        checkStarted();
        File file = buildIndexFile(sequence);

        IndexFile segment = new SequentialIndexFile(file, sequence);
        indexMap.put(sequence, segment);
        return segment;
    }

    @Override
    public IndexFile get(long sequence) {
        return indexMap.get(sequence);
    }

    private File buildIndexFile(long sequence) {
        String name = sequence + EXTENSION_SEPARATOR + EXTENSION;
        return new File(directory, name);
    }

    @Override
    public IndexFile last() {
        checkStarted();
        Map.Entry<Long, IndexFile> entry = indexMap.lastEntry();
        return entry == null ? null : entry.getValue();
    }

    @Override
    protected void doStart() throws Exception {
        loadSegments();

        Map.Entry<Long, IndexFile> entry = indexMap.lastEntry();
        if (entry == null) {
            return;
        }

        IndexFile indexFile = entry.getValue();
        long validOffset = indexFile.check();
        indexFile.truncate(validOffset);
    }

    private void loadSegments() throws IOException {
        Collection<File> files = FileUtil.listAllFile(directory, File::isFile);

        for (File file : files) {
            long firstSequence = parseFirstSequence(file);
            if (firstSequence <= 0) {
                continue;
            }
            IndexFile indexFile = new SequentialIndexFile(file, firstSequence);
            indexMap.put(firstSequence, indexFile);
        }
    }


    public long parseFirstSequence(File file) {
        String name = file.getName();
        if (!name.endsWith(EXTENSION_SEPARATOR + EXTENSION)) {
            return -1;
        }

        int end = name.lastIndexOf(EXTENSION_SEPARATOR);
        String sequence = name.substring(0, end);
        long value;
        try {
            value = Long.parseLong(sequence);
        } catch (Throwable e) {
            logger.warn("file:{} is not a segment", name);
            return -1;
        }

        return value;
    }

    @Override
    protected void doClose() {

    }
}
