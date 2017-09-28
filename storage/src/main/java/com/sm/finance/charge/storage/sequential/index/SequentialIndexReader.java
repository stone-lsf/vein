package com.sm.finance.charge.storage.sequential.index;

import com.sm.finance.charge.storage.api.exceptions.BadDataException;
import com.sm.finance.charge.storage.api.index.IndexFile;
import com.sm.finance.charge.storage.api.index.IndexReader;
import com.sm.finance.charge.storage.api.index.OffsetIndex;

/**
 * @author shifeng.luo
 * @version created on 2017/9/26 上午12:08
 */
public class SequentialIndexReader implements IndexReader {

    private final IndexFile indexFile;

    public SequentialIndexReader(IndexFile indexFile) {
        this.indexFile = indexFile;
    }

    @Override
    public IndexFile getIndexFile() {
        return indexFile;
    }

    @Override
    public OffsetIndex readIndex() throws BadDataException {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
