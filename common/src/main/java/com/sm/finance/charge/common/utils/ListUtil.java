package com.sm.finance.charge.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author shifeng.luo
 * @version created on 2017/10/24 下午10:53
 */
public class ListUtil {

    public static <T> List<T> toList(Map<?, T> map) {
        if (map == null || map.size() == 0) {
            return new ArrayList<>(0);
        }

        List<T> result = new ArrayList<>(map.size());
        result.addAll(map.values());
        return result;
    }
}
