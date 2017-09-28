package com.sm.finance.charge.storage.sequential.index;

import com.sm.finance.charge.storage.api.index.IndexAppender;
import com.sm.finance.charge.storage.api.index.IndexFile;
import com.sm.finance.charge.storage.api.index.OffsetIndex;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/9/26 上午12:08
 */
public class SequentialIndexAppender implements IndexAppender {

    private final IndexFile indexFile;

    public SequentialIndexAppender(IndexFile indexFile) {
        this.indexFile = indexFile;
    }

    @Override
    public IndexFile getIndexFile() {
        return indexFile;
    }

    @Override
    public CompletableFuture<Boolean> write(OffsetIndex index) {
        return null;
    }

    @Override
    public IndexAppender flush() {
        return null;
    }
}
