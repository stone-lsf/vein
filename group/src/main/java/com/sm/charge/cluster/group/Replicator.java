package com.sm.charge.cluster.group;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午10:45
 */
public class Replicator<T> { //extends AbstractService {

//    private final Server self;
//    private final ServerGroup group;
//    private final int maxBatchSize;
//    private Store<T> store;
//    private Map<Long, CompletableFuture> futureMap = new ConcurrentHashMap<>();
//    private final ExecutorService executorService;
//
//    public Replicator(Server self, ServerGroup group, int maxBatchSize) {
//        this.self = self;
//        this.group = group;
//        this.maxBatchSize = maxBatchSize;
//        this.executorService = Executors.newScheduledThreadPool(PROCESSORS + 1, new NamedThreadFactory("ReplicatePool"));
//    }
//
//
//    @Override
//    protected void doStart() throws Exception {
//        List<Server> servers = group.getServers();
//        for (Server server : servers) {
//            if (!server.getAddress().equals(group.getLeader().getAddress())) {
//                executorService.execute(() -> replicateTo(server));
//            }
//        }
//    }
//
//    @Override
//    protected void doClose() {
//
//    }
//
//    public CompletableFuture<Boolean> replicate(BaseObject<T> obj) {
//        CompletableFuture<Boolean> future = new CompletableFuture<>();
//        long index = store.add(obj);
//
//        CompletableFuture<?> commitFuture = new CompletableFuture<>();
//        commitFuture.whenComplete((result, error) -> {
//            if (error == null) {
//                future.complete(true);
//            } else {
//                future.completeExceptionally(error);
//            }
//        });
//        futureMap.put(index, commitFuture);
//        return future;
//    }
//
//
//    public void replicateTo(Server server) {
//        ReplicateRequest request = buildReplicateRequest(server);
//        Connection connection = server.getConnection();
//        if (connection == null) {
//            return;
//        }
//
//        connection.<ReplicateResponse>request(request).whenComplete((response, error) -> {
//            if (error == null) {
//                this.handleReplicateResponse(response, server);
//            } else {
//                this.handleReplicateResponseFailure(server, error);
//            }
//        });
//    }
//
//    private ReplicateRequest buildReplicateRequest(Server server) {
//        ReplicateRequest<T> request = new ReplicateRequest<>();
//        request.setLeader(server.getAddress());
//        request.setCommitIndex(server.getCommitIndex());
//
//
//        List<Entry<T>> objects = new ArrayList<>(maxBatchSize);
//        long index = server.getReplicateIndex();
//        long lastIndex = store.lastIndex();
//        int size = 0;
//        for (; index <= lastIndex; index++) {
//            BaseObject<T> object = store.get(index);
//            if (object == null) {
//                continue;
//            }
//            size++;
//            if (size >= maxBatchSize) {
//                break;
//            }
//
//            objects.add(new Entry<>(index, object));
//        }
//        request.setEntries(objects);
//
//        return request;
//    }
//
//
//    public CompletableFuture<ReplicateResponse> handleReplicate(ReplicateRequest<T> request) {
//        ReplicateResponse response = new ReplicateResponse();
//        response.setSuccess(true);
//
//        List<Entry<T>> entries = request.getEntries();
//        if (CollectionUtils.isEmpty(entries)) {
//            response.setNextIndex(store.lastIndex());
//            return CompletableFuture.completedFuture(response);
//        }
//
//        long prevIndex = entries.get(0).getIndex() - 1;
//        store.truncate(prevIndex);
//        long lastIndex = 0;
//        for (Entry<T> entry : entries) {
//            long skipSize = entry.getIndex() - lastIndex - 1;
//            store.skip(skipSize);
//            store.add(entry.getObject());
//            lastIndex = entry.getIndex();
//        }
//
//        commit(request.getCommitIndex(), self);
//        response.setNextIndex(store.lastIndex() + 1);
//        return CompletableFuture.completedFuture(response);
//    }
//
//
//    public void handleReplicateResponse(ReplicateResponse response, Server server) {
//        server.setReplicateIndex(response.getNextIndex());
//        if (response.isSuccess()) {
//            server.setMatchIndex(response.getNextIndex() - 1);
//            replicateSuccess();
//            if (!closed.get()) {
//                executorService.execute(() -> replicateTo(server));
//            }
//            return;
//        }
//
//        if (!closed.get()) {
//            executorService.execute(() -> replicateTo(server));
//        }
//    }
//
//    private void replicateSuccess() {
//        List<Server> servers = group.getServers();
//        servers.sort(Comparator.comparingLong(member -> member.getMatchIndex()));
//        int quorum = group.getQuorum();
//        if (servers.size() < quorum) {
//            return;
//        }
//        Server server = servers.get(quorum - 1);
//        long commitIndex = server.getMatchIndex();
//
//        Server leader = group.getLeader();
//        long prevCommittedIndex = leader.getCommitIndex();
//        if (commitIndex > prevCommittedIndex) {
//            commit(commitIndex, leader);
//            while (++prevCommittedIndex <= commitIndex) {
//                CompletableFuture<?> future = futureMap.get(prevCommittedIndex);
//                if (future != null) {
//                    future.complete(null);
//                }
//            }
//        }
//    }
//
//    private void commit(long commitIndex, Server server) {
//        store.commit(Math.min(commitIndex, store.lastIndex()));
//        server.setCommitIndex(commitIndex);
//    }
//
//    public void handleReplicateResponseFailure(Server server, Throwable error) {
//        logger.error("replicate data to:{} failed:{}", server.getAddress(), error);
//        if (!closed.get()) {
//            executorService.execute(() -> replicateTo(server));
//        }
//    }


}
