package com.sm.finance.charge.common.base;

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

    private static ConcurrentMap<String, Configure> configures = new ConcurrentHashMap<>();

    public static Configure loader(String fileName) {
        Configure configure = configures.get(fileName);
        if (configure != null) {
            return configure;
        }

        synchronized (ConfigureLoader.class) {
            configure = configures.get(fileName);
            if (configure == null) {
                InputStream inputStream = ConfigureLoader.class.getClassLoader().getResourceAsStream(fileName);

                try {
                    if (inputStream == null) {
                        throw new RuntimeException("找不到配置文件:" + fileName);
                    }
                    Properties properties = new Properties();
                    properties.load(inputStream);
                    configure = new Configure(fileName, properties);
                    configures.putIfAbsent(fileName, configure);
                    return configure;
                } catch (IOException e) {
                    throw new RuntimeException("找不到配置文件:" + fileName);
                }
            }
        }

        return configure;
    }
}
