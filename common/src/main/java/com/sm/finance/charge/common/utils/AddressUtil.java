package com.sm.finance.charge.common.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import com.sm.finance.charge.common.Address;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午9:23
 */
public class AddressUtil {

    private static final Logger logger = LoggerFactory.getLogger(AddressUtil.class);

    public static final String COMMA = ",";

    /**
     * 解析地址列表
     *
     * @param addressStr 地址列表字符串
     * @return Address列表
     */
    public static List<Address> parseList(String addressStr) {
        Preconditions.checkState(StringUtils.isNotBlank(addressStr), "地址不能为空");

        String[] addressList = addressStr.split(COMMA);

        List<Address> addresses = Lists.newArrayListWithCapacity(addressList.length);
        for (String address : addressList) {
            addresses.add(parse(address.trim()));
        }
        return addresses;
    }

    /**
     * 解析地址
     *
     * @param addressStr 地址字符串
     * @return {@link Address}
     */
    public static Address parse(String addressStr) {
        String[] hostPort = addressStr.split(":");
        Preconditions.checkState(hostPort.length == 2, "服务器地址错误:" + addressStr);

        String hostname = hostPort[0];
        try {
            InetAddress address = InetAddress.getByName(hostname);
            int port = Integer.parseInt(hostPort[1]);
            return new Address(new InetSocketAddress(address, port));
        } catch (UnknownHostException e) {
            logger.error("illegal address:{}", addressStr);
            throw new IllegalArgumentException("illegal address:" + addressStr);
        }
    }

    /**
     * 获取本地ip
     *
     * @return ip地址
     */
    public static String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.error("get local ip fail!", e);
            throw new IllegalStateException(e);
        }
    }

    public static InetAddress getInetAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            logger.error("get local ip fail!", e);
            throw new IllegalStateException(e);
        }
    }

    public static Address getLocalAddress(int port) {
        return new Address(new InetSocketAddress(getInetAddress(), port));
    }
}
