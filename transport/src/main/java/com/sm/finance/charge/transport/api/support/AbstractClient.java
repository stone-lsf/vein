package com.sm.finance.charge.transport.api.support;

import com.sm.finance.charge.common.AbstractService;
import com.sm.finance.charge.common.Address;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.TransportClient;

import java.util.concurrent.CompletableFuture;

/**
 * @author shifeng.luo
 * @version created on 2017/10/24 下午7:17
 */
public abstract class AbstractClient extends AbstractService implements TransportClient {

    @Override
    public CompletableFuture<Connection> connect(Address address, int retryTimes) {
        CompletableFuture<Connection> result = new CompletableFuture<>();

        this.connect(address).whenComplete((connection, error) -> {
            if (error != null) {
                logger.warn("connect to address:[{}] caught exception:{}", address, error);
                if (retryTimes > 0) {
                    connect(address, retryTimes - 1).whenComplete((conn, e) -> {
                        if (e == null) {
                            result.complete(conn);
                        } else {
                            result.completeExceptionally(e);
                        }
                    });
                } else {
                    result.completeExceptionally(error);
                }
            } else {
                result.complete(connection);
            }
        });

        return result;
    }

}
