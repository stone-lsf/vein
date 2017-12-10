package com.vein.view.controller;

import com.vein.common.base.AjaxResponse;
import com.vein.common.base.LoggerSupport;
import com.vein.raft.client.RaftClient;
import com.vein.view.views.PrintCommand;
import com.vein.view.views.raft.RaftNodeInfo;

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

    @RequestMapping("/send")
    public AjaxResponse sendMessage(int count) {
        String test = "test";
        for (int i = 0; i < count; i++) {
            PrintCommand command = new PrintCommand(test + i);
            raftClient.submit(command);
        }

        return AjaxResponse.success();
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
