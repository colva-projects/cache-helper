package org.colva.common.helper.cache.redis.test.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * 
 * @description 
 * @author piaoruiqing
 * @date: 2019/02/01 18:46
 *
 * @since JDK 1.8
 */
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SomeParam {

    private String name;
    private Integer type;
}
