package com.healthcare.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotifyService {

    private final SimpMessagingTemplate messagingTemplate;

    public static final String TOPIC_NEW_CONSULT = "/topic/new-consult";
    public static final String TOPIC_CONSULT_PREFIX = "/topic/consult/";

    public void broadcastNewConsult(Long sessionId, String contentPreview, Long patientId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sessionId", sessionId);
        payload.put("contentPreview", contentPreview != null && contentPreview.length() > 80 ? contentPreview.substring(0, 80) + "..." : contentPreview);
        payload.put("patientId", patientId);
        payload.put("type", "NEW_CONSULT");
        messagingTemplate.convertAndSend(TOPIC_NEW_CONSULT, payload);
        log.debug("Broadcast new consult sessionId={}", sessionId);
    }

    public void sendConsultMessage(Long sessionId, Long senderId, String content, boolean fromPatient) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sessionId", sessionId);
        payload.put("senderId", senderId);
        payload.put("content", content);
        payload.put("fromPatient", fromPatient);
        payload.put("type", "MESSAGE");
        messagingTemplate.convertAndSend(TOPIC_CONSULT_PREFIX + sessionId, payload);
    }

    public void notifyClaimed(Long sessionId, Long patientId, Long doctorId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sessionId", sessionId);
        payload.put("doctorId", doctorId);
        payload.put("type", "CLAIMED");
        messagingTemplate.convertAndSend(TOPIC_CONSULT_PREFIX + sessionId, payload);
    }
}
