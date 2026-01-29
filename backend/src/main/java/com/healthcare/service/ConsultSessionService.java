package com.healthcare.service;

import com.healthcare.entity.ConsultMessage;
import com.healthcare.entity.ConsultSession;
import com.healthcare.entity.DoctorProfile;
import com.healthcare.entity.User;
import com.healthcare.mapper.ConsultMessageMapper;
import com.healthcare.mapper.ConsultSessionMapper;
import com.healthcare.mapper.DoctorProfileMapper;
import com.healthcare.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultSessionService {

    private final ConsultSessionMapper consultSessionMapper;
    private final ConsultMessageMapper consultMessageMapper;
    private final UserMapper userMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final WebSocketNotifyService notifyService;

    @Transactional
    public ConsultSessionResult sendMessage(Long userId, Long sessionId, String content) {
        ConsultSession session;
        if (sessionId == null || sessionId <= 0) {
            session = new ConsultSession();
            session.setPatientId(userId);
            session.setStatus(ConsultSession.ConsultStatus.WAITING_CLAIM);
            consultSessionMapper.insert(session);
            ConsultMessage msg = new ConsultMessage();
            msg.setSessionId(session.getId());
            msg.setSenderType(ConsultMessage.SenderType.PATIENT);
            msg.setSenderId(userId);
            msg.setContent(content);
            consultMessageMapper.insert(msg);
            notifyService.broadcastNewConsult(session.getId(), content, userId);
        } else {
            session = consultSessionMapper.selectById(sessionId);
            if (session == null) throw new RuntimeException("会话不存在");
            boolean isPatient = session.getPatientId().equals(userId);
            boolean isDoctor = session.getDoctorId() != null && session.getDoctorId().equals(userId);
            if (!isPatient && !isDoctor) throw new RuntimeException("无权限操作该会话");
            ConsultMessage msg = new ConsultMessage();
            msg.setSessionId(session.getId());
            msg.setSenderType(isPatient ? ConsultMessage.SenderType.PATIENT : ConsultMessage.SenderType.DOCTOR);
            msg.setSenderId(userId);
            msg.setContent(content);
            consultMessageMapper.insert(msg);
            notifyService.sendConsultMessage(session.getId(), userId, content, isPatient);
        }
        ConsultSessionResult result = new ConsultSessionResult();
        result.setSessionId(session.getId());
        result.setStatus(session.getStatus().name());
        return result;
    }

    @Transactional
    public void claim(Long doctorId, Long sessionId) {
        ConsultSession session = consultSessionMapper.selectByIdForUpdate(sessionId);
        if (session == null) throw new RuntimeException("会话不存在");
        if (session.getStatus() != ConsultSession.ConsultStatus.WAITING_CLAIM) {
            throw new RuntimeException("该会话已被认领或已结束");
        }
        session.setDoctorId(doctorId);
        session.setStatus(ConsultSession.ConsultStatus.IN_PROGRESS);
        consultSessionMapper.update(session);
        ConsultMessage sys = new ConsultMessage();
        sys.setSessionId(session.getId());
        sys.setSenderType(ConsultMessage.SenderType.SYSTEM);
        sys.setSenderId(null);
        sys.setContent("医生已接诊，请开始沟通。");
        consultMessageMapper.insert(sys);
        notifyService.notifyClaimed(session.getId(), session.getPatientId(), doctorId);
    }

    /**
     * 医生将会话标记为已完成
     */
    @Transactional
    public void completeByDoctor(Long doctorId, Long sessionId) {
        ConsultSession session = consultSessionMapper.selectByIdForUpdate(sessionId);
        if (session == null) throw new RuntimeException("会话不存在");
        if (session.getDoctorId() == null || !session.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("仅接诊医生可以完成该会话");
        }
        if (session.getStatus() == ConsultSession.ConsultStatus.FINISHED
                || session.getStatus() == ConsultSession.ConsultStatus.CLOSED) {
            return;
        }
        session.setStatus(ConsultSession.ConsultStatus.FINISHED);
        consultSessionMapper.update(session);

        ConsultMessage sys = new ConsultMessage();
        sys.setSessionId(session.getId());
        sys.setSenderType(ConsultMessage.SenderType.SYSTEM);
        sys.setSenderId(null);
        sys.setContent("医生已将本次会话标记为完成，如需继续咨询可发起新的会话。");
        consultMessageMapper.insert(sys);
        notifyService.notifyStatusChanged(session.getId(), session.getStatus().name());
    }

    /**
     * 患者主动关闭会话
     */
    @Transactional
    public void closeByPatient(Long patientId, Long sessionId) {
        ConsultSession session = consultSessionMapper.selectByIdForUpdate(sessionId);
        if (session == null) throw new RuntimeException("会话不存在");
        if (!session.getPatientId().equals(patientId)) {
            throw new RuntimeException("仅发起该会话的患者可以结束会话");
        }
        if (session.getStatus() == ConsultSession.ConsultStatus.FINISHED
                || session.getStatus() == ConsultSession.ConsultStatus.CLOSED) {
            return;
        }
        session.setStatus(ConsultSession.ConsultStatus.CLOSED);
        consultSessionMapper.update(session);

        ConsultMessage sys = new ConsultMessage();
        sys.setSessionId(session.getId());
        sys.setSenderType(ConsultMessage.SenderType.SYSTEM);
        sys.setSenderId(null);
        sys.setContent("患者已结束本次会话，如需继续请重新发起咨询。");
        consultMessageMapper.insert(sys);
        notifyService.notifyStatusChanged(session.getId(), session.getStatus().name());
    }

    public List<ConsultSessionVo> listByPatient(Long patientId) {
        return consultSessionMapper.selectByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    public List<ConsultSessionVo> listWaitingForDoctors() {
        return consultSessionMapper.selectByStatusOrderByCreatedAtAsc(ConsultSession.ConsultStatus.WAITING_CLAIM.name()).stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    public List<ConsultSessionVo> listByDoctor(Long doctorId) {
        return consultSessionMapper.selectByDoctorIdOrderByUpdatedAtDesc(doctorId).stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    public List<ConsultMessageVo> listMessages(Long userId, Long sessionId) {
        ConsultSession s = consultSessionMapper.selectById(sessionId);
        if (s == null) throw new RuntimeException("会话不存在");
        boolean allowed = s.getPatientId().equals(userId)
                || (s.getDoctorId() != null && s.getDoctorId().equals(userId));
        if (!allowed) throw new RuntimeException("无权限");
        return consultMessageMapper.selectBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(m -> {
                    ConsultMessageVo v = new ConsultMessageVo();
                    v.setId(m.getId());
                    v.setSenderType(m.getSenderType().name());
                    v.setSenderId(m.getSenderId());
                    v.setContent(m.getContent());
                    v.setCreatedAt(m.getCreatedAt());
                    return v;
                })
                .collect(Collectors.toList());
    }

    public ConsultSessionVo getSession(Long userId, Long sessionId) {
        ConsultSession s = consultSessionMapper.selectById(sessionId);
        if (s == null) throw new RuntimeException("会话不存在");
        boolean allowed = s.getPatientId().equals(userId)
                || (s.getDoctorId() != null && s.getDoctorId().equals(userId));
        if (!allowed) throw new RuntimeException("无权限");
        return toVo(s);
    }

    private ConsultSessionVo toVo(ConsultSession s) {
        ConsultSessionVo v = new ConsultSessionVo();
        v.setId(s.getId());
        v.setPatientId(s.getPatientId());
        v.setDoctorId(s.getDoctorId());
        v.setStatus(s.getStatus().name());
        v.setCreatedAt(s.getCreatedAt());
        v.setUpdatedAt(s.getUpdatedAt());
        User patient = userMapper.selectById(s.getPatientId());
        v.setPatientName(patient != null ? (patient.getNickname() != null ? patient.getNickname() : patient.getUsername()) : null);
        if (s.getDoctorId() != null) {
            Long doctorId = s.getDoctorId();
            // 优先展示医生档案里的真实姓名，其次昵称，最后用户名
            String doctorName = null;
            DoctorProfile dp = doctorProfileMapper.selectByUserId(doctorId);
            if (dp != null && dp.getRealName() != null && !dp.getRealName().isBlank()) {
                doctorName = dp.getRealName();
            } else {
                User doctor = userMapper.selectById(doctorId);
                if (doctor != null) {
                    if (doctor.getNickname() != null && !doctor.getNickname().isBlank()) {
                        doctorName = doctor.getNickname();
                    } else {
                        doctorName = doctor.getUsername();
                    }
                }
            }
            v.setDoctorName(doctorName);
        }
        return v;
    }

    @lombok.Data
    public static class ConsultSessionResult {
        private Long sessionId;
        private String status;
    }

    @lombok.Data
    public static class ConsultSessionVo {
        private Long id;
        private Long patientId;
        private Long doctorId;
        private String patientName;
        private String doctorName;
        private String status;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
    }

    @lombok.Data
    public static class ConsultMessageVo {
        private Long id;
        private String senderType;
        private Long senderId;
        private String content;
        private java.time.LocalDateTime createdAt;
    }
}
