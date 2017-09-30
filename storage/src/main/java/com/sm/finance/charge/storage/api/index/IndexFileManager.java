package com.sm.finance.charge.storage.api.index;

import com.sm.finance.charge.common.Closable;
import com.sm.finance.charge.common.Startable;
import com.sm.finance.charge.common.exceptions.BadDiskException;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:01
 */
public interface IndexFileManager extends Startable, Closable {

    IndexFile create(long sequence) throws BadDiskException;

    IndexFile get(long sequence);

    IndexFile last();
}
