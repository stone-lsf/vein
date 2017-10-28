package com.sm.charge.cluster.group;

import com.sm.charge.cluster.BaseObject;

/**
 * @author shifeng.luo
 * @version created on 2017/10/28 下午10:39
 */
public interface GroupService {

    <T> boolean receive(BaseObject<T> obj);
}
