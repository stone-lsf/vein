package com.sm.finance.charge.transport.api;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.CloseListener;
import com.sm.finance.charge.common.IntegerIdGenerator;
import com.sm.finance.charge.common.ReflectUtil;
import com.sm.finance.charge.transport.api.exceptions.RemoteException;
import com.sm.finance.charge.transport.api.exceptions.TimeoutException;
import com.sm.finance.charge.transport.api.handler.RequestHandler;
import com.sm.finance.charge.transport.api.handler.ResponseHandler;
import com.sm.finance.charge.transport.api.support.HandleListener;
import com.sm.finance.charge.transport.api.support.RequestContext;
import com.sm.finance.charge.transport.api.support.ResponseContext;
import com.sm.finance.charge.transport.api.support.TimeoutScheduler;

import java.io.IOException;
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
    protected final Address remoteAddress;
    protected final Address localAddress;
    protected final int defaultTimeout;
    protected final TimeoutScheduler timeoutScheduler;

    protected final IntegerIdGenerator idGenerator = new IntegerIdGenerator();
    protected final CopyOnWriteArrayList<CloseListener> listeners = new CopyOnWriteArrayList<>();

    protected final ConcurrentMap<Class, RequestHandler> requestHandlers = new ConcurrentHashMap<>();
    protected final ConcurrentMap<Integer, CompletableFuture> responseFutures = new ConcurrentHashMap<>();

    public AbstractConnection(Address remoteAddress, Address localAddress, int defaultTimeout) {
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.defaultTimeout = defaultTimeout;
        this.timeoutScheduler = new TimeoutScheduler(this);
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
        try {
            if (message instanceof Response) {
                sendResponse((Response) message);
            } else {
                int id = idGenerator.nextId();
                Request request = new Request(id, message);
                sendRequest(request);
            }
        } catch (Exception e) {
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
        logger.info("receive request:{}", request);
        Object requestMessage = request.getMessage();
        RequestHandler handler = requestHandlers.get(requestMessage.getClass());
        if (handler == null) {
            logger.error("[{}] don't have matched handler ", requestMessage.getClass());
            return;
        }

        int requestId = request.getId();
        try {
            RequestContext context = new RequestContext(requestId, this, remoteAddress);
            CompletableFuture<Object> responseFuture = handler.handle(requestMessage, context);
            if (responseFuture == null) {
                return;
            }


            responseFuture.whenComplete((response, error) -> {
                if (error != null) {
                    handleRequestFailure(requestId, error, handler.getAllListeners());
                } else {
                    handleRequestSuccess(requestId, response, handler.getAllListeners());
                }
            });
        } catch (Throwable e) {
            handleRequestFailure(requestId, e, handler.getAllListeners());
        }
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
        logger.info("receive response:{}", response);

        CompletableFuture future = responseFutures.remove(response.getId());
        if (future == null) {
            logger.info("received timeout response:[{}],connection:{}", response, getConnectionId());
            return;
        }

        timeoutScheduler.cancel(response.getId());
        if (!response.hasException()) {
            future.complete(response.getMessage());
        } else {
            future.completeExceptionally(response.getException());
        }
    }

    @Override
    public void addCloseListener(CloseListener listener) {
        listeners.add(listener);
    }

    @Override
    protected void doClose() {
        responseFutures.clear();
        for (CloseListener listener : listeners) {
            listener.onClose();
        }
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
}
