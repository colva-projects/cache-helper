package org.colva.common.helper.cache.redis.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * string工具类<br/>
 * 
 * @description 
 * @author piaoruiqing
 * @date: 2019/01/09 10:57
 *
 * @since JDK 1.8
 */
abstract public class StringUtils extends org.apache.commons.lang3.StringUtils {
    
    private final static char[] DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 
        'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    
    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setSerializationInclusion(Include.NON_NULL);
    }

    /**
     * 数组转字符串
     * 
     * @author piaoruiqing
     * @date: 2019/01/09 14:14
     * 
     * @param array 待转换数组
     * @param separator 分隔符
     * @return
     * @throws JsonProcessingException
     */
    public static String simpleJoin(Object[] array, String separator) throws JsonProcessingException {
        if (array == null) {
            return null;
        }

        return simpleJoin(array, separator, 0, array.length);
    }
    
    /**
     * 数组转字符串
     * 
     * @author piaoruiqing
     * @date: 2019/01/09 14:14
     * 
     * @param array 待转换数组
     * @param separator 分隔符
     * @param startIndex    开始位置
     * @param endIndex  结束位置
     * @return
     * @throws JsonProcessingException
     */
    public static String simpleJoin(Object[] array, String separator, int startIndex, int endIndex) throws JsonProcessingException {
        if (array == null) {
            return null;
        }
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return EMPTY;
        }

        return simpleJoinToBuilder(array, separator, startIndex, endIndex).toString();
    }
    
    /**
     * 数组转StringBuilder
     * 
     * @author piaoruiqing
     * @date: 2019/01/09 14:14
     * 
     * @param array 待转换数组
     * @param separator 分隔符
     * @return
     * @throws JsonProcessingException
     */
    public static StringBuilder simpleJoinToBuilder(Object[] array, String separator) throws JsonProcessingException {
        if (array == null) {
            return null;
        }

        return simpleJoinToBuilder(array, separator, 0, array.length);
    }
    
    /**
     * Map转StringBuilder
     * 
     * @author piaoruiqing
     * @date: 2019/01/14 09:48
     * 
     * @param map   待转换map
     * @param separatorKV key与value间分隔符
     * @param separator Entry间分隔符
     * @return
     * @throws JsonProcessingException
     */
    public static <K, V> StringBuilder simpleJoinToBuilder(LinkedHashMap<K, V> map, String separatorKV, String separator) throws JsonProcessingException {
        if (map == null) {
            return null;
        }
        if (map.size() <= 0) {
            return new StringBuilder(0);
        }
        Entry<K, V> next = map.entrySet().iterator().next();
        int bufSize =
            map.size() * (next.getKey().toString().length() + Optional.ofNullable(next.getValue()).map(String::valueOf).map(String::length).orElse(4) + 2);
        StringBuilder builder = new StringBuilder(bufSize);
        
        int index = 0;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            if (index > 0) {
                builder.append(separator);
            }
            appendObject(builder, key, separatorKV, value);
            index++;
        }
        
        return builder;
    }
    
    /**
     * 数组转StringBuilder
     * 
     * @author piaoruiqing
     * @date: 2019/01/09 14:14
     * 
     * @param array 待转换数组
     * @param separator 分隔符
     * @param startIndex    开始位置
     * @param endIndex  结束位置
     * @return
     * @throws JsonProcessingException
     */
    public static StringBuilder simpleJoinToBuilder(Object[] array, String separator, int startIndex, int endIndex) throws JsonProcessingException {
        if (array == null) {
            return null;
        }
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return new StringBuilder(0);
        }

        bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length()) + 1);
        StringBuilder builder = new StringBuilder(bufSize);

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                builder.append(separator);
            }
            appendObject(builder, array[i]);
        }
        return builder;
    }
    
    /**
     * 追加字符串
     * 
     * @author piaoruiqing
     * @date: 2019/01/14 09:59
     * 
     * @param builder   待追加StringBuilder
     * @param object    追加参数
     * @return
     * @throws JsonProcessingException
     */
    public static StringBuilder appendObject(StringBuilder builder, Object... object) throws JsonProcessingException {
        
        for (Object item : object) {
            if (item instanceof Number || item instanceof String || item instanceof Boolean || item instanceof Character) {
                builder.append(item);
            } else {
                builder.append(OBJECT_MAPPER.writeValueAsString(item));
            }
        }
        return builder;
    }
    
    /**
     * 指定长度随机字串
     * 
     * @author piaoruiqing
     * @date: 2019/01/25 15:52
     * 
     * @param length
     * @return
     */
    public static String random(int length) {
        
        char[] cs = new char[length];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = DIGITS[ThreadLocalRandom.current().nextInt(DIGITS.length)];
        }
        return new String(cs);
    }
    
}
