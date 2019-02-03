package org.colva.common.helper.cache.redis.boot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 
 * 
 * @description 
 * @author piaoruiqing
 * @date: 2019/02/01 18:43
 *
 * @since JDK 1.8
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RedisHelperConfig.class)
public @interface EnableRedisHelper {
}
