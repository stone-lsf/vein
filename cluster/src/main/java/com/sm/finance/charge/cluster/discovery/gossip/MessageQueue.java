package com.sm.finance.charge.cluster.discovery.gossip;

import com.google.common.collect.Lists;

import com.sm.finance.charge.cluster.discovery.gossip.messages.GossipMessage;
import com.sm.finance.charge.common.LogSupport;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author shifeng.luo
 * @version created on 2017/9/19 下午1:02
 */
public class MessageQueue extends LogSupport {

    private BlockingQueue<GossipMessage> messages;

    public MessageQueue(int capacity) {
        this.messages = new LinkedBlockingQueue<>(capacity);
    }

    public boolean enqueue(GossipMessage message) {
        try {
            messages.put(message);
            return true;
        } catch (InterruptedException e) {
            logger.error("enqueue gossip message:{} caught exception:{}", message, e);
            return false;
        }
    }

    public List<GossipMessage> dequeue(int expectSize) {
        List<GossipMessage> result = Lists.newArrayListWithCapacity(expectSize);
        GossipMessage message;

        while ((message = messages.poll()) != null) {
            result.add(message);
            expectSize--;
            if (expectSize == 0) {
                break;
            }
        }

        return result;
    }
}
