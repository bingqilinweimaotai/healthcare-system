package com.healthcare.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.healthcare.common.Result;
import com.healthcare.dto.AiConsultDto;
import com.healthcare.service.AiConsultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Slf4j
public class AiConsultController {

    private final AiConsultService aiConsultService;

    @PostMapping("/consult")
    public Result<AiConsultService.AiConsultResult> consult(@Valid @RequestBody AiConsultDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(aiConsultService.consult(userId, dto.getSessionId(), dto.getContent()));
    }

    /**
     * 流式问诊：通过 SSE 逐步推送 AI 回复（提升“不卡住”的体验）。
     * 前端用 fetch 读取流即可（EventSource 只支持 GET，不适合带 body）。
     */
    @PostMapping(value = "/consult/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter consultStream(@Valid @RequestBody AiConsultDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        SseEmitter emitter = new SseEmitter(0L);

        CompletableFuture.runAsync(() -> {
            try {
                AiConsultService.StreamInit init = aiConsultService.initStream(userId, dto.getSessionId(), dto.getContent());

                emitter.send(SseEmitter.event().name("meta").data(Map.of("sessionId", init.getSessionId())));

                aiConsultService.streamReply(
                        init,
                        delta -> {
                            try {
                                emitter.send(SseEmitter.event().name("delta").data(delta));
                            } catch (Exception e) {
                                // 客户端断开等
                                throw new RuntimeException(e);
                            }
                        },
                        full -> {
                            try {
                                emitter.send(SseEmitter.event().name("done").data("OK"));
                                emitter.complete();
                            } catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        },
                        err -> {
                            log.warn("AI stream error: {}", err.getMessage());
                            try {
                                emitter.send(SseEmitter.event().name("error").data(err.getMessage() != null ? err.getMessage() : "AI stream error"));
                            } catch (Exception ignore) {
                                // ignore
                            }
                            emitter.complete();
                        }
                );
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage() != null ? e.getMessage() : "AI stream error"));
                } catch (Exception ignore) {
                    // ignore
                }
                emitter.complete();
            }
        });

        return emitter;
    }

    @GetMapping("/sessions")
    public Result<List<AiConsultService.AiSessionVo>> listSessions() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(aiConsultService.listSessions(userId));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<AiConsultService.AiMessageVo>> listMessages(@PathVariable Long sessionId) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(aiConsultService.listMessages(userId, sessionId));
    }
}
