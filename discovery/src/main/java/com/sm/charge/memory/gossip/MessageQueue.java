package com.sm.charge.memory.gossip;

import com.google.common.collect.Lists;

import com.sm.charge.memory.gossip.messages.GossipMessage;
import com.sm.charge.memory.gossip.messages.MemberWrapper;
import com.sm.charge.memory.gossip.messages.MessageWrapper;
import com.sm.finance.charge.common.base.LoggerSupport;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author shifeng.luo
 * @version created on 2017/9/19 下午1:02
 */
public class MessageQueue extends LoggerSupport {
    private final int maxTransmit = 6;

    private ConcurrentMap<String, MemberWrapper> nodeMessages = new ConcurrentHashMap<>();

    private BlockingQueue<MessageWrapper> messages;

    public MessageQueue(int capacity) {
        this.messages = new LinkedBlockingQueue<>(capacity);
    }

    public boolean enqueue(MemberWrapper wrapper) {
        try {
            MemberWrapper oldMessage = nodeMessages.replace(wrapper.message().getNodeId(), wrapper);
            if (oldMessage != null) {
                wrapper.invalidate(oldMessage);
            }
            messages.put(wrapper);
            return true;
        } catch (InterruptedException e) {
            logger.error("enqueue com.sm.charge.memory.gossip message:{} caught exception:{}", wrapper, e);
            return false;
        }
    }

    public List<GossipMessage> dequeue(int expectSize) {
        List<GossipMessage> result = Lists.newArrayListWithCapacity(expectSize);

        Iterator<MessageWrapper> iterator = messages.iterator();
        while (expectSize > 0 && iterator.hasNext()) {
            MessageWrapper wrapper = iterator.next();
            if (wrapper.invalid()) {
                iterator.remove();
                continue;
            }

            if (wrapper.transmits() > maxTransmit) {
                wrapper.onGossipFinish();
                iterator.remove();
                continue;
            }
            wrapper.increaseTransmits();
            result.add(wrapper.message());
            expectSize--;
        }

        return result;
    }
}
