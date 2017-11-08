package com.sm.finance.charge.server.core;

import com.sm.charge.cluster.ClusterConfig;
import com.sm.charge.cluster.ClusterService;
import com.sm.charge.cluster.ClusterServiceImpl;
import com.sm.finance.charge.common.SystemConstants;
import com.sm.finance.charge.common.base.Configure;
import com.sm.finance.charge.common.base.ConfigureLoader;
import com.sm.finance.charge.common.base.LoggerSupport;
import com.sm.finance.charge.transport.api.Connection;
import com.sm.finance.charge.transport.api.Transport;
import com.sm.finance.charge.transport.api.TransportFactory;
import com.sm.finance.charge.transport.api.TransportServer;
import com.sm.finance.charge.transport.api.exceptions.BindException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/10/22 上午11:11
 */
@Component
public class ChargeServer extends LoggerSupport {

    private ClusterService clusterService;

    private TransportServer transportServer;

    @Value("${charge.transport.type}")
    private String transportType;

    @Value("${charge.transport.port}")
    private int port;


    public void start() {
        listenPort();

        String profile = SystemConstants.PROFILE == null ? "dev" : SystemConstants.PROFILE;
        Configure configure = ConfigureLoader.loader(profile + File.separator + "cluster.properties");
        clusterService = new ClusterServiceImpl(new ClusterConfig(configure), new PrintLogStateMachine());

        try {
            clusterService.start();
        } catch (Exception e) {
            logger.error("clusterService start caught exception", e);
            throw new RuntimeException(e);
        }
    }

    private void listenPort() {
        Transport transport = TransportFactory.create(transportType);
        this.transportServer = transport.server();
        try {
            transportServer.listen(port, (Connection connection) -> logger.info("accept connection:{}", connection.getConnectionId()));
        } catch (BindException e) {
            logger.error("bind port:{} caught exception", port, e);
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            transportServer.close();
        } catch (Exception e) {
            logger.error("close charge server caught exception", e);
            throw new RuntimeException(e);
        }
    }
}
