package org.colva.common.helper.cache.redis.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * 配置
 * 
 * @description 
 * @author piaoruiqing
 * @date: 2019/02/03 10:29
 *
 * @since JDK 1.8
 */
@Setter
@Getter
@ConfigurationProperties(prefix = RedisHelperProperties.REDIS_CACHE_HELPER_PREFIX)
public class RedisHelperProperties {

    public static final String REDIS_CACHE_HELPER_PREFIX = "colva.cache-helper.redis";
    /** JVM缓存数量 */
    long loadingCacheMaximumSize = 1024;
    /** JVM缓存允许并发更新数量 */
    int loadingCacheConcurrencyLevel = 16;
    /** JVM缓存过期时间(单位: ms) */
    long loadingCacheExpireAfterWrite = 500;
    
    
}
