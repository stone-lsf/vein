package com.sm.finance.charge.transport.api.support;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.common.CloseListener;
import com.sm.finance.charge.common.IntegerIdGenerator;
import com.sm.finance.charge.common.utils.ReflectUtil;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.Request;
import com.sm.finance.charge.transport.api.Response;
import com.sm.finance.charge.transport.api.exceptions.RemoteException;
import com.sm.finance.charge.transport.api.exceptions.TimeoutException;
import com.sm.finance.charge.transport.api.handler.AbstractExceptionResponseHandler;
import com.sm.finance.charge.transport.api.handler.RequestHandler;
import com.sm.finance.charge.transport.api.handler.ResponseHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午3:45
 */
public abstract class AbstractConnection1 extends AbstractService implements Connection {
    protected final Address remoteAddress;
    protected final Address localAddress;
    protected final int defaultTimeout;

    protected final IntegerIdGenerator idGenerator = new IntegerIdGenerator();
    protected final CopyOnWriteArrayList<CloseListener> listeners = new CopyOnWriteArrayList<>();

    protected final ConcurrentMap<Class, RequestHandler> requestHandlers = new ConcurrentHashMap<>();
    protected final ConcurrentMap<Integer, ResponseHandler> responseHandlers = new ConcurrentHashMap<>();

    public AbstractConnection1(Address remoteAddress, Address localAddress, int defaultTimeout) {
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.defaultTimeout = defaultTimeout;
    }

    @Override
    public void registerHandlers(List<RequestHandler> handlers) {
        for (RequestHandler handler : handlers) {
            Class<Object> type = ReflectUtil.getSuperClassGenericType(handler.getClass());
            requestHandlers.put(type, handler);
        }
    }


    @Override
    public <T> void send(Object message, ResponseHandler<T> handler) {
        send(message, defaultTimeout, handler);
    }

    @Override
    public <T> void send(Object message, int timeout, ResponseHandler<T> handler) {
        sendMessage(message, timeout, handler);
    }

    @Override
    public <T> CompletableFuture<T> request(Object message) {
        return request(message, defaultTimeout);
    }

    @Override
    public <T> CompletableFuture<T> request(Object message, int timeout) {
        CompletableFuture<T> result = new CompletableFuture<>();
        sendMessage(message, timeout, new AbstractExceptionResponseHandler<T>() {
            @Override
            protected void onRemoteException(RemoteException e, ResponseContext context) {
                result.completeExceptionally(e);
            }

            @Override
            protected void onTimeoutException(TimeoutException e, ResponseContext context) {
                result.completeExceptionally(e);
            }

            @Override
            public void handle(T response, Connection connection) {
                result.complete(response);
            }
        });

        return result;
    }

    @Override
    public <T> T syncRequest(Object message) throws IOException {
        return syncRequest(message, defaultTimeout);
    }

    @Override
    public <T> T syncRequest(Object message, int timeout) throws IOException {
        CompletableFuture<T> future = request(message, timeout);
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new IOException(e);
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
    }

    protected abstract <T> void sendMessage(Object message, int timeout, ResponseHandler<T> handler);

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
            logger.error("[{}] don't have matched com.sm.charge.memory.handler ", requestMessage.getClass());
            return;
        }

        int id = request.getId();
        try {
            RequestContext context = new RequestContext(id, this, remoteAddress);
            Object responseMessage = handler.handle(requestMessage, context);
            if (responseMessage == null) {
                return;
            }
            Response response = responseMessage == Response.EMPTY_MESSAGE ? new Response(id) : new Response(id, responseMessage);
            List<HandleListener> listeners = handler.getAllListeners();
            if (listeners == null) {
                sendResponse(response);
                return;
            }

            for (HandleListener listener : listeners) {
                listener.onSuccess();
            }

            sendResponse(response);
        } catch (Exception e) {
            List<HandleListener> listeners = handler.getAllListeners();
            if (listeners != null) {
                for (HandleListener listener : listeners) {
                    listener.onFail(e);
                }
            }

            RemoteException exception = new RemoteException(e);
            Response response = new Response(id, exception);
            sendResponse(response);
        }
    }

    private void sendResponse(Response response) {
        try {
            send(response);
        } catch (Exception e) {
            logger.error("send response:{} failure ", response);
        }
    }

    @SuppressWarnings("unchecked")
    private void handResponse(Response response) {
        logger.info("receive response:{}", response);
        ResponseHandler handler = responseHandlers.remove(response.getId());
        if (handler == null) {
            logger.info("received timeout response:[{}],connection:{}", response, getConnectionId());
            return;
        }


        if (!response.hasException()) {
            handler.handle(response.getMessage(), this);
        } else {
            ResponseContext context = new ResponseContext(this, response.getException(), remoteAddress);
            handler.onException(response.getException(), context);
        }
    }

    @Override
    public void addCloseListener(CloseListener listener) {
        listeners.add(listener);
    }

    @Override
    protected void doClose() {
        responseHandlers.clear();
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
