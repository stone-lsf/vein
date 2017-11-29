package com.vein.storage.api.index;

import com.vein.common.base.Closable;
import com.vein.common.base.Startable;

import java.io.IOException;

/**
 * @author shifeng.luo
 * @version created on 2017/9/25 下午11:01
 */
public interface IndexFileManager extends Startable, Closable {

    IndexFile create(long sequence) throws IOException;

    boolean delete(long sequence);

    IndexFile get(long sequence);

    IndexFile lookup(long sequence);

    IndexFile last();
}
