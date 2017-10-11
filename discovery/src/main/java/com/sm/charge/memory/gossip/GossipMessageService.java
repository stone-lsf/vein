package com.sm.charge.memory.gossip;


import com.sm.charge.memory.DiscoveryNodeListener;
import com.sm.charge.memory.gossip.messages.AliveMessage;
import com.sm.charge.memory.gossip.messages.DeadMessage;
import com.sm.charge.memory.gossip.messages.SuspectMessage;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午11:21
 */
public interface GossipMessageService {

    /**
     * 声明某个节点是Alive状态
     *
     * @param message   {@link AliveMessage}存活消息
     * @param bootstrap 是否是启动时调用
     */
    void aliveNode(AliveMessage message, boolean bootstrap);

    /**
     * 声明某个节点是Suspect状态
     *
     * @param message {@link SuspectMessage}猜疑消息
     */
    void suspectNode(SuspectMessage message);

    /**
     * 声明某个节点是Dead状态
     *
     * @param message {@link DeadMessage}死亡消息
     */
    void deadNode(DeadMessage message);

    /**
     * 添加节点监听器
     *
     * @param listener 监听器{@link DiscoveryNodeListener}
     */
    void addListener(DiscoveryNodeListener listener);

    /**
     * 设置gossip消息通知器
     *
     * @param messageNotifier 消息通知器
     */
    void setMessageNotifier(GossipMessageNotifier messageNotifier);


    void handle(GossipRequest request);
}
