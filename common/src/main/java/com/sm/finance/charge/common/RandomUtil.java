package com.sm.finance.charge.common;

import com.google.common.base.Preconditions;

import java.util.Random;

/**
 * @author shifeng.luo
 * @version created on 2017/9/18 下午3:35
 */
public class RandomUtil {

    /**
     * 生成一个指定边界值的随机数
     *
     * @param bound 边界值
     * @return 随机数
     */
    public static int random(int bound) {
        Random random = new Random();
        return random.nextInt(bound);
    }

    /**
     * 生成一个处于min和max之间的一个随机数
     *
     * @param min 下限
     * @param max 上限
     * @return 返回一个result, min<=result<max
     */
    public static int between(int min, int max) {
        Preconditions.checkState(min < max, "min[" + min + "] must less than max[" + max + "]");
        Random random = new Random();
        return min + random.nextInt(max - min);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println(RandomUtil.random(200));
        }
    }
}
