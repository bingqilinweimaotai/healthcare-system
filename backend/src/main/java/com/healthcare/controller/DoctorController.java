package com.healthcare.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.healthcare.common.Result;
import com.healthcare.dto.ManualConsultDto;
import com.healthcare.dto.PrescriptionDto;
import com.healthcare.entity.Drug;
import com.healthcare.mapper.DrugMapper;
import com.healthcare.service.ConsultSessionService;
import com.healthcare.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final ConsultSessionService consultSessionService;
    private final PrescriptionService prescriptionService;
    private final DrugMapper drugMapper;

    @GetMapping("/consult/waiting")
    public Result<List<ConsultSessionService.ConsultSessionVo>> listWaiting() {
        return Result.ok(consultSessionService.listWaitingForDoctors());
    }

    @PostMapping("/consult/claim/{sessionId}")
    public Result<Void> claim(@PathVariable Long sessionId) {
        long doctorId = StpUtil.getLoginIdAsLong();
        consultSessionService.claim(doctorId, sessionId);
        return Result.ok();
    }

    @GetMapping("/consult/sessions")
    public Result<List<ConsultSessionService.ConsultSessionVo>> listMySessions() {
        long doctorId = StpUtil.getLoginIdAsLong();
        return Result.ok(consultSessionService.listByDoctor(doctorId));
    }

    @GetMapping("/consult/sessions/{sessionId}")
    public Result<ConsultSessionService.ConsultSessionVo> getSession(@PathVariable Long sessionId) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(consultSessionService.getSession(userId, sessionId));
    }

    @PostMapping("/consult/send")
    public Result<ConsultSessionService.ConsultSessionResult> send(@Valid @RequestBody ManualConsultDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(consultSessionService.sendMessage(userId, dto.getSessionId(), dto.getContent()));
    }

    @GetMapping("/consult/sessions/{sessionId}/messages")
    public Result<List<ConsultSessionService.ConsultMessageVo>> listMessages(@PathVariable Long sessionId) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(consultSessionService.listMessages(userId, sessionId));
    }

    @GetMapping("/drugs")
    public Result<List<Drug>> listDrugs() {
        return Result.ok(drugMapper.selectByStatusOrderByNameAsc(Drug.DrugStatus.ACTIVE.name()));
    }

    @PostMapping("/prescriptions")
    public Result<PrescriptionService.PrescriptionVo> createPrescription(@Valid @RequestBody PrescriptionDto dto) {
        long doctorId = StpUtil.getLoginIdAsLong();
        return Result.ok(prescriptionService.create(doctorId, dto));
    }

    @GetMapping("/prescriptions")
    public Result<List<PrescriptionService.PrescriptionVo>> listPrescriptions() {
        long doctorId = StpUtil.getLoginIdAsLong();
        return Result.ok(prescriptionService.listByDoctor(doctorId));
    }

    @GetMapping("/prescriptions/{id}")
    public Result<PrescriptionService.PrescriptionVo> getPrescription(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(prescriptionService.get(id, userId, true));
    }
}
