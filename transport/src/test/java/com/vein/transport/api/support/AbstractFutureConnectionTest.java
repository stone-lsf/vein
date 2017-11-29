package com.vein.transport.api.support;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;


/**
 * @author shifeng.luo
 * @version created on 2017/9/23 下午3:13
 */
public class AbstractFutureConnectionTest {


    @Test
    public void exceptionally() {
        try {
            String result = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (1 == 1) {
                    throw new RuntimeException("测试一下异常情况");
                }
                return "s1";
            }).exceptionally(e -> {
                System.out.println(e.getMessage());
                throw new NullPointerException();
            }).join();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}