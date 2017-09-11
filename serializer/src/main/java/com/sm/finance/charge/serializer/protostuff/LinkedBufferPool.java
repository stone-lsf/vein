package com.sm.finance.charge.serializer.protostuff;

import java.util.concurrent.ConcurrentLinkedQueue;

import io.protostuff.LinkedBuffer;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:30
 */
public class LinkedBufferPool {
    private static ConcurrentLinkedQueue<LinkedBuffer> linkedQueue = new ConcurrentLinkedQueue<LinkedBuffer>();

    private static final int CORE_LENGTH = 50;

    public static LinkedBuffer getLinkedBuffer() {
        LinkedBuffer linkedBuffer = linkedQueue.poll();
        if (linkedBuffer == null) {
            linkedBuffer = LinkedBuffer.allocate();
        }
        return linkedBuffer;
    }

    public static void recycle(LinkedBuffer linkedBuffer) {
        linkedBuffer.clear();
        if (linkedQueue.size() < CORE_LENGTH) {
            linkedQueue.offer(linkedBuffer);
        }
    }
}
