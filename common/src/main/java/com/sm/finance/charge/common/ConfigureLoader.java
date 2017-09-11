package com.sm.finance.charge.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:57
 */
public class ConfigureLoader {

    private static ConcurrentMap<String, Properties> configures = new ConcurrentHashMap<>();

    public static Properties loader(String config) {
        Properties properties = configures.get(config);
        if (properties != null) {
            return properties;
        }

        synchronized (ConfigureLoader.class) {
            properties = configures.get(config);
            if (properties == null) {
                InputStream inputStream = ConfigureLoader.class.getClassLoader().getResourceAsStream(config);

                try {
                    if (inputStream == null) {
                        throw new RuntimeException("找不到配置文件:" + config);
                    }
                    properties = new Properties();
                    properties.load(inputStream);
                    configures.putIfAbsent(config, properties);
                    return properties;
                } catch (IOException e) {
                    throw new RuntimeException("找不到配置文件:" + config);
                }
            }
        }

        return properties;
    }
}
