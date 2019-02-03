package org.colva.common.helper.cache.redis.serializer;

import static org.colva.common.helper.cache.redis.util.Constants.*;

import java.nio.charset.Charset;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;

/**
 * generic redis key serializer
 * 
 * @description 
 * @author piaoruiqing
 * @date: 2019/01/18 10:53
 *
 * @since JDK 1.8
 */
public class GenericRedisKeySerializer implements RedisSerializer<Object> {

    private final Charset charset;
    private String prefix;
    private int index;
    
    public GenericRedisKeySerializer(String prefix) {
        
        this(prefix, Charset.forName("UTF8"));
    }

    public GenericRedisKeySerializer(String prefix, Charset charset) {
        
        Assert.notNull(charset, "charset must not null");
        Assert.notNull(prefix, "prefix must not null");
        this.charset = charset;
        this.prefix = prefix + COLON;
        index = this.prefix.length();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.serializer.RedisSerializer#deserialize(byte[])
     */
    @Override
    public String deserialize(byte[] bytes) {
        
        if (null == bytes) {
            return null;
        }
        String key = new String(bytes, charset);
        if (key.indexOf(prefix) == 0) {
            return key.substring(index, key.length());
        }
        return key;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.serializer.RedisSerializer#serialize(java.lang.Object)
     */
    @Override
    public byte[] serialize(Object key) {
        
        if (null == key) {
            return null;
        }
        String string = key.toString();
        if (!string.startsWith(prefix)) {
            string = prefix + string;
        }
        return string.getBytes();
    }
    
}
