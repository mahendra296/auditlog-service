package com.audit.entity;

import com.audit.dto.AuditLog;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_activity")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "active_timestamp")
    private ZonedDateTime activeTimeStamp;

    @Column(name = "timezone")
    private String timeZone;

    private String channel;

    @Column(name = "ip_address")
    private String ipAddress;

    private String device;

    @Column(name = "operating_system")
    private String operatingSystem;

    private String browser;

    private String activity;
    @Column(name = "method_name")
    private String methodName;

    @Column(name = "other_fields")
    private String otherFields;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "customer_number")
    private String customerNumber;

    @Column(name = "username")
    private String username;

    @Column(name = "request_url")
    private String requestUrl;

    @Column(name = "request")
    private String request;

    @Column(name = "response")
    private String response;

    public static AuditLogEntity builder(AuditLog auditLog) {
        AuditLogEntity auditLogEntity = new AuditLogEntity();
        if (auditLog != null) {
            auditLogEntity.setCustomerId(auditLog.getCustomerId());
            auditLogEntity.setTimeZone(auditLog.getTimeZone());
            auditLogEntity.setChannel(auditLog.getChannel());
            auditLogEntity.setIpAddress(auditLog.getIpAddress());
            auditLogEntity.setDevice(auditLog.getDevice());
            auditLogEntity.setOperatingSystem(auditLog.getOperatingSystem());
            auditLogEntity.setBrowser(auditLog.getBrowser());
            auditLogEntity.setActivity(auditLog.getActivity());
            auditLogEntity.setOtherFields(auditLog.getOtherFields());
            auditLogEntity.setTraceId(auditLog.getTraceId());
            auditLogEntity.setPhoneNumber(auditLog.getPhoneNumber());
            auditLogEntity.setUsername(auditLog.getUsername());
            auditLogEntity.setCustomerNumber(auditLog.getCustomerNumber());
            auditLogEntity.setRequest(auditLog.getRequest());
            auditLogEntity.setResponse(auditLog.getResponse());
            auditLogEntity.setMethodName(auditLog.getMethodName());
            auditLogEntity.setRequestUrl(auditLog.getRequestUrl());
        }
        return auditLogEntity;
    }
}

