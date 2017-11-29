package com.vein.common.base;

import com.vein.common.utils.ExpressionUtil;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author shifeng.luo
 * @version created on 2017/10/23 下午4:41
 */
public class Configure {

    private final String name;
    private final Properties properties;

    public Configure(String name, Properties properties) {
        this.name = name;
        this.properties = properties;
    }

    public byte getByte(String key) {
        String value = check(key);
        return Byte.parseByte(value);
    }

    public byte getByte(String key, byte defaultValue) {
        String value = properties.getProperty(key);
        return StringUtils.isBlank(value) ? defaultValue : Byte.parseByte(value);
    }

    public int getInt(String key) {
        String value = check(key);
        return ExpressionUtil.getInt(value);
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        return StringUtils.isBlank(value) ? defaultValue : ExpressionUtil.getInt(value);
    }

    public short getShort(String key) {
        String value = check(key);
        return ExpressionUtil.getShort(value);
    }

    public short getShort(String key, short defaultValue) {
        String value = properties.getProperty(key);
        return StringUtils.isBlank(value) ? defaultValue : ExpressionUtil.getShort(value);
    }


    public long getLong(String key) {
        String value = check(key);
        return ExpressionUtil.getLong(value);
    }

    public long getLong(String key, long defaultValue) {
        String value = properties.getProperty(key);
        return StringUtils.isBlank(value) ? defaultValue : ExpressionUtil.getLong(value);
    }


    public double getDouble(String key) {
        String value = check(key);
        return ExpressionUtil.getDouble(value);
    }

    public double getDouble(String key, double defaultValue) {
        String value = properties.getProperty(key);
        return StringUtils.isBlank(value) ? defaultValue : ExpressionUtil.getDouble(value);
    }


    public float getFloat(String key) {
        String value = check(key);
        return ExpressionUtil.getFloat(value);
    }

    public float getFloat(String key, float defaultValue) {
        String value = properties.getProperty(key);
        return StringUtils.isBlank(value) ? defaultValue : ExpressionUtil.getFloat(value);
    }

    public boolean getBoolean(String key) {
        String value = check(key);
        return Boolean.valueOf(value);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return StringUtils.isBlank(value) ? defaultValue : Boolean.valueOf(value);
    }

    public String getString(String key) {
        return check(key);
    }

    public String getString(String key, String defaultValue) {
        String value = properties.getProperty(key);
        return StringUtils.isBlank(value) ? defaultValue : value;
    }

    private String check(String key) {
        String property = properties.getProperty(key);
        if (property == null) {
            throw new RuntimeException("在" + name + "中找不到配置" + key);
        }
        return property;
    }

    public Map<String, String> getAllConfig() {
        Map<String, String> map = new HashMap<>();
        Set<String> propertyNames = properties.stringPropertyNames();
        for (String property : propertyNames) {
            map.put(property, properties.getProperty(property));
        }

        return map;
    }

    public Properties getProperties() {
        return properties;
    }
}
