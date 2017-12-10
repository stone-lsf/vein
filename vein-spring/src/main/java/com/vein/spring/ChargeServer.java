package com.vein.spring;

import com.vein.cluster.ClusterConfig;
import com.vein.cluster.ClusterService;
import com.vein.cluster.ClusterServiceImpl;
import com.vein.common.SystemConstants;
import com.vein.common.base.Configure;
import com.vein.common.base.ConfigureLoader;
import com.vein.common.base.LoggerSupport;
import com.vein.transport.api.Connection;
import com.vein.transport.api.Transport;
import com.vein.transport.api.TransportFactory;
import com.vein.transport.api.TransportServer;
import com.vein.transport.api.exceptions.BindException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/10/22 上午11:11
 */
@Service
public class ChargeServer extends LoggerSupport implements InitializingBean{

    private ClusterService clusterService;

    private TransportServer transportServer;

    @Value("${charge.transport.type}")
    private String transportType;

    @Value("${charge.transport.port}")
    private int port;


    public void start() {
        listenPort();

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
            clusterService.close();
            transportServer.close();
        } catch (Exception e) {
            logger.error("close charge server caught exception", e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        String profile = SystemConstants.PROFILE == null ? "dev" : SystemConstants.PROFILE;
        Configure configure = ConfigureLoader.loader(profile + File.separator + "cluster.properties");
        clusterService = new ClusterServiceImpl(new ClusterConfig(configure), new PrintLogStateMachine());
    }

    public ClusterService getClusterService() {
        return clusterService;
    }
}
