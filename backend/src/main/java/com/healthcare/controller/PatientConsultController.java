package com.healthcare.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.healthcare.dto.ManualConsultDto;
import com.healthcare.service.ConsultSessionService;
import com.healthcare.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patient/consult")
@RequiredArgsConstructor
public class PatientConsultController {

    private final ConsultSessionService consultSessionService;
    private final PrescriptionService prescriptionService;

    @PostMapping("/send")
    public ConsultSessionService.ConsultSessionResult send(@Valid @RequestBody ManualConsultDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        return consultSessionService.sendMessage(userId, dto.getSessionId(), dto.getContent());
    }

    @GetMapping("/sessions")
    public List<ConsultSessionService.ConsultSessionVo> listSessions() {
        long userId = StpUtil.getLoginIdAsLong();
        return consultSessionService.listByPatient(userId);
    }

    @GetMapping("/sessions/{sessionId}")
    public ConsultSessionService.ConsultSessionVo getSession(@PathVariable Long sessionId) {
        long userId = StpUtil.getLoginIdAsLong();
        return consultSessionService.getSession(userId, sessionId);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public List<ConsultSessionService.ConsultMessageVo> listMessages(@PathVariable Long sessionId) {
        long userId = StpUtil.getLoginIdAsLong();
        return consultSessionService.listMessages(userId, sessionId);
    }

    @GetMapping("/prescriptions")
    public List<PrescriptionService.PrescriptionVo> listPrescriptions() {
        long userId = StpUtil.getLoginIdAsLong();
        return prescriptionService.listByPatient(userId);
    }

    @GetMapping("/prescriptions/{id}")
    public PrescriptionService.PrescriptionVo getPrescription(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        return prescriptionService.get(id, userId, false);
    }
}
