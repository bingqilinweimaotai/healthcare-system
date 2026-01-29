package com.healthcare.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.healthcare.common.Result;
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
@SaCheckRole("PATIENT")
public class PatientConsultController {

    private final ConsultSessionService consultSessionService;
    private final PrescriptionService prescriptionService;

    @PostMapping("/send")
    @SaCheckPermission("patient:consult:create")
    public Result<ConsultSessionService.ConsultSessionResult> send(@Valid @RequestBody ManualConsultDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(consultSessionService.sendMessage(userId, dto.getSessionId(), dto.getContent()));
    }

    @GetMapping("/sessions")
    @SaCheckPermission("patient:consult:list")
    public Result<List<ConsultSessionService.ConsultSessionVo>> listSessions() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(consultSessionService.listByPatient(userId));
    }

    @GetMapping("/sessions/{sessionId}")
    public Result<ConsultSessionService.ConsultSessionVo> getSession(@PathVariable Long sessionId) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(consultSessionService.getSession(userId, sessionId));
    }

    @PostMapping("/sessions/{sessionId}/close")
    public Result<Void> closeSession(@PathVariable Long sessionId) {
        long userId = StpUtil.getLoginIdAsLong();
        consultSessionService.closeByPatient(userId, sessionId);
        return Result.ok();
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ConsultSessionService.ConsultMessageVo>> listMessages(@PathVariable Long sessionId) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(consultSessionService.listMessages(userId, sessionId));
    }

    @GetMapping("/prescriptions")
    @SaCheckPermission("patient:history:view")
    public Result<List<PrescriptionService.PrescriptionVo>> listPrescriptions() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(prescriptionService.listByPatient(userId));
    }

    @GetMapping("/prescriptions/{id}")
    public Result<PrescriptionService.PrescriptionVo> getPrescription(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(prescriptionService.get(id, userId, false));
    }
}
