package com.vein.transport.api;

/**
 * @author shifeng.luo
 * @version created on 2017/9/11 下午2:09
 */
public interface Transport {

    TransportClient client();

    TransportServer server();

}
