package org.colva.common.helper.cache.redis.util;

/**
 * 常量
 * 
 * @description 
 * @author piaoruiqing
 * @date: 2019/01/19 23:53
 *
 * @since JDK 1.8
 */
public interface Constants {
    
    /** BeanName, 使用Jackson序列化的redisTemplate */
    public static final String REDIS_TEMPLATE = "redisTemplate";
    /** BeanName, 使用Protostuff序列化的redisTemplate */
    public static final String PROTOSTUFF_REDIS_TEMPLATE = "cacheHelper.protostuffRedisTemplate";
    /** 线程池 */
    public static final String CACHE_HELPER_EXECUTOR_SERVICE = "cacheHelper.executorService";
    public static final String CACHE_HELPER_LOADING_CACHE = "cacheHelper.loadingCache";
    
    /** default source */
    String DEFAULT_SOURCE = "default";
    /** separator of key */
    String COLON = ":";
    /** nxxx */
    /** Only set the key if it does not already exist */
    String NX = "NX";
    /** Only set the key if it already exist */
    String XX = "XX";
    /** expx */
    /** seconds -- Set the specified expire time, in seconds */
    String EX = "EX";
    /** milliseconds -- Set the specified expire time, in milliseconds */
    String PX = "PX";
    /** if SET was executed correctly */
    String OK = "OK";
    String NIL = "nil";

}
