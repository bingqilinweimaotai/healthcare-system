package com.healthcare.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 问诊会话，对应表 ai_session
 */
@Data
public class AiSession {

    private Long id;
    private Long userId;
    private SessionStatus status = SessionStatus.ONGOING;
    private LocalDateTime createdAt;

    public enum SessionStatus { ONGOING, FINISHED }
}
