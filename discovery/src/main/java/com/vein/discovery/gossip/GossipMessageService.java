package com.vein.discovery.gossip;


import com.vein.discovery.NodeListener;
import com.vein.discovery.gossip.messages.AliveMessage;
import com.vein.discovery.gossip.messages.DeadMessage;
import com.vein.discovery.gossip.messages.SuspectMessage;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午11:21
 */
public interface GossipMessageService {

    /**
     * 声明某个节点是Alive状态
     *
     * @param message   {@link AliveMessage}存活消息
     * @param notifier  gossip完成通知
     * @param bootstrap 是否是启动时调用
     */
    void aliveNode(AliveMessage message, GossipFinishNotifier notifier, boolean bootstrap);

    /**
     * 声明某个节点是Suspect状态
     *
     * @param message  {@link SuspectMessage}猜疑消息
     * @param notifier gossip完成通知
     */
    void suspectNode(SuspectMessage message, GossipFinishNotifier notifier);

    /**
     * 声明某个节点是Dead状态
     *
     * @param message  {@link DeadMessage}死亡消息
     * @param notifier gossip完成通知
     */
    void deadNode(DeadMessage message, GossipFinishNotifier notifier);

    /**
     * 添加节点监听器
     *
     * @param listener 监听器{@link NodeListener}
     */
    void addListener(NodeListener listener);

    /**
     * 设置gossip消息通知器
     *
     * @param messageNotifier 消息通知器
     */
    void setMessageNotifier(GossipMessageNotifier messageNotifier);


    void handle(GossipRequest request);
}
