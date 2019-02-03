package org.colva.common.helper.cache.redis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 缓存包装类
 * 
 * @description 
 * @author piaoruiqing
 * @date: 2019/02/03 10:20
 *
 * @since JDK 1.8
 */
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CacheWrapper {

    private Object data;
    private long gmtLoad;

    public static final CacheWrapper EMPTY = new CacheWrapper() {
        @Override
        public final void setData(Object data) {}
        @Override
        public final void setGmtLoad(long gmtLoad) {}
    };
}
