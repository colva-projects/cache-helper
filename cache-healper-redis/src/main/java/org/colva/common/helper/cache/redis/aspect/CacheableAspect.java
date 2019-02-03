package org.colva.common.helper.cache.redis.aspect;
import static org.colva.common.helper.cache.redis.util.Constants.CACHE_HELPER_EXECUTOR_SERVICE;
import static org.colva.common.helper.cache.redis.util.Constants.CACHE_HELPER_LOADING_CACHE;
import static org.colva.common.helper.cache.redis.util.Constants.PROTOSTUFF_REDIS_TEMPLATE;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.colva.common.helper.cache.redis.annotation.Cacheable;
import org.colva.common.helper.cache.redis.model.CacheWrapper;
import org.colva.common.helper.cache.redis.support.KeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;

import com.google.common.cache.LoadingCache;

import lombok.extern.slf4j.Slf4j;

/**
 * 缓存切面<br/>
 * 
 * @see {@link org.colva.common.helper.cache.redis.annotation.Cacheable}
 * 
 * @description 
 * @author piaoruiqing
 * @date: 2018/10/05 22:58
 *
 * @since JDK 1.8
 */
@Slf4j
@Aspect
public class CacheableAspect implements KeyGenerator {
    
    @Autowired
    @Qualifier(CACHE_HELPER_EXECUTOR_SERVICE)
    private ExecutorService executorService;
    @Autowired
    @Qualifier(CACHE_HELPER_LOADING_CACHE)
    private LoadingCache<String, CacheWrapper> loadingCache;
    @Autowired
    @Qualifier(PROTOSTUFF_REDIS_TEMPLATE)
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private Environment environment;

    /**
     * {@link Cacheable}切面
     * 
     * @author piaoruiqing
     *
     */
    @Pointcut(value = "execution(* *(..)) && @annotation(org.colva.common.helper.cache.redis.annotation.Cacheable)")
    public void cacheable() {}

    @Around(value = "cacheable() && @annotation(cacheable)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, Cacheable cacheable) throws Throwable {

        final String key = this.generate(proceedingJoinPoint, cacheable.prefix(), cacheable.argNames(), cacheable.argsAssociated()).toString();

        CacheWrapper cacheWrapper = loadingCache.get(key);
        if (CacheWrapper.EMPTY != cacheWrapper) {
            return cacheWrapper.getData();
        }

        CacheWrapper cache = (CacheWrapper)redisTemplate.opsForValue().get(key);
        if (null != cache) {
            // 预加载
            if (cacheable.preloading() > 0) {
                this.preload(proceedingJoinPoint, cacheable, key, cache);
            }
            return cache.getData();
        }

        if (!cacheable.blocking()) {
            return this.load(proceedingJoinPoint, cacheable, key).getData();
        }
        synchronized (key.intern()) {
            CacheWrapper wrapper = loadingCache.get(key);
            if (CacheWrapper.EMPTY != wrapper) {
                return wrapper.getData();
            }
            wrapper = (CacheWrapper)redisTemplate.opsForValue().get(key);
            if (null != wrapper) {
                return wrapper.getData();
            }
            wrapper = this.load(proceedingJoinPoint, cacheable, key);
            loadingCache.put(key, wrapper);
            return wrapper.getData();
        }
    }
	
    /**
     * 加载缓存数据
     * 
     * @author piaoruiqing
     * @date: 2019/01/09 11:13
     * 
     * @param proceedingJoinPoint
     * @param cacheable
     * @param key
     * @return
     * @throws Throwable
     */
    private CacheWrapper load(ProceedingJoinPoint proceedingJoinPoint, Cacheable cacheable, String key) throws Throwable {

        long timeout = cacheable.timeout();
        String timeoutConfigKey = cacheable.timeoutConfigKey();
        if (StringUtils.isNotBlank(timeoutConfigKey)) {
            timeout = environment.getProperty(timeoutConfigKey, Long.class, timeout);
        }

        Object result = proceedingJoinPoint.proceed();

        CacheWrapper wrapper = CacheWrapper.builder().data(result).gmtLoad(System.currentTimeMillis()).build();
        redisTemplate.opsForValue().set(key, wrapper, timeout, cacheable.timeUnit());

        return wrapper;
    }

    /**
     * 预加载缓存数据
     * 
     * @author piaoruiqing
     * @date: 2019/01/28 16:42
     * 
     * @param proceedingJoinPoint
     * @param cacheable
     * @param key
     * @param cacheWrapper
     */
    private void preload(ProceedingJoinPoint proceedingJoinPoint, Cacheable cacheable, String key, CacheWrapper old) {

        executorService.execute(() -> {
            if (cacheable.preloading() > cacheable.timeout() / 2) {
                log.warn("illegal preloading config, preloading: {}, timeout: {}", cacheable.preloading(), cacheable.timeout());
                return;
            }
            long timeout = cacheable.timeUnit().toMillis(cacheable.timeout());
            long preloading = cacheable.timeUnit().toMillis(cacheable.preloading());
            boolean preload = 
                null == old || (timeout - (System.currentTimeMillis() - old.getGmtLoad()) <= preloading);
            if (!preload) {
                return;
            }
            try {
                loadingCache.get(key, () -> {
                    try {
                        log.debug("preloading, key: {}", key);
                        return load(proceedingJoinPoint, cacheable, key);
                    } catch (Throwable e) {
                        log.error("cache preloading error, key: {}", key, e);
                    }
                    return null;
                });
            } catch (ExecutionException e) {
                log.error("cache preloading error, key: {}", key, e);
            }
        });
    }

}
