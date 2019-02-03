package org.colva.common.helper.cache.redis.boot;

import static org.colva.common.helper.cache.redis.util.Constants.*;
import static org.colva.common.helper.cache.redis.util.Constants.PROTOSTUFF_REDIS_TEMPLATE;
import static org.colva.common.helper.cache.redis.util.Constants.REDIS_TEMPLATE;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.colva.common.helper.cache.redis.aspect.CacheableAspect;
import org.colva.common.helper.cache.redis.model.CacheWrapper;
import org.colva.common.helper.cache.redis.serializer.GenericProtostuff2ByteRedisSerializer;
import org.colva.common.helper.cache.redis.serializer.GenericRedisKeySerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Redis cache helper config
 * 
 * @description
 * @author piaoruiqing
 * @date: 2019/02/01 18:26
 *
 * @since JDK 1.8
 */
@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(RedisHelperProperties.class)
public class RedisHelperConfig implements ImportAware {

    @Value("${spring.application.name:default}")
    private String applicationName;
    @Autowired
    private RedisHelperProperties properties;
    
    /**
     * Jackson RedisTemplate
     * 
     * @author piaoruiqing
     * @date: 2019/01/20 13:49
     * 
     * @param factory
     * @return
     */
    @Bean(REDIS_TEMPLATE)
    @ConditionalOnMissingBean(name = REDIS_TEMPLATE)
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory factory) {

        RedisTemplate<?, ?> template = new RedisTemplate<>();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new GenericRedisKeySerializer(applicationName));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();

        return template;
    }

    /**
     * Protostuff RedisTemplate
     * 
     * @author piaoruiqing
     * @date: 2019/01/20 13:49
     * 
     * @param redisConnectionFactory
     * @return
     */
    @Bean(PROTOSTUFF_REDIS_TEMPLATE)
    @ConditionalOnMissingBean(name = PROTOSTUFF_REDIS_TEMPLATE)
    public RedisTemplate<?, ?> protostuffRedisTemplate(RedisConnectionFactory factory) {

        RedisTemplate<?, ?> template = new RedisTemplate<>();
        GenericProtostuff2ByteRedisSerializer protostuff2ByteRedisSerializer = new GenericProtostuff2ByteRedisSerializer();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new GenericRedisKeySerializer(applicationName));
        template.setValueSerializer(protostuff2ByteRedisSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(protostuff2ByteRedisSerializer);
        template.afterPropertiesSet();

        return template;
    }

    /**
     * String RedisTemplate
     * 
     * @author piaoruiqing
     * @date: 2019/01/20 13:50
     * 
     * @param factory
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {

        StringRedisTemplate template = new StringRedisTemplate();
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new GenericRedisKeySerializer(applicationName));
        template.setValueSerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);
        template.afterPropertiesSet();

        return template;
    }

    /**
     * 缓存
     * 
     * @author piaoruiqing
     * @date: 2019/01/19 21:11
     * 
     * @return
     */
    @Bean
    public CacheableAspect redisCacheableAspect() {

        return new CacheableAspect();
    }

    /**
     * 线程池
     * 
     * @author piaoruiqing
     * @date: 2019/02/03 10:18
     * 
     * @return
     */
    @Bean(CACHE_HELPER_EXECUTOR_SERVICE)
    @ConditionalOnMissingBean(name = CACHE_HELPER_EXECUTOR_SERVICE)
    public ExecutorService executorService() {
        
        return new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors() * 2, 30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1024));
    }

    /**
     * 内存缓存
     * 
     * @author piaoruiqing
     * @date: 2019/02/03 10:27
     * 
     * @return
     */
    @Bean(CACHE_HELPER_LOADING_CACHE)
    @ConditionalOnMissingBean(name = CACHE_HELPER_LOADING_CACHE)
    public LoadingCache<String, CacheWrapper> loadingCache() {
        
        return CacheBuilder.newBuilder()
            .maximumSize(properties.getLoadingCacheMaximumSize())
            .concurrencyLevel(properties.getLoadingCacheConcurrencyLevel())
            .expireAfterWrite(properties.getLoadingCacheExpireAfterWrite(), TimeUnit.MILLISECONDS)
            .build(new CacheLoader<String, CacheWrapper>() {
                @Override
                public CacheWrapper load(String key) {
                    return CacheWrapper.EMPTY;
                }
            });
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {

    }

}
