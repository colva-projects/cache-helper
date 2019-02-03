package org.colva.common.helper.cache.redis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁
 * 
 * @description 
 * @author piaoruiqing
 * @date: 2019/01/07 15:54
 *
 * @since JDK 1.8
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DistributedLockable {

    /**
     * 超时时间 <br/>
     * 单位: s
     * @author piaoruiqing
     * @date: 2019/01/07 16:13
     * 
     * @return
     */
    long timeout() default 5L;
    
    /**
     * 重试次数
     * @author piaoruiqing
     * @date: 2019/01/07 16:13
     * 
     * @return
     */
    int retries() default 0;
    
    /**
     * 重试等待时间<br/>
     * 单位: ms
     * @author piaoruiqing
     * @date: 2019/01/07 16:13
     * 
     * @return
     */
    long waitingTime() default 0L;
    
    /**
     * 自定义KEY<br/>
     * 默认使用包路径
     * 
     * @author piaoruiqing
     * @date: 2019/01/09 10:48
     * 
     * @return
     */
    String prefix() default "";
    
    /**
     * 参数列表<br/>
     * 默认全部参数
     * 
     * @author piaoruiqing
     * @date: 2019/01/13 23:16
     * 
     * @return
     */
    String[] argNames() default {};
    
    /**
     * 关注目标方法参数<br/>
     * 设置为false时, 粒度较大, 相同方法不同参数也为同一把锁
     * 
     * @author piaoruiqing
     * @date: 2019/01/09 10:55
     * 
     * @return
     */
    boolean argsAssociated() default true;
    
    /**
     * 自动解锁<br/>
     * 方法执行完毕后自动删除key
     * 
     * @author piaoruiqing
     * @date: 2019/01/25 17:27
     * 
     * @return
     */
    boolean autoUnlock() default true;
    
}
