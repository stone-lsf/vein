package com.sm.finance.charge.storage.sequential;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.storage.api.StorageReader;

import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午10:41
 */
public class SequentialStorageReader extends AbstractService implements StorageReader {
    @Override
    public void readFrom(long sequence) {

    }

    @Override
    public Object read() {
        return null;
    }

    @Override
    public List read(int expectCount) {
        return null;
    }

    @Override
    protected void doStart() throws Exception {

    }

    @Override
    protected void doClose() {

    }
}
