package com.healthcare.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.healthcare.dto.AiConsultDto;
import com.healthcare.service.AiConsultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiConsultController {

    private final AiConsultService aiConsultService;

    @PostMapping("/consult")
    public AiConsultService.AiConsultResult consult(@Valid @RequestBody AiConsultDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        return aiConsultService.consult(userId, dto.getSessionId(), dto.getContent());
    }

    @GetMapping("/sessions")
    public List<AiConsultService.AiSessionVo> listSessions() {
        long userId = StpUtil.getLoginIdAsLong();
        return aiConsultService.listSessions(userId);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public List<AiConsultService.AiMessageVo> listMessages(@PathVariable Long sessionId) {
        long userId = StpUtil.getLoginIdAsLong();
        return aiConsultService.listMessages(userId, sessionId);
    }
}
