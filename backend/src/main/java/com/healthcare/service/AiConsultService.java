package com.healthcare.service;

import com.healthcare.entity.AiMessage;
import com.healthcare.entity.AiSession;
import com.healthcare.mapper.AiMessageMapper;
import com.healthcare.mapper.AiSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiConsultService {

    private final AiSessionMapper aiSessionMapper;
    private final AiMessageMapper aiMessageMapper;
    private final LangChain4jService langChain4jService;

    private static final String SYSTEM_PROMPT = "你是一名医疗咨询助手。请根据用户描述的症状给予初步的健康建议，并提醒用户：本建议仅供参考，不能替代线下就医。如有急症或持续不适，请及时到正规医院就诊。不要直接开具处方或替代医生诊断。";

    @Transactional
    public AiConsultResult consult(Long userId, Long sessionId, String content) {
        AiSession session;
        if (sessionId == null || sessionId <= 0) {
            session = new AiSession();
            session.setUserId(userId);
            session.setStatus(AiSession.SessionStatus.ONGOING);
            aiSessionMapper.insert(session);
        } else {
            session = aiSessionMapper.selectById(sessionId);
            if (session == null) throw new RuntimeException("会话不存在");
            if (!session.getUserId().equals(userId)) throw new RuntimeException("无权限操作该会话");
        }

        AiMessage userMsg = new AiMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setSender(AiMessage.Sender.USER);
        userMsg.setContent(content);
        aiMessageMapper.insert(userMsg);

        List<AiMessage> history = aiMessageMapper.selectBySessionIdOrderByCreatedAtAsc(session.getId());
        String reply = langChain4jService.chat(SYSTEM_PROMPT, history, content);

        AiMessage aiMsg = new AiMessage();
        aiMsg.setSessionId(session.getId());
        aiMsg.setSender(AiMessage.Sender.AI);
        aiMsg.setContent(reply);
        aiMessageMapper.insert(aiMsg);

        AiConsultResult result = new AiConsultResult();
        result.setSessionId(session.getId());
        result.setUserMessage(content);
        result.setAiReply(reply);
        return result;
    }

    public List<AiSessionVo> listSessions(Long userId) {
        return aiSessionMapper.selectByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(s -> {
                    AiSessionVo v = new AiSessionVo();
                    v.setId(s.getId());
                    v.setStatus(s.getStatus().name());
                    v.setCreatedAt(s.getCreatedAt());
                    return v;
                })
                .collect(Collectors.toList());
    }

    public List<AiMessageVo> listMessages(Long userId, Long sessionId) {
        AiSession s = aiSessionMapper.selectById(sessionId);
        if (s == null) throw new RuntimeException("会话不存在");
        if (!s.getUserId().equals(userId)) throw new RuntimeException("无权限");
        return aiMessageMapper.selectBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(m -> {
                    AiMessageVo v = new AiMessageVo();
                    v.setId(m.getId());
                    v.setSender(m.getSender().name());
                    v.setContent(m.getContent());
                    v.setCreatedAt(m.getCreatedAt());
                    return v;
                })
                .collect(Collectors.toList());
    }

    @lombok.Data
    public static class AiConsultResult {
        private Long sessionId;
        private String userMessage;
        private String aiReply;
    }

    @lombok.Data
    public static class AiSessionVo {
        private Long id;
        private String status;
        private java.time.LocalDateTime createdAt;
    }

    @lombok.Data
    public static class AiMessageVo {
        private Long id;
        private String sender;
        private String content;
        private java.time.LocalDateTime createdAt;
    }
}
