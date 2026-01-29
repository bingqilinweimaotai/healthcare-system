package com.healthcare.controller;

import cn.dev33.satoken.stp.StpUtil;
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
    public List<ConsultSessionService.ConsultSessionVo> listWaiting() {
        return consultSessionService.listWaitingForDoctors();
    }

    @PostMapping("/consult/claim/{sessionId}")
    public void claim(@PathVariable Long sessionId) {
        long doctorId = StpUtil.getLoginIdAsLong();
        consultSessionService.claim(doctorId, sessionId);
    }

    @GetMapping("/consult/sessions")
    public List<ConsultSessionService.ConsultSessionVo> listMySessions() {
        long doctorId = StpUtil.getLoginIdAsLong();
        return consultSessionService.listByDoctor(doctorId);
    }

    @GetMapping("/consult/sessions/{sessionId}")
    public ConsultSessionService.ConsultSessionVo getSession(@PathVariable Long sessionId) {
        long userId = StpUtil.getLoginIdAsLong();
        return consultSessionService.getSession(userId, sessionId);
    }

    @PostMapping("/consult/send")
    public ConsultSessionService.ConsultSessionResult send(@Valid @RequestBody ManualConsultDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        return consultSessionService.sendMessage(userId, dto.getSessionId(), dto.getContent());
    }

    @GetMapping("/consult/sessions/{sessionId}/messages")
    public List<ConsultSessionService.ConsultMessageVo> listMessages(@PathVariable Long sessionId) {
        long userId = StpUtil.getLoginIdAsLong();
        return consultSessionService.listMessages(userId, sessionId);
    }

    @GetMapping("/drugs")
    public List<Drug> listDrugs() {
        return drugMapper.selectByStatusOrderByNameAsc(Drug.DrugStatus.ACTIVE.name());
    }

    @PostMapping("/prescriptions")
    public PrescriptionService.PrescriptionVo createPrescription(@Valid @RequestBody PrescriptionDto dto) {
        long doctorId = StpUtil.getLoginIdAsLong();
        return prescriptionService.create(doctorId, dto);
    }

    @GetMapping("/prescriptions")
    public List<PrescriptionService.PrescriptionVo> listPrescriptions() {
        long doctorId = StpUtil.getLoginIdAsLong();
        return prescriptionService.listByDoctor(doctorId);
    }

    @GetMapping("/prescriptions/{id}")
    public PrescriptionService.PrescriptionVo getPrescription(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        return prescriptionService.get(id, userId, true);
    }
}
