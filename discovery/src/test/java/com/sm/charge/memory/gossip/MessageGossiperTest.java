package com.sm.charge.memory.gossip;

import com.sm.charge.memory.DiscoveryConfig;
import com.sm.charge.memory.NodeType;
import com.sm.charge.memory.Nodes;
import com.sm.charge.memory.gossip.messages.AliveMessage;
import com.sm.charge.memory.gossip.messages.GossipContent;
import com.sm.charge.memory.gossip.messages.MemberMessage;

import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.util.List;

import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author shifeng.luo
 * @version created on 2017/11/12 下午1:25
 */
public class MessageGossiperTest {

    private MessageGossiper gossiper;
    private DiscoveryConfig discoveryConfig;

    @Before
    public void init() {
        Nodes nodes = new Nodes("sldfkj");
        discoveryConfig = PowerMockito.mock(DiscoveryConfig.class);
        when(discoveryConfig.getGossipNodes()).thenReturn(2);
        when(discoveryConfig.getMaxGossipMessageCount()).thenReturn(2);
        when(discoveryConfig.getMaxGossipTimes()).thenReturn(2);
        when(discoveryConfig.getGossipQueueSize()).thenReturn(200);

        gossiper = new MessageGossiper(nodes, discoveryConfig);
    }

    @Test
    public void gossip() throws Exception {
    }

    @Test
    public void getMessage() throws Exception {

        MemberMessage message = new MemberMessage(new AliveMessage("test", null, 11, NodeType.DATA));
        MemberMessage message1 = new MemberMessage(new AliveMessage("test", null, 12, NodeType.DATA));
        MemberMessage message2 = new MemberMessage(new AliveMessage("test", null, 13, NodeType.DATA));

        gossiper.gossip(message);
        gossiper.gossip(message1);
        gossiper.gossip(message2);


//        List<GossipContent> contents = gossiper.getMessage(3);
//        System.out.println(contents);
    }

}