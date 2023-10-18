package fr.insee.sugoi.services.logging;

import fr.insee.sugoi.commons.services.controller.technics.SugoiAdviceController;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Aspect
@Component
public class SugoiLoggerComponent {
    private static final Logger log = LoggerFactory.getLogger(SugoiLoggerComponent.class);
    private SugoiAdviceController sugoiAdviceController;
    @Autowired
    public void setSugoiAdviceController(SugoiAdviceController sugoiAdviceController) {
        this.sugoiAdviceController = sugoiAdviceController;
    }

    private static final String POINTCUT = "within(fr.insee.sugoi.services.controller.*)";
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    @Around(POINTCUT)
    public Object logArroundExec(ProceedingJoinPoint pjp) throws Throwable {
        ResponseEntity<?> proceed = (ResponseEntity<?>) pjp.proceed();

        log.info("{user : {}, request : {}, response : {}}",getAuthentication().getName(),constructLogMsg(pjp), proceed.getStatusCode().value());
        return proceed;
    }

    @AfterThrowing(pointcut = POINTCUT, throwing = "e")
    public void logAfterException(JoinPoint jp, Exception e) {
        ResponseEntity<?> error = sugoiAdviceController.exception(e);
        log.error("{user : {}, request : {}, response : {}, exception : {}}", getAuthentication().getName(), constructLogMsg(jp),error.getStatusCode(),  e.toString());
    }

    private String constructLogMsg(JoinPoint jp) {
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        var methodNameSnakeUpperCase = method.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
        var sb = new StringBuilder("{");
        sb.append("'type'");
        sb.append(":");
        sb.append("'"+methodNameSnakeUpperCase+"'");
        sb.append(",");
        sb.append("'arguments':");
        sb.append(computeArgs(jp));
        sb.append("}");
        return sb.toString();
    }

    private String computeArgs(JoinPoint jp){
        var parameters = ((MethodSignature) jp.getSignature()).getMethod().getParameters();
        Map<Object,Object> map = new HashMap<>();
        for (int i=0;i<parameters.length;i++) {
            map.put(parameters[i].getName(),jp.getArgs()[i]);
        }
        return map.keySet().stream().map( key -> "'"+key.toString() + "':'" + map.get(key)+"'").collect(Collectors.joining(",","[","]"));
    }
}
