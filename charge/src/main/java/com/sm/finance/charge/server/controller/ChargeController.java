package com.sm.finance.charge.server.controller;

import com.sm.finance.charge.server.core.ChargeServer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * @author shifeng.luo
 * @version created on 2017/10/22 上午10:48
 */
@Controller
public class ChargeController {

    @Resource
    private ChargeServer chargeServer;

    @RequestMapping("/start")
    public void start() {
        chargeServer.start();
    }
}
