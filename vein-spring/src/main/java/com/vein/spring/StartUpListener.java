package com.vein.spring;

import com.vein.common.base.LoggerSupport;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author shifeng.luo
 * @version created on 2017/11/9 下午3:34
 */
@Service
public class StartUpListener extends LoggerSupport implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private ChargeServer chargeServer;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            logger.info("start to start task center");
//            chargeServer.start();
        }
    }
}
