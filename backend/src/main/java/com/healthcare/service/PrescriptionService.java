package com.healthcare.service;

import com.healthcare.dto.PrescriptionDto;
import com.healthcare.entity.ConsultMessage;
import com.healthcare.entity.Drug;
import com.healthcare.entity.Prescription;
import com.healthcare.entity.PrescriptionItem;
import com.healthcare.mapper.ConsultMessageMapper;
import com.healthcare.mapper.ConsultSessionMapper;
import com.healthcare.mapper.DrugMapper;
import com.healthcare.mapper.PrescriptionItemMapper;
import com.healthcare.mapper.PrescriptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final ConsultSessionMapper consultSessionMapper;
    private final DrugMapper drugMapper;
    private final ConsultMessageMapper consultMessageMapper;
    private final WebSocketNotifyService notifyService;

    @Transactional
    public PrescriptionVo create(Long doctorId, PrescriptionDto dto) {
        com.healthcare.entity.ConsultSession session = consultSessionMapper.selectById(dto.getSessionId());
        if (session == null) throw new RuntimeException("会话不存在");
        if (session.getDoctorId() == null || !session.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("仅接诊医生可开具处方");
        }
        if (session.getStatus() != com.healthcare.entity.ConsultSession.ConsultStatus.IN_PROGRESS) {
            throw new RuntimeException("会话已结束，无法开处方");
        }
        Prescription p = new Prescription();
        p.setSessionId(session.getId());
        p.setPatientId(session.getPatientId());
        p.setDoctorId(doctorId);
        p.setDiagnosis(dto.getDiagnosis());
        prescriptionMapper.insert(p);
        for (PrescriptionDto.PrescriptionItemDto item : dto.getItems()) {
            Drug drug = drugMapper.selectById(item.getDrugId());
            if (drug == null) throw new RuntimeException("药品不存在: " + item.getDrugId());
            if (drug.getStatus() != Drug.DrugStatus.ACTIVE) throw new RuntimeException("药品已禁用: " + drug.getName());
            PrescriptionItem pi = new PrescriptionItem();
            pi.setPrescriptionId(p.getId());
            pi.setDrugId(drug.getId());
            pi.setQuantity(item.getQuantity() != null ? item.getQuantity() : 1);
            pi.setDosage(item.getDosage());
            pi.setFrequency(item.getFrequency());
            prescriptionItemMapper.insert(pi);
        }

        // 写入一条系统消息，提示患者处方已开具
        ConsultMessage sys = new ConsultMessage();
        sys.setSessionId(session.getId());
        sys.setSenderType(ConsultMessage.SenderType.SYSTEM);
        sys.setSenderId(null);
        sys.setContent("医生已为您开具处方，可前往「问诊记录」查看处方详情。如无其他问题，您可以结束本次会话。");
        consultMessageMapper.insert(sys);
        // 通过 WebSocket 通知会话双方刷新消息
        notifyService.sendConsultMessage(session.getId(), doctorId, sys.getContent(), false);

        return toVo(p);
    }

    public List<PrescriptionVo> listByPatient(Long patientId) {
        return prescriptionMapper.selectByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    public List<PrescriptionVo> listByDoctor(Long doctorId) {
        return prescriptionMapper.selectByDoctorIdOrderByCreatedAtDesc(doctorId).stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    public PrescriptionVo get(Long id, Long userId, boolean isDoctor) {
        Prescription p = prescriptionMapper.selectById(id);
        if (p == null) throw new RuntimeException("处方不存在");
        if (!p.getPatientId().equals(userId) && (!isDoctor || !p.getDoctorId().equals(userId))) {
            throw new RuntimeException("无权限");
        }
        return toVo(p);
    }

    private PrescriptionVo toVo(Prescription p) {
        PrescriptionVo v = new PrescriptionVo();
        v.setId(p.getId());
        v.setSessionId(p.getSessionId());
        v.setPatientId(p.getPatientId());
        v.setDoctorId(p.getDoctorId());
        v.setDiagnosis(p.getDiagnosis());
        v.setCreatedAt(p.getCreatedAt());
        List<PrescriptionItem> items = prescriptionItemMapper.selectByPrescriptionId(p.getId());
        v.setItems(items.stream().map(i -> {
            PrescriptionItemVo iv = new PrescriptionItemVo();
            iv.setDrugId(i.getDrugId());
            iv.setQuantity(i.getQuantity());
            iv.setDosage(i.getDosage());
            iv.setFrequency(i.getFrequency());
            Drug d = drugMapper.selectById(i.getDrugId());
            if (d != null) {
                iv.setDrugName(d.getName());
                iv.setSpec(d.getSpec());
                iv.setUnit(d.getUnit());
            }
            return iv;
        }).collect(Collectors.toList()));
        return v;
    }

    @lombok.Data
    public static class PrescriptionVo {
        private Long id;
        private Long sessionId;
        private Long patientId;
        private Long doctorId;
        private String diagnosis;
        private java.time.LocalDateTime createdAt;
        private List<PrescriptionItemVo> items;
    }

    @lombok.Data
    public static class PrescriptionItemVo {
        private Long drugId;
        private String drugName;
        private String spec;
        private String unit;
        private Integer quantity;
        private String dosage;
        private String frequency;
    }
}
