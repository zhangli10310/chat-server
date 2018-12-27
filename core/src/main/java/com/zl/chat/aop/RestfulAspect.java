package com.zl.chat.aop;

import com.zl.chat.entity.BaseEntity;
import com.zl.chat.exception.ControllerException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class RestfulAspect {

    private static final Logger logger = LoggerFactory.getLogger(RestfulAspect.class);

    @Around("execution(public * com.zl.chat.controller.*.*(..))")
    public Object around(ProceedingJoinPoint pjp) {
        BaseEntity result = new BaseEntity();
        Object object = null;
        try {
            object = pjp.proceed();
            result.setCode(0);
            result.setData(object);
        } catch (Throwable throwable) {
            result.setCode(-1);
            logger.info(throwable.getMessage());
            if (throwable instanceof ControllerException) {
                ControllerException exception = (ControllerException) throwable;
                if (exception.getCode() != 0) {
                    result.setCode(exception.getCode());
                }
            }
            result.setMessage(throwable.getMessage());
        }
        return result;
    }

}