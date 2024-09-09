package com.audit.dto;

import com.audit.entity.AuditLogEntity;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    private Long customerId;
    private String customerNumber;
    private ZonedDateTime activeTimeStamp = ZonedDateTime.now(ZoneId.of("UTC"));
    private String timeZone;
    private String methodName;
    private String channel;
    private String ipAddress;
    private String device;
    private String operatingSystem;
    private String browser;
    private String activity;
    private String otherFields;
    private String traceId;
    private String request;
    private String response;
    private String responseCode;
    private String phoneNumber;
    private String username;
    private String requestUrl;
}
