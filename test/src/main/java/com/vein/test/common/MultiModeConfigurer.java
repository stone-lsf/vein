package com.vein.test.common;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.Properties;

/**
 * @author shifeng.luo
 * @version created on 2017/10/22 上午11:01
 */
public class MultiModeConfigurer extends PropertyPlaceholderConfigurer {

    //静态初始化块
    static {
        //获取java参数中的property,默认使用开发环境的配置
        String argName = "profile";
        String devConf = "dev";
        String profile = System.getProperty(argName);
        if (profile == null) {
            profile = devConf;
        }

        //写入系统变量，并打印出信息
        System.setProperty(argName, profile);
        System.out.println("----------------------------------------------");
        System.out.println("You are using the configuration in folder " + profile);
        System.out.println("----------------------------------------------");

        //停顿两秒作为提示
        try {
            Thread.sleep(1000 * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void convertProperties(Properties props) {
        super.convertProperties(props);
        properties.putAll(props);
    }


    /**
     * 配置项的集合
     */
    private final static Properties properties = new Properties();

    /**
     * 获取配置文件中的配置项
     *
     * @return <br/>created by Tianxin on 2015年8月5日 下午6:06:44
     */
    public static String getPropertyByKey(String key) {
        return properties.getProperty(key);
    }
}
