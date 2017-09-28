package com.sm.finance.charge.storage.api.index;

import com.sm.finance.charge.common.Closable;
import com.sm.finance.charge.common.Startable;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:01
 */
public interface IndexManager extends Startable, Closable {

    IndexFile create(long sequence);

    IndexFile get(long sequence);

    IndexFile last();
}
