package com.sm.finance.charge.storage.sequential.segment;

import com.sm.finance.charge.storage.api.segment.Entry;
import com.sm.finance.charge.storage.api.segment.Header;

/**
 * @author shifeng.luo
 * @version created on 2017/9/26 上午12:05
 */
public class SequentialEntry implements Entry {

    private Header header;

    private byte[] payload;

    @Override
    public Header head() {
        return header;
    }

    @Override
    public byte[] payload() {
        return payload;
    }
}
