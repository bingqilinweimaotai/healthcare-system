package com.healthcare.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医生档案，对应表 doctor_profile
 */
@Data
public class DoctorProfile {

    private Long id;
    private Long userId;
    private String realName;
    private String hospital;
    private String department;
    private String title;
    private String licenseNo;
    private AuditStatus auditStatus = AuditStatus.PENDING;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum AuditStatus { PENDING, APPROVED, REJECTED }
}
