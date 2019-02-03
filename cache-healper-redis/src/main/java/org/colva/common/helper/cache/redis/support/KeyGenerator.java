package org.colva.common.helper.cache.redis.support;

import static org.colva.common.helper.cache.redis.util.Constants.COLON;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.colva.common.helper.cache.redis.aspect.CacheableAspect;
import org.colva.common.helper.cache.redis.util.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 通用缓存key生成器<br/>
 * 将参数转为StringBuilder, 支持SpEL表达式取值
 * 
 * @see {@link org.colva.common.helper.cache.redis.aspect.CacheableAspect}
 * 
 * @description 
 * @author piaoruiqing
 * @date: 2019/01/14 11:23
 *
 * @since JDK 1.8
 */
public interface KeyGenerator {
    
    ExpressionParser PARSER = new SpelExpressionParser();
    
    ThreadLocal<EvaluationContext> THREAD_LOCAL = new ThreadLocal<EvaluationContext>() {
        @Override
        protected EvaluationContext initialValue() {
            return new StandardEvaluationContext();
        }
    };
    
    /**
     * 缓存key
     * 
     * @author piaoruiqing
     * @date: 2019/01/25 14:27
     * 
     * @param joinPoint
     * @param prefix    key前缀
     * @param argNames  null 或 空数组时 将使用全部参数
     * @param argsAssociated    key中是否包含参数
     * @return
     * @throws JsonProcessingException
     */
    default StringBuilder generate(ProceedingJoinPoint joinPoint, String prefix, String[] argNames, boolean argsAssociated) throws JsonProcessingException {
        
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(prefix)) {
            builder = builder.append(joinPoint.getTarget().getClass().getName()).append(COLON).append(signature.getName());
        } else {
            builder = builder.append(prefix);
        }
        
        if (!argsAssociated) {
            return builder;
        }
        
        String[] parameterNames = signature.getParameterNames();
        LinkedHashMap<String, Object> argMap = new LinkedHashMap<>(16);
        for (int index = 0 ; index < parameterNames.length ; index++) {
            argMap.put(parameterNames[index], args[index]);
        }
        // argsNames为空时默认为全部参数
        LinkedHashMap<String, Object> chosen;
        if (null == argNames || argNames.length <= 0) {
            chosen = argMap;
        } else {
            chosen = Arrays.stream(argNames).collect(
                Collectors.toMap(
                    k -> k,
                    k -> {
                        String[] expression = StringUtils.split(k, '.');
                        if (expression.length == 1) {
                            return argMap.get(expression[0]);
                        }
                        String argName = expression[0];
                        Object arg = argMap.get(argName);
                        if (null == arg) {
                            return arg;
                        }
                        EvaluationContext context = THREAD_LOCAL.get();
                        context.setVariable(argName, arg);
                        return PARSER.parseExpression("#" + k).getValue(context);
                    }, 
                    (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); }, LinkedHashMap::new)
            );
            THREAD_LOCAL.remove();
        }
        return builder.append(COLON).append(StringUtils.simpleJoinToBuilder(chosen, "=", "|"));
    }

}
