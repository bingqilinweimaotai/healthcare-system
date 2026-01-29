package com.healthcare.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人工咨询会话，对应表 consult_session
 */
@Data
public class ConsultSession {

    private Long id;
    private Long patientId;
    private Long doctorId;
    private ConsultStatus status = ConsultStatus.WAITING_CLAIM;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    public enum ConsultStatus { WAITING_CLAIM, IN_PROGRESS, FINISHED, CLOSED }
}
