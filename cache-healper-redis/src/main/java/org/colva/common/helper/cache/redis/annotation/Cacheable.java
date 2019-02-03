package org.colva.common.helper.cache.redis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 方法缓存
 * 
 * @see {@link org.colva.common.helper.cache.redis.aspect.CacheableAspect}
 * 
 * @author piaoruiqing
 * @date 2018/09/17 15:41:03
 * @version
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cacheable {
	
    /**
     * 超时时间 <br/>
     * 单位: ms (默认)
     * 
     * @see #timeoutConfigKey()
     * @see #timeUnit()
     * 
     * @author piaoruiqing
     * @date: 2019/01/08 13:48
     * 
     * @return
     */
	long timeout() default 10000L;
	
	/**
	 * 时间单位
	 * @author piaoruiqing
	 * @date: 2019/01/08 13:48
	 * 
	 * @return
	 */
	TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
	
	/**
	 * 缓存超时时间配置KEY<br/>
	 * 优先级高于{@link #timeout()}
	 * 
	 * @see #timeout()
	 * @see #timeUnit()
	 * 
	 * @author piaoruiqing
	 * @date: 2019/01/08 13:48
	 * 
	 * @return
	 */
    String timeoutConfigKey() default "";
    
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
     * 设置为false时, 相同方法不同参数也为同一缓存<br/>
     * 建议: <br/>
     * 1. 参数尽量简单, 少用复杂对象<br/>
     * 2. 减少使用变化频繁的参数(如当前时间戳)
     * 
     * @author piaoruiqing
     * @date: 2019/01/09 10:55
     * 
     * @return
     */
    boolean argsAssociated() default true;
    
    /**
     * 阻塞加载
     * 
     * @author piaoruiqing
     * @date: 2019/01/09 09:32
     * 
     * @return
     */
    boolean blocking() default false;
    
    /**
     * 预加载时间
     * <pre>
     * 缓存过期剩余时间小于preloading, 若有访问则触发预加载.
     * 预加载异步执行, 触发预加载当次访问的返回值为尚未过期的缓存.
     * 预加载完成前多次触发只加载一次.
     * </pre>
     * 
     * @see {@link #timeUnit()}
     * 
     * @author piaoruiqing
     * @date: 2019/01/28 09:10
     * 
     * @return
     */
    long preloading() default -1L;
	
}
