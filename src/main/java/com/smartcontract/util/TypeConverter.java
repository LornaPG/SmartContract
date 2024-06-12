package com.smartcontract.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeConverter {
    /**
     * @param obj 需要转换的对象
     * @param tClass 需要转换后的类型
     * @param <T> 泛型
     * @return 返回转换后的结果
     */
    public static <T> List<T> objToList(Object obj, Class<T> tClass) {
        List<T> list = new ArrayList<>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                list.add(tClass.cast(o));
            }
        }
        return list;
    }

    public static <K, V> Map<K, V> objToMap(Object obj, Class<K> kClass, Class<V> vClass) {
        Map<K, V> map = new HashMap<>();
        if (obj instanceof Map<?, ?>) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                map.put(kClass.cast(entry.getKey()), vClass.cast(entry.getValue()));
            }
        }
        return map;
    }
}
