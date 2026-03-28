package com.demo.aop;


import com.demo.constants.HeaderConstants;
import com.demo.exceptions.InvalidHeaderException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;


@Aspect
@Component
@Slf4j
public class HeaderValidationAspect {

    @Around("@annotation(com.demo.annotation.ValidateEnvHeader)")
    public Object validateHeaders(ProceedingJoinPoint joinPoint) throws Throwable {


        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            log.warn("HeaderValidationAspect: no RequestAttributes found — skipping validation");
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();


        MethodSignature signature  = (MethodSignature) joinPoint.getSignature();
        Method          method     = signature.getMethod();

        String env = request.getHeader(HeaderConstants.ENV_HEADER);

        if (env == null || env.isBlank()) {
            log.error("HeaderValidationAspect: '{}' header is missing or blank", HeaderConstants.ENV_HEADER);
            throw new InvalidHeaderException(
                    HeaderConstants.ENV_HEADER,
                    env,
                    "Required header '" + HeaderConstants.ENV_HEADER + "' is missing or blank"
            );
        }


        if (!HeaderConstants.ALLOWED_ENVIRONMENTS.contains(env.toLowerCase())) {
            log.error("HeaderValidationAspect: invalid env='{}'. Allowed values: {}",
                    env, HeaderConstants.ALLOWED_ENVIRONMENTS);
            throw new InvalidHeaderException(
                    HeaderConstants.ENV_HEADER,
                    env,
                    "Invalid value '" + env + "' for header '" + HeaderConstants.ENV_HEADER

            );
        }



        log.info("HeaderValidationAspect: headers valid — env={}, method={}",
                env, method.getName());

        return joinPoint.proceed();
    }
}

