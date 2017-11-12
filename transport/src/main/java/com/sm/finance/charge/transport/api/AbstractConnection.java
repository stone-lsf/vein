package com.sm.finance.charge.transport.api;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.IntegerIdGenerator;
import com.sm.finance.charge.common.base.CloseListener;
import com.sm.finance.charge.common.utils.ReflectUtil;
import com.sm.finance.charge.transport.api.exceptions.RemoteException;
import com.sm.finance.charge.transport.api.exceptions.TimeoutException;
import com.sm.finance.charge.transport.api.handler.RequestHandler;
import com.sm.finance.charge.transport.api.handler.ResponseHandler;
import com.sm.finance.charge.transport.api.support.HandleListener;
import com.sm.finance.charge.transport.api.support.RequestContext;
import com.sm.finance.charge.transport.api.support.ResponseContext;
import com.sm.finance.charge.transport.api.support.TimeoutScheduler;

import org.joda.time.DateTime;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:45
 */
public abstract class AbstractConnection extends AbstractService implements Connection {
    private final Address remoteAddress;
    private final Address localAddress;
    private final String connectionId;
    protected final int defaultTimeout;
    protected final TimeoutScheduler timeoutScheduler;

    private final IntegerIdGenerator idGenerator = new IntegerIdGenerator();
    private final CopyOnWriteArrayList<CloseListener> listeners = new CopyOnWriteArrayList<>();

    private final ConcurrentMap<Class, RequestHandler> requestHandlers = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, CompletableFuture> responseFutures = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, RequestInfo> requestMap = new ConcurrentHashMap<>();

    public AbstractConnection(Address remoteAddress, Address localAddress, int defaultTimeout) {
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.defaultTimeout = defaultTimeout;
        this.timeoutScheduler = new TimeoutScheduler(this);
        this.connectionId = buildConnectionId();
    }

    @Override
    public void registerHandlers(List<RequestHandler> handlers) {
        for (RequestHandler handler : handlers) {
            Class<Object> type = ReflectUtil.getSuperClassGenericType(handler.getClass());
            requestHandlers.put(type, handler);
        }
    }


    @Override
    public void send(Object message) throws IOException {
        StringBuilder logBuilder = new StringBuilder();
        try {
            if (message instanceof Response) {
                sendResponse((Response) message);
            } else {
                int id = idGenerator.nextId();
                Request request = new Request(id, message);
                logRequest(logBuilder, 0, id, message);
                sendRequest(request);
                logResponse(logBuilder, null);
            }
        } catch (Exception e) {
            logException(logBuilder, e);
            throw new IOException(e);
        }
    }

    @Override
    public <T> void send(Object message, ResponseHandler<T> handler) {
        send(message, defaultTimeout, handler);
    }

    @Override
    public <T> void send(Object message, int timeout, ResponseHandler<T> handler) {
        this.<T>request(message, timeout).whenComplete((response, error) -> {
            if (error != null) {
                ResponseContext context = new ResponseContext(this, error, remoteAddress);
                handler.onException(error, context);
            } else {
                handler.handle(response, this);
            }
        });
    }

    @Override
    public <T> CompletableFuture<T> request(Object message) {
        return request(message, defaultTimeout);
    }

    @Override
    public <T> CompletableFuture<T> request(Object message, int timeout) {
        CompletableFuture<T> result = new CompletableFuture<>();
        int id = idGenerator.nextId();
        Request request = new Request(id, message);
        responseFutures.put(id, result);
        requestMap.put(id, new RequestInfo(request, System.currentTimeMillis()));
        try {
            sendRequest(request, timeout);
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    protected abstract void sendRequest(Request request, int timeout) throws Exception;

    protected abstract void sendRequest(Request request) throws Exception;

    protected abstract void sendResponse(Response response) throws Exception;

    @Override
    public <T> T syncRequest(Object message) throws IOException {
        return syncRequest(message, defaultTimeout);
    }

    @Override
    public <T> T syncRequest(Object message, int timeout) throws IOException {
        CompletableFuture<T> future = request(message, timeout);
        try {
            return future.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RemoteException) {
                throw (RemoteException) cause;
            }

            if (cause instanceof TimeoutException) {
                throw (TimeoutException) cause;
            }

            throw new IOException(cause);
        }
    }


    @Override
    public void onMessage(Object message) {
        if (message instanceof Request) {
            Request request = (Request) message;
            handRequest(request);
        } else if (message instanceof Response) {
            Response response = (Response) message;
            handResponse(response);
        } else {
            logger.warn("error message ,neither request nor response:{}", message);
        }
    }

    @SuppressWarnings("unchecked")
    private void handRequest(Request request) {
        Object message = request.getMessage();
        RequestHandler handler = requestHandlers.get(message.getClass());
        if (handler == null) {
            logger.error("[{}] don't have matched com.sm.charge.memory.handler ", message.getClass());
            return;
        }

        int requestId = request.getId();
//        StringBuilder logBuilder = new StringBuilder();
//        DateTime now = DateTime.now();
//        String startTime = now.toString("yyyy-MM-dd HH:mm:ss.sss");
//        logHandleRequest(logBuilder, startTime, requestId, message);
        try {
            RequestContext context = new RequestContext(requestId, this, remoteAddress);
            CompletableFuture<Object> responseFuture = handler.handle(message, context);
            if (responseFuture == null) {
//                logHandleResponse(logBuilder, now, null);
                return;
            }

            responseFuture.whenComplete((response, error) -> {
                if (error != null) {
//                    logHandleException(logBuilder, now, error);
                    handleRequestFailure(requestId, error, handler.getAllListeners());
                } else {
//                    logHandleResponse(logBuilder, now, response);
                    handleRequestSuccess(requestId, response, handler.getAllListeners());
                }
            });
        } catch (Throwable e) {
//            logHandleException(logBuilder, now, e);
            handleRequestFailure(requestId, e, handler.getAllListeners());
        }
    }

    private void logHandleRequest(StringBuilder logBuilder, String startTime, int requestId, Object message) {
        logBuilder.append("###").append("handleRequest")
            .append("###").append(startTime)
            .append("###").append(requestId)
            .append("###").append(message);
    }

    private void logHandleResponse(StringBuilder logBuilder, DateTime start, Object response) {
        long useTime = System.currentTimeMillis() - start.getMillis();
        logBuilder.append("###").append(response == null ? "" : response)
            .append("###").append(connectionId)
            .append("###").append(useTime);
        logger.info(logBuilder.toString());
    }

    private void logHandleException(StringBuilder logBuilder, DateTime start, Throwable throwable) {
        long useTime = System.currentTimeMillis() - start.getMillis();
        StringBuffer message = getThrowableMessage(throwable);
        logBuilder.append("###").append(message)
            .append("###").append(connectionId)
            .append("###").append(useTime);
        logger.error(logBuilder.toString());
    }

    private void handleRequestSuccess(int requestId, Object responseMessage, List<HandleListener> listeners) {
        Response response = responseMessage == Response.EMPTY_MESSAGE ? new Response(requestId) : new Response(requestId, responseMessage);

        try {
            sendResponse(response);
        } catch (Exception e) {
            handleRequestFailure(requestId, e, listeners);
            return;
        }

        for (HandleListener listener : listeners) {
            listener.onSuccess();
        }
    }

    private void handleRequestFailure(int requestId, Throwable error, List<HandleListener> listeners) {
        if (listeners != null) {
            for (HandleListener listener : listeners) {
                listener.onFail(error);
            }
        }

        RemoteException exception = new RemoteException(error);
        Response response = new Response(requestId, exception);
        try {
            sendResponse(response);
        } catch (Exception e) {
            logger.error("send response failed, cased by exception", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void handResponse(Response response) {
        CompletableFuture future = responseFutures.remove(response.getId());
        if (future == null) {
            return;
        }

        RequestInfo info = requestMap.remove(response.getId());
        StringBuilder logBuilder = new StringBuilder();
        long useTime = System.currentTimeMillis() - info.startTime;
        logRequest(logBuilder, useTime, info.request.getId(), info.request.getMessage());

        timeoutScheduler.cancel(response.getId());
        if (!response.hasException()) {
            logResponse(logBuilder, response.getMessage());
            future.complete(response.getMessage());
        } else {
            logException(logBuilder, response.getException());
            future.completeExceptionally(response.getException());
        }
    }

    private void logRequest(StringBuilder logBuilder, long useTime, int requestId, Object request) {
        logBuilder.append("###").append(useTime)
            .append("###").append(requestId)
            .append("###").append(request);
    }

    private void logResponse(StringBuilder logBuilder, Object response) {
        logBuilder.append("###").append(response == null ? "" : response)
            .append("###").append(connectionId);
        logger.info(logBuilder.toString());
    }

    private void logException(StringBuilder logBuilder, Throwable throwable) {
        StringBuffer message = getThrowableMessage(throwable);

        logBuilder.append("###").append(message)
            .append("###").append(connectionId);
        logger.error(logBuilder.toString());
    }

    private StringBuffer getThrowableMessage(Throwable throwable) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        return writer.getBuffer();
    }

    @Override
    public void addCloseListener(CloseListener listener) {
        listeners.add(listener);
    }

    @Override
    protected void doClose() throws Exception {
        responseFutures.clear();
        for (CloseListener listener : listeners) {
            listener.onClose();
        }
    }

    @Override
    public String getConnectionId() {
        return connectionId;
    }

    @Override
    public boolean closed() {
        return closed.get();
    }

    @Override
    public Address localAddress() {
        return localAddress;
    }

    @Override
    public Address remoteAddress() {
        return remoteAddress;
    }

    private String buildConnectionId() {
        return localAddress.getIp() + ":" + localAddress.getPort() + "/" + remoteAddress.getIp() + ":" + remoteAddress.getPort();
    }

    private class RequestInfo {
        private Request request;
        private long startTime;

        RequestInfo(Request request, long startTime) {
            this.request = request;
            this.startTime = startTime;
        }
    }
}
