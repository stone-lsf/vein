package com.sm.finance.charge.common;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI 服务加载器
 *
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:52
 */
public class ServiceLoader {

    private static final ConcurrentHashMap<Class, Map<String, Object>> map = new ConcurrentHashMap<>();

    private static final String PREFIX = "META-INF/services/";

    private static final ClassLoader loader = ServiceLoader.class.getClassLoader();

    @SuppressWarnings("unchecked")
    public static <T> T findService(String serviceName, Class<T> service) {
        Preconditions.checkNotNull(service, "接口类型不能为空");
        Map<String, Object> serviceMap = map.get(service);
        if (serviceMap != null) {
            return (T) getService(serviceMap, serviceName);
        }

        serviceMap = loadService(service);
        map.putIfAbsent(service, serviceMap);
        return (T) getService(serviceMap, serviceName);

    }

    private static Object getService(Map<String, Object> serviceMap, String serviceName) {
        Object service = serviceMap.get(serviceName);
        Preconditions.checkNotNull(service, "没有找到:" + serviceName + "对应的服务实现");
        return service;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadService(Class service) {
        String fullName = PREFIX + service.getName();
        Properties properties = ConfigureLoader.loader(fullName);
        Map<String, String> allServiceImpl = getAllImpl(properties);

        Map<String, Object> map = new HashMap<>();
        try {
            for (String serviceKey : allServiceImpl.keySet()) {
                String serviceImpl = allServiceImpl.get(serviceKey);
                Class c = Class.forName(serviceImpl, false, ServiceLoader.loader);

                if (!service.isAssignableFrom(c)) {
                    fail(service, "的实现者: " + serviceImpl + " 不是一个子类型");
                }

                Object impl = null;
                try {
                    impl = service.cast(c.newInstance());
                } catch (Throwable e) {
                    fail(service, "的实现者 " + serviceImpl + " 不能被初始化", e);
                }
                map.put(serviceKey, impl);
            }
        } catch (ClassNotFoundException e) {
            fail(service, "未找到服务实现者:" + allServiceImpl);
        }

        return map;
    }

    private static Map<String, String> getAllImpl(Properties properties) {
        Map<String, String> map = new HashMap<>();
        Set<String> propertyNames = properties.stringPropertyNames();
        for (String property : propertyNames) {
            map.put(property, properties.getProperty(property));
        }

        return map;
    }

    private static void fail(Class<?> service, String msg) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private static void fail(Class<?> service, String msg, Throwable cause) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg, cause);
    }
}
