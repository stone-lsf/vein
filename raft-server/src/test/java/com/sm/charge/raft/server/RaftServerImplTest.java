package com.sm.charge.raft.server;

import com.sm.charge.raft.client.Command;
import com.sm.charge.raft.server.storage.SnapshotReader;
import com.sm.charge.raft.server.storage.SnapshotWriter;
import com.sm.finance.charge.common.base.Configure;
import com.sm.finance.charge.common.base.ConfigureLoader;
import com.sm.finance.charge.common.utils.ThreadUtil;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/11/4 下午2:06
 */
public class RaftServerImplTest {


    private RaftServer raft1;
    private RaftServer raft2;
    private RaftServer raft3;

    @Before
    public void setUp() throws Exception {

        LogStateMachine stateMachine = new LogStateMachine() {
            @Override
            public <T> CompletableFuture<T> apply(Command command) {
                System.out.println(command);
                CompletableFuture<T> future = new CompletableFuture<>();
                future.complete(null);
                return future;
            }

            @Override
            public Compactor compactor() {
                return null;
            }

            @Override
            public void take(SnapshotWriter writer) {

            }

            @Override
            public void install(SnapshotReader reader) {

            }
        };

        Configure configure = ConfigureLoader.loader("test/raft1.properties");
        RaftConfig config1 = new RaftConfig(configure);
        raft1 = new RaftServerImpl(config1, stateMachine);

        Configure configure2 = ConfigureLoader.loader("test/raft2.properties");
        RaftConfig config2 = new RaftConfig(configure2);
        raft2 = new RaftServerImpl(config2, stateMachine);

        Configure configure3 = ConfigureLoader.loader("test/raft3.properties");
        RaftConfig config3 = new RaftConfig(configure3);
        raft3 = new RaftServerImpl(config3, stateMachine);
    }

    @Test
    public void testJoin() throws Exception {
        new Thread(() -> {
            try {
                raft1.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                raft2.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                raft3.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();


        ThreadUtil.sleepUnInterrupted(2000000000);
    }
}