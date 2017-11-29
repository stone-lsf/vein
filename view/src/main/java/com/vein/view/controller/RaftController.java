package com.vein.view.controller;

import com.vein.raft.client.RaftClient;
import com.vein.view.views.raft.RaftNodeInfo;
import com.vein.common.base.AjaxResponse;
import com.vein.common.base.LoggerSupport;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * @author shifeng.luo
 * @version created on 2017/11/18 下午7:15
 */
@Controller
@RequestMapping("/raft")
public class RaftController extends LoggerSupport implements InitializingBean {

    @Resource
    private RaftClient raftClient;


    @RequestMapping("/all")
    public AjaxResponse<RaftNodeInfo> getAllNodeInfo() {

        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("================================");
        try {
            raftClient.start();
        } catch (Throwable e) {
            logger.error("start error", e);
        }
    }
}
