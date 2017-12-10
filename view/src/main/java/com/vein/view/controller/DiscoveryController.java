package com.vein.view.controller;

import com.vein.common.SystemConstants;
import com.vein.common.base.AjaxResponse;
import com.vein.common.base.Configure;
import com.vein.common.base.ConfigureLoader;
import com.vein.discovery.DiscoveryConfig;
import com.vein.discovery.DiscoveryServer;
import com.vein.discovery.DiscoveryServerImpl;
import com.vein.discovery.Nodes;
import com.vein.view.views.raft.RaftNodeInfo;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;

/**
 * @author shifeng.luo
 * @version created on 2017/10/22 上午10:48
 */
@Controller
@RequestMapping("/discovery")
public class DiscoveryController implements InitializingBean {

    private DiscoveryServer discoveryServer;

    @RequestMapping("/all")
    public AjaxResponse<RaftNodeInfo> getAllNodeInfo() {
        Nodes nodes = discoveryServer.getNodes();
        return null;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        String profile = SystemConstants.PROFILE == null ? "dev" : SystemConstants.PROFILE;

        Configure configure = ConfigureLoader.loader(profile + File.separator + "discovery.properties");
        discoveryServer = new DiscoveryServerImpl(new DiscoveryConfig(configure));
        discoveryServer.start();
        discoveryServer.join();
    }
}
