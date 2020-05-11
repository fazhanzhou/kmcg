package com.greentech.kmcg.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Method;
import java.util.Calendar;

/**
 * @author: 周发展
 * @Date: 2020/5/9 14:22
 * @Description:拦截所有请求，做时间判断
 */
@Slf4j
@Component
@Aspect
public class Aop {
    /**
     * 定义切点，排除nosatrt方法，不然会死循环
     */
    @Pointcut("execution(* com.greentech.kmcg.controller.*.*(..)) &&  !execution(* com.greentech.kmcg.controller.ResultController.noStart(..))")
    public void limit() {
    }

    @Around("limit()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        log.debug("around");
        //类型转换，向下转型，必定成功，因为其内部的实现MethodSignatureImpl实现的就是MethodSignature接口
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Class returnClass = method.getReturnType();
        //检查时间是否到了
        if (checkTime()) {
            Object o = pjp.proceed();
            return o;
        } else {
            Object object = returnClass.newInstance();
            if (object instanceof JSONObject) {
                ((JSONObject) object).put("code", 0);
                ((JSONObject) object).put("msg", "活动还未开始");
                return object;
            } else if (object instanceof ModelAndView) {
                ModelAndView modelAndView = new ModelAndView("redirect:/nostart");
                return modelAndView;
            }
        }
        log.debug(returnClass.getName());
        Object o = pjp.proceed();
        return o;
    }

    /**
     * 检查活动时间
     *
     * @return 时间到了返回true，否则false
     */
    public boolean checkTime() {
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        log.debug(month + "-" + day + "-" + hours);
        return hours > 15;
    }
}