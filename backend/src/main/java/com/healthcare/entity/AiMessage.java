package com.healthcare.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 问诊消息，对应表 ai_message
 */
@Data
public class AiMessage {

    private Long id;
    private Long sessionId;
    private Sender sender;
    private String content;
    private LocalDateTime createdAt;

    public enum Sender { USER, AI }
}
