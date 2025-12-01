package com.phyo.food_management_system.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Aspect
@Component
@Slf4j
public class LoggerAspect {

    // Target only service, controller, and repository layers
    @Pointcut("within(com.phyo.food_management_system.controller..*) || " +
            "within(com.phyo.food_management_system.service..*) || " +
            "within(com.phyo.food_management_system.repository..*)")
    public void applicationPackagePointcut() {}

    @Around("applicationPackagePointcut()")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("{} method execution start", joinPoint.getSignature());
        Instant start = Instant.now();
        Object returnObj = joinPoint.proceed();
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        log.info("Time took to execute {} method is: {}", joinPoint.getSignature(), timeElapsed);
        log.info("{} method execution end", joinPoint.getSignature());
        return returnObj;
    }

    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Exception ex) {
        log.error("{} An exception happened due to: {}", joinPoint.getSignature(), ex.getMessage());
    }
}
