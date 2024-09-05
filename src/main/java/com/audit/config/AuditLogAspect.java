package com.audit.config;

import com.audit.annotation.Audited;
import com.audit.constant.AppConstant;
import com.audit.dto.AuditLog;
import com.audit.entity.AuditLogEntity;
import com.audit.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static com.audit.constant.AppConstant.HEADER_DEVICE_NAME;

@Aspect
@Component
@ConditionalOnProperty(name = "app.audit.enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;

    @After("@annotation(audited)")
    public void createAuditLogRecord(JoinPoint joinPoint, Audited audited) {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        AuditLog auditLog = new AuditLog();
        audit(audited, joinPoint, methodSignature, httpRequest, auditLog);

        saveAuditLog(auditLog);
    }

    private void saveAuditLog(AuditLog auditLog) {
        AuditLogEntity builder = AuditLogEntity.builder(auditLog);
        auditLogRepository.save(builder);
    }

    private AuditLog audit(Audited audited, JoinPoint joinPoint, MethodSignature method, HttpServletRequest httpRequest, AuditLog auditLog) {
        auditLog.setTimeZone(httpRequest.getHeader(AppConstant.HEADER_ZONE_ID));
        auditLog.setChannel(httpRequest.getHeader(AppConstant.HEADER_USER_PLATFORM));
        auditLog.setIpAddress(httpRequest.getRemoteAddr());
        auditLog.setDevice(httpRequest.getHeader(AppConstant.HEADER_DEVICE_NAME));
        auditLog.setOperatingSystem(httpRequest.getHeader(AppConstant.HEADER_OPERATING_SYSTEM));
        auditLog.setBrowser(httpRequest.getHeader(AppConstant.HEADER_BROWSER));
        auditLog.setActivity(method.getName());

        String idToken = httpRequest.getHeader(AppConstant.HEADER_ID_TOKEN);
        try {
            Map<String, Object> argumentsMap = prepareArgumentsMap(joinPoint, audited.fieldsToAudit(), audited.index());

            prepareAudit(audited, httpRequest, argumentsMap, auditLog);

        } catch (Exception e) {
            log.warn("Wrong index {} provided for input params for method {}, exception : {}", audited.index(), method.getMethod(), e.getMessage());
        }
        return auditLog;
    }

    private Map<String, Object> prepareArgumentsMap(JoinPoint joinPoint, String[] params, int auditIndex) {
        Map<String, Object> argumentMap = new HashMap<>();
        Object paramValue = null;

        try {
            paramValue = joinPoint.getArgs()[auditIndex];
        } catch (ArrayIndexOutOfBoundsException e) {
            log.error("Audit parameter not found for index: {}", auditIndex);
        }

        if (paramValue != null) {
            for (String param : params) {
                String[] splitValues = param.split("\\.");
                if (splitValues.length == 1) {
                    var pair = getFieldValue(paramValue, splitValues[0]);
                    argumentMap.put(pair.a, pair.b);
                } else if (splitValues.length > 1) {
                    Pair<String, Object> nestedPair = null;
                    for (String split : splitValues) {
                        if (nestedPair != null) {
                            nestedPair = getFieldValue(nestedPair.b, split);
                        } else nestedPair = getFieldValue(paramValue, split);
                    }
                    /*var pair = getFieldValue(paramValue, splitValues[0]);
                    Pair<String, Object> nestedPair = null;
                    if (pair.b != null) {
                        nestedPair = getFieldValue(pair.b, splitValues[1]);
                    }*/
                    argumentMap.put(nestedPair.a, nestedPair.b); // getSecond() should be implemented in Pair
                } else {
                    log.debug("Invalid audited parameter defined");
                }
            }
        }
        return argumentMap;
    }

    public static Pair<String, Object> getFieldValue(Object obj, String name) {
        String[] split = name.split("#");

        try {
            if (split.length == 1) {
                // Handle field access
                Field field = obj.getClass().getDeclaredField(name);
                field.setAccessible(true);
                return new Pair(name, field.get(obj));
            } else if (split.length == 2) {
                // Handle method invocation
                Method method = obj.getClass().getDeclaredMethod(split[1]);
                return new Pair(split[0], method.invoke(obj));
            } else {
                return new Pair(name, null);
            }
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();  // Handle exceptions as needed
            return new Pair(name, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AuditLog prepareAudit(Audited audited, HttpServletRequest httpRequest, Map<String, Object> argumentsMap, AuditLog optionalAuditLog) {
        AuditLog auditLog = (optionalAuditLog != null) ? optionalAuditLog : new AuditLog();
        StringJoiner fields = new StringJoiner(System.lineSeparator(), auditLog.getOtherFields() != null ? auditLog.getOtherFields() : "", System.lineSeparator());

        for (String field : audited.fieldsToAudit()) {
            String[] parts = field.split("\\.");
            String key = parts[parts.length - 1];
            String objValue = argumentsMap.containsKey(key) ? argumentsMap.get(key).toString() : null;

            switch (key) {
                case "customerId":
                    if(auditLog.getCustomerId() == null){
                        assert objValue != null;
                        auditLog.setCustomerId(Long.valueOf(objValue));
                    }
                    break;
                case "phoneNumber":
                    String channel = httpRequest.getHeader(AppConstant.HEADER_REQUEST_CHANNEL);
                    if (AppConstant.MOMO_LOAN_CHANNEL.equals(channel) || AppConstant.WHATSAPP_CHANNEL.equals(channel)) {
                        auditLog.setPhoneNumber(objValue);
                    }
                    break;
                case "customerNumber":
                    auditLog.setCustomerNumber(objValue);
                    break;
                case "name":
                    auditLog.setUsername(objValue);
                    break;
                default:
                    fields.add(key + " : " + objValue);
                    break;
            }
        }

        auditLog.setOtherFields(audited.shouldStoreAll() ? fields.toString() : auditLog.getOtherFields());
        return auditLog;
    }
}
