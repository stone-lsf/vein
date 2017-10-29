package com.sm.charge.cluster.group;

import com.sm.charge.cluster.Store;
import com.sm.charge.cluster.messages.JoinRequest;
import com.sm.charge.cluster.messages.JoinResponse;
import com.sm.charge.cluster.messages.PullRequest;
import com.sm.charge.cluster.messages.PullResponse;
import com.sm.charge.cluster.messages.PullState;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.Connection;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author shifeng.luo
 * @version created on 2017/10/29 下午1:50
 */
public class Selector extends LoggerSupport {
    private final Server self;
    private final ServerGroup group;
    private final int electTimeout;
    private Store store;
    private final ConcurrentMap<Address, PullState> pullStates = new ConcurrentHashMap<>();
    private final AtomicReference<ElectionContext> contextReference = new AtomicReference<>();
    private final ConcurrentMap<Address, CompletableFuture<Void>> pendJoinFutures = new ConcurrentHashMap<>();


    public Selector(Server self, ServerGroup group, int electTimeout) {
        this.self = self;
        this.group = group;
        this.electTimeout = electTimeout;
    }


    public void joinGroup() {
        Server master = null;
        while (master == null) {
            master = findMaster();
        }

        if (master == self) {
            waitToBeLeader(new ElectionCallback() {
                @Override
                public void onElectAsLeader() {
                    logger.info("server:{} select as leader", self.getAddress());
                }

                @Override
                public void onFailure(Throwable error) {
                    joinGroup();
                }
            }, group.getQuorum());
        } else {
            joinElectedLeader(master).whenComplete((success, error) -> {
                if (error != null || !success) {
                    joinGroup();
                    return;
                }

                //TODO pull from leader
            });
        }
    }


    public Server findMaster() {
        List<PullState> states = pullState();

        List<PullState> masters = new ArrayList<>();
        for (PullState state : states) {
            if (state.isLeader()) {
                masters.add(state);
            }
        }

        PullState master;
        if (masters.isEmpty()) {
            if (states.size() >= group.getQuorum()) {
                master = selectMaster(states);
            } else {
                logger.error("not enough nodes:{} to select", states);
                return null;
            }
        } else {
            master = selectMaster(masters);
        }

        if (master != null) {
            return group.get(master.getAddress());
        }
        return null;
    }

    private PullState selectMaster(List<PullState> states) {
        if (CollectionUtils.isEmpty(states)) {
            return null;
        }

        states.sort(new StateComparator());
        return states.get(0);
    }


    private List<PullState> pullState() {
        List<Server> servers = group.getServers();
        CountDownLatch latch = new CountDownLatch(servers.size() - 1);
        PullRequest request = new PullRequest(buildPullState());

        Map<Address, PullState> responseMap = new HashMap<>();
        for (Server server : servers) {
            if (server == self) {
                continue;
            }

            Connection connection = server.getConnection();
            if (connection == null) {
                latch.countDown();
                continue;
            }

            connection.<PullResponse>request(request).whenComplete((response, error) -> {
                if (error != null) {
                    logger.error("send pull request to {} caught exception", server.getAddress(), error);
                } else {
                    merge(response, responseMap, server);
                }
                latch.countDown();
            });
        }

        return new ArrayList<>(responseMap.values());
    }

    private synchronized void merge(PullResponse response, Map<Address, PullState> responseMap, Server from) {
        List<PullState> states = response.getStates();
        for (PullState state : states) {
            Address address = state.getAddress();
            if (address.equals(from.getAddress())) {
                responseMap.put(address, state);
                continue;
            }

            PullState pullState = responseMap.get(address);
            if (pullState == null) {
                responseMap.put(address, state);
            } else if (pullState.getId() < state.getId()) {
                responseMap.put(address, state);
            }
        }
    }


    public CompletableFuture<PullResponse> handle(PullRequest request) {
        PullState state = request.getState();
        pullStates.put(state.getAddress(), state);

        List<PullState> states = new ArrayList<>(pullStates.values());
        states.add(buildPullState());

        PullResponse response = new PullResponse(states);
        return CompletableFuture.completedFuture(response);
    }


    private PullState buildPullState() {
        PullState state = new PullState(self.getAddress(), store.lastIndex(), group.getVersion());

        Server leader = group.getLeader();
        if (leader == self) {
            state.setLeader(true);
        }

        return state;
    }


    private void waitToBeLeader(ElectionCallback callback, int requiredJoins) {
        CountDownLatch done = new CountDownLatch(1);
        ElectionContext context = new ElectionContext(callback, requiredJoins) {
            @Override
            void onClose() {

            }
        };

        if (!contextReference.compareAndSet(null, context)) {
            failElect(context, new IllegalStateException("double waiting for election"));
            return;
        }

        try {
            if (done.await(electTimeout, TimeUnit.MILLISECONDS)) {
                return;
            }
            failElect(context, new TimeoutException("waiting to be elected timeout:" + electTimeout));
        } catch (Throwable error) {
            logger.error("while waiting to be elected,unexpected error", error);
            failElect(context, error);
        }
    }

    private void failElect(ElectionContext context, Throwable error) {
        logger.error("wait to be elected as leader caught error", error);
        context.onFailure(error);
    }

    private CompletableFuture<Boolean> joinElectedLeader(Server server) {
        JoinRequest request = new JoinRequest(self.getAddress(), group.getVersion(), store.lastIndex());
        Connection connection = server.getConnection();
        if (connection == null) {
            return CompletableFuture.completedFuture(false);
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        connection.<JoinResponse>request(request).whenComplete((response, error) -> {
            if (error != null) {
                logger.error("join to elected leader:{} caught exception", server.getAddress(), error);
                future.complete(false);
            } else {
                if (response.getStatus() == JoinResponse.SUCCESS) {
                    group.setVersion(response.getVersion());
                    future.complete(true);
                } else {
                    future.complete(false);
                }
            }
        });

        return future;
    }

    public CompletableFuture<JoinResponse> handle(JoinRequest request) {
        CompletableFuture<JoinResponse> future = new CompletableFuture<>();

        CompletableFuture<Void> electedFuture = new CompletableFuture<>();
        pendJoinFutures.put(request.getAddress(), electedFuture);
        electedFuture.whenComplete((result, error) -> {
            if (error != null) {
                future.complete(new JoinResponse(JoinResponse.INNER_ERROR));
            } else {
                future.complete(new JoinResponse(JoinResponse.SUCCESS, group.getVersion()));
            }
        });

        ElectionContext context = contextReference.get();
        if (context == null) {
            if (group.getLeader() != self) {
                pendJoinFutures.remove(request.getAddress());
                future.complete(new JoinResponse(JoinResponse.NOT_LEADER));
            } else {
                future.complete(processJoin(request));
            }
        } else {
            boolean elected = checkPendingJoins(context);
            if (elected) {
                CompletableFuture<Void> completableFuture = pendJoinFutures.remove(request.getAddress());
                if (completableFuture != null) {
                    future.complete(new JoinResponse(JoinResponse.SUCCESS, group.getVersion()));
                }
            }
        }

        return future;
    }

    private JoinResponse processJoin(JoinRequest request) {
        if (group.getLeader() != self) {
            return new JoinResponse(JoinResponse.NOT_LEADER);
        }

        Server server = group.get(request.getAddress());
        if (server == null) {
            //TODO add server to group
        }

        return new JoinResponse(JoinResponse.SUCCESS, group.getVersion());
    }

    private boolean checkPendingJoins(ElectionContext context) {
        int size = pendJoinFutures.size();
        if (size < context.getRequiredJoins()) {
            return false;
        }

        if (context.getEnoughJoins().getAndSet(true)) {
            logger.info("elected as leader has submit ,ignore...");
            return true;
        }

        context.onElectAsLeader();
        return true;
    }


    private static class StateComparator implements Comparator<PullState> {

        @Override
        public int compare(PullState state1, PullState state2) {
            int compare = Long.compare(state1.getVersion(), state2.getVersion());
            if (compare != 0) {
                return compare;
            }

            return Long.compare(state1.getLastIndex(), state2.getLastIndex());
        }
    }


    abstract class ElectionContext implements ElectionCallback {

        private final ElectionCallback callback;
        private final int requiredJoins;
        private final AtomicBoolean enoughJoins = new AtomicBoolean(false);
        final AtomicBoolean closed = new AtomicBoolean();

        ElectionContext(ElectionCallback callback, int requiredJoins) {
            this.callback = callback;
            this.requiredJoins = requiredJoins;
        }

        abstract void onClose();

        @Override
        public void onElectAsLeader() {
            Set<Address> addresses = new HashSet<>(pendJoinFutures.keySet());
            for (Address address : addresses) {
                CompletableFuture<Void> future = pendJoinFutures.remove(address);
                future.complete(null);
            }
            if (closed.compareAndSet(false, true)) {
                try {
                    onClose();
                } finally {
                    callback.onElectAsLeader();
                }
            }
        }

        @Override
        public void onFailure(Throwable error) {
            Set<Address> addresses = new HashSet<>(pendJoinFutures.keySet());
            for (Address address : addresses) {
                CompletableFuture<Void> future = pendJoinFutures.remove(address);
                future.completeExceptionally(error);
            }
            if (closed.compareAndSet(false, true)) {
                try {
                    onClose();
                } finally {
                    callback.onFailure(error);
                }
            }
        }


        int getRequiredJoins() {
            return requiredJoins;
        }

        AtomicBoolean getEnoughJoins() {
            return enoughJoins;
        }
    }
}
