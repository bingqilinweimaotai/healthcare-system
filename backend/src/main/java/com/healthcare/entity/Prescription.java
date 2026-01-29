package com.healthcare.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 处方，对应表 prescription
 */
@Data
public class Prescription {

    private Long id;
    private Long sessionId;
    private Long patientId;
    private Long doctorId;
    private String diagnosis;
    private LocalDateTime createdAt;
}
