package com.healthcare.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人工咨询消息，对应表 consult_message
 */
@Data
public class ConsultMessage {

    private Long id;
    private Long sessionId;
    private SenderType senderType;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;

    public enum SenderType { PATIENT, DOCTOR, SYSTEM }
}
