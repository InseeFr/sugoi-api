/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.services.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.commons.services.controller.technics.SugoiAdviceController;
import java.lang.reflect.Parameter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Aspect
@Component
public class SugoiLoggerComponent {
  private static final Logger log = LoggerFactory.getLogger(SugoiLoggerComponent.class);

  @Autowired private SugoiAdviceController sugoiAdviceController;
  ObjectMapper objectMapper = new ObjectMapper();

  private static final String POINTCUT = "within(fr.insee.sugoi.services.controller.*)";

  public Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  @Around(POINTCUT)
  public Object logArroundExec(ProceedingJoinPoint pjp) throws Throwable {
    ResponseEntity<?> proceed = (ResponseEntity<?>) pjp.proceed();
    if (log.isInfoEnabled())
      log.info(
          "type={} user={} requestArguments='{}' responseCode='{}'",
          computeRequestType(pjp),
          getAuthentication().getName(),
          computeRequestArguments(pjp),
          proceed.getStatusCode());
    return proceed;
  }

  @AfterThrowing(pointcut = POINTCUT, throwing = "e")
  public void logAfterException(JoinPoint jp, Exception e) {
    ResponseEntity<?> error = sugoiAdviceController.exception(e);
    if (log.isInfoEnabled())
      log.info(
          "type={} user={} requestArguments='{}' responseCode='{}' exception='{}'",
          computeRequestType(jp) + "_ERROR",
          getAuthentication().getName(),
          computeRequestArguments(jp),
          error.getStatusCode(),
          e.toString());
  }

  private String computeRequestType(JoinPoint jp) {
    return ((MethodSignature) jp.getSignature())
        .getMethod()
        .getName()
        .replaceAll("([a-z])([A-Z])", "$1_$2")
        .toUpperCase();
  }

  private String computeRequestArguments(JoinPoint jp) {
    try {
      Parameter[] parameters = ((MethodSignature) jp.getSignature()).getMethod().getParameters();
      var mapOfParamNameParamValue =
          IntStream.range(0, parameters.length)
              .boxed()
              .map(i -> new ImmutablePair<>(i, extractNameOfParameter(parameters[i])))
              .filter(p -> p.getValue() != null)
              .collect(
                  Collectors.toMap(
                      Pair::getValue,
                      p -> jp.getArgs()[p.getKey()] != null ? jp.getArgs()[p.getKey()] : "null"));
      return objectMapper.writeValueAsString(mapOfParamNameParamValue);
    } catch (JsonProcessingException e) {
      log.error("Logging failed during argument creation with exception {}", e.getMessage());
      return "{\"error\": \"error while parsing data\"}";
    }
  }

  private String extractNameOfParameter(Parameter parameter) {
    if (parameter.isAnnotationPresent(PathVariable.class)) {
      return !parameter.getAnnotation(PathVariable.class).value().isEmpty()
          ? parameter.getAnnotation(PathVariable.class).value()
          : parameter.getAnnotation(PathVariable.class).name();
    }
    if (parameter.isAnnotationPresent(RequestParam.class)) {
      return !parameter.getAnnotation(RequestParam.class).value().isEmpty()
          ? parameter.getAnnotation(RequestParam.class).value()
          : parameter.getAnnotation(RequestParam.class).name();
    }
    return null;
  }
}
