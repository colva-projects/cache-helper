package org.colva.common.helper.cache.redis.test;


import static org.colva.common.helper.cache.redis.util.Constants.PROTOSTUFF_REDIS_TEMPLATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.colva.common.helper.cache.redis.annotation.Cacheable;
import org.colva.common.helper.cache.redis.annotation.DistributedLockable;
import org.colva.common.helper.cache.redis.boot.EnableRedisHelper;
import org.colva.common.helper.cache.redis.test.model.SomeParam;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * 单元测试
 * 
 * @description
 * @author piaoruiqing
 * @date: 2019/01/13 22:26
 *
 * @since JDK 1.8
 */
@EnableRedisHelper
@SpringBootApplication
@RunWith(SpringRunner.class)
@SpringBootTest
public class CacheSupportTest {
    
    private static final ExecutorService POOL = Executors.newFixedThreadPool(32);

    @Autowired
    private CacheSupportService cacheSupportService;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    @Qualifier(PROTOSTUFF_REDIS_TEMPLATE)
    private RedisTemplate<Object, Object> protostuffRedisTemplate;

    /**
     * 测试 `@Cacheable` 缓存
     * 
     * @author piaoruiqing
     * @date: 2019/01/25 17:07
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void testCacheable() throws InterruptedException, ExecutionException {

        Set<Long> set = new HashSet<>();
        
        List<Future<Long>> results = new ArrayList<>();
        for (int index = 0 ; index < 100 ; index++) {
            final Integer param2 = index;
            results.add(POOL.submit(() -> {
                    return cacheSupportService.cacheable(new SomeParam("1", 2), "1", param2, 3);
                }));
        }
        
        for (Future<Long> future : results) {
            set.add(future.get());
        }
        
        assertTrue(set.size() == 1);

    }
    
    /**
     * 测试 `@Cacheable` 缓存预加载
     * <pre>
     * 缓存过期剩余时间小于preloading, 若有访问则触发预加载.
     * 预加载异步执行, 触发预加载当次访问的返回值为尚未过期的缓存.
     * 预加载完成前多次触发只加载一次.
     * </pre>
     * 
     * @author piaoruiqing
     * @date: 2019/01/28 15:24
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void testCacheablePreloading() throws InterruptedException, ExecutionException {

        Long result1 = cacheSupportService.cacheablePreloading("0", 2, 4);
        TimeUnit.SECONDS.sleep(4);                                         // 4s
        Long result2 = cacheSupportService.cacheablePreloading("1", 2, 3); // < 1s
        TimeUnit.SECONDS.sleep(4);                                         // 4s

        // preloading
        POOL.execute(() -> {
            cacheSupportService.cacheablePreloading("1", 2, 3);
        });
        POOL.execute(() -> {
            cacheSupportService.cacheablePreloading("1", 2, 3);
        });
        POOL.execute(() -> {
            cacheSupportService.cacheablePreloading("1", 2, 3);
        });
        
        TimeUnit.SECONDS.sleep(5);
        Long result3 = cacheSupportService.cacheablePreloading("1", 2, 3);
        TimeUnit.SECONDS.sleep(1);
        Long result4 = cacheSupportService.cacheablePreloading("1", 2, 3);
        
        assertEquals(result1, result2);
        assertNotEquals(result1, result3);
        assertEquals(result3, result4);
        
    }
    
    /**
     * 测试 `@DistributedLockable` 分布式锁
     * 
     * @author piaoruiqing
     * @date: 2019/01/25 17:08
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testDistributedLockable() throws InterruptedException, ExecutionException {

        List<Future<?>> results = new ArrayList<>();
        for (int index = 0 ; index < 100 ; index++) {
            final Integer param2 = index;
            results.add(POOL.submit(() -> {
                    return cacheSupportService.distributedLockable(new SomeParam("2", 0), "1", param2, 3);
                }));
        }
        
        int countNotNull = 0;
        for (Future<?> future : results) {
            if (null != future.get()) {
                countNotNull++ ;
            }
        }

        assertTrue(countNotNull == 1);
    }

    /**
     * Jackson序列化的RedisTemplate
     * 
     * @author piaoruiqing
     * @date: 2019/01/25 17:08
     *
     */
    @Test
    public void testRedisTemplate() {

        SomeParam someParam = SomeParam.builder().name("1").type(1).build();
        redisTemplate.opsForValue().set("cache-support-value", someParam);
        redisTemplate.opsForHash().put("cache-support-hash", "hash-key", someParam);

        someParam = (SomeParam)redisTemplate.opsForValue().get("cache-support-value");
        assertNotNull(someParam);
        someParam = (SomeParam)redisTemplate.opsForHash().get("cache-support-hash", "hash-key");
        assertNotNull(someParam);

        redisTemplate.delete("cache-support-value");
        redisTemplate.delete("cache-support-hash");
    }
    
    /**
     * Protostuff序列化的RedisTemplate
     * 
     * @author piaoruiqing
     * @date: 2019/01/25 17:08
     *
     */
    @Test
    public void testProtostuffRedisTemplate() {
        
        SomeParam someParam = SomeParam.builder().name("1").type(1).build();
        protostuffRedisTemplate.opsForValue().set("cache-support-value", someParam);
        protostuffRedisTemplate.opsForHash().put("cache-support-hash", "hash-key", someParam);
        
        someParam = (SomeParam)protostuffRedisTemplate.opsForValue().get("cache-support-value");
        assertNotNull(someParam);
        someParam = (SomeParam)protostuffRedisTemplate.opsForHash().get("cache-support-hash", "hash-key");
        assertNotNull(someParam);
        
        protostuffRedisTemplate.delete("cache-support-value");
        protostuffRedisTemplate.delete("cache-support-hash");
    }
    
    @Service
    public static class CacheSupportService {

        /**
         * `@Cacheable` 测试
         * 
         * @author piaoruiqing
         * @date: 2019/01/20 13:23
         * 
         * @param userParam
         * @param param1
         * @param param2
         * @param param3
         * @return
         */
        @Cacheable(
            // 可选择关注的参数, 推荐使用简单参数
            argNames = {"someParam.name", "someParam.type", "param3"}, 
            // 自定义key前缀, key的组成: appName + prefix + args
            prefix = "cache-support.cacheable", timeout = 30, timeUnit = TimeUnit.SECONDS,
            // 超时时间, 从environment取值, 优先级高于 timeout
            timeoutConfigKey = "cache-support.config.timeout.cacheable",
            // 同步加载
            blocking = true
        )
        public Long cacheable(SomeParam someParam, String param1, Integer param2, Object param3){
            
            try {
                TimeUnit.SECONDS.sleep(5);
                System.err.println("cacheable load: " + System.nanoTime());
            } catch (InterruptedException e) {
            }
            
            return System.nanoTime();
        }
        
        /**
         * `@Cacheable` 测试
         * 
         * @author piaoruiqing
         * @date: 2019/01/20 13:23
         * 
         * @param userParam
         * @param param1
         * @param param2
         * @param param3
         * @return
         */
        @Cacheable(
            // 可选择关注的参数, 推荐使用简单参数
            argNames = {"param2"}, 
            // 自定义key前缀, key的组成: appName + prefix + args
            prefix = "cache-support.cacheable-preloading", timeout = 10, timeUnit = TimeUnit.SECONDS,
            // 同步加载
            blocking = true,
            // 预加载
            preloading = 3
        )
        public Long cacheablePreloading(String param1, Integer param2, Object param3){
            
            try {
                TimeUnit.SECONDS.sleep(1);
                System.err.println("cacheable-preloading load: " + System.nanoTime());
            } catch (InterruptedException e) {
            }
            
            return System.nanoTime();
        }
        
        /**
         * `@DistributedLockable` 测试
         * 
         * @author piaoruiqing
         * @date: 2019/01/20 13:27
         * 
         * @param userParam
         * @param param1
         * @param param2
         * @param param3
         * @return
         */
        @DistributedLockable(
            // 可选择关注的参数, 推荐使用简单参数
            argNames = {"someParam.name", "someParam.type", "param3"},
            // 超时时间, 单位: s
            timeout = 10
        )
        public Long distributedLockable(SomeParam someParam, String param1, Integer param2, Object param3) {
            
            try {
                TimeUnit.SECONDS.sleep(5);
                System.err.println("distributed-lockable load: " + System.nanoTime());
            } catch (InterruptedException e) {
            }
            
            return System.nanoTime();
        }
    }

}
