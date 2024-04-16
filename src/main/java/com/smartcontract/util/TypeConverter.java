package com.smartcontract.util;

import java.util.ArrayList;
import java.util.List;

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
}
