package com.healthcare.service;

import com.healthcare.dto.DrugDto;
import com.healthcare.entity.ConsultSession;
import com.healthcare.entity.DoctorProfile;
import com.healthcare.entity.Drug;
import com.healthcare.entity.User;
import com.healthcare.mapper.ConsultSessionMapper;
import com.healthcare.mapper.DoctorProfileMapper;
import com.healthcare.mapper.DrugMapper;
import com.healthcare.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final DrugMapper drugMapper;
    private final ConsultSessionMapper consultSessionMapper;

    public Page<UserVo> listUsers(int page, int size, String keyword, String status) {
        List<User> all = userMapper.selectAll();
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.toLowerCase();
            all = all.stream().filter(u ->
                    (u.getUsername() != null && u.getUsername().toLowerCase().contains(k))
                            || (u.getNickname() != null && u.getNickname().toLowerCase().contains(k))
                            || (u.getPhone() != null && u.getPhone().contains(keyword)))
                    .collect(Collectors.toList());
        }
        if (status != null && !status.isBlank()) {
            User.UserStatus s = User.UserStatus.valueOf(status);
            all = all.stream().filter(u -> u.getStatus() == s).collect(Collectors.toList());
        }
        int total = all.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<User> pageContent = from < to ? all.subList(from, to) : List.of();
        Pageable pg = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new PageImpl<>(pageContent.stream().map(this::userToVo).collect(Collectors.toList()), pg, total);
    }

    public UserVo getUser(Long id) {
        User u = userMapper.selectById(id);
        if (u == null) throw new RuntimeException("用户不存在");
        return userToVo(u);
    }

    @Transactional
    public void updateUserStatus(Long id, User.UserStatus status) {
        if (userMapper.selectById(id) == null) throw new RuntimeException("用户不存在");
        userMapper.updateStatus(id, status.name());
    }

    /**
     * 管理员编辑用户基础信息（昵称、手机号、角色、头像）
     */
    @Transactional
    public void updateUser(Long id, com.healthcare.dto.AdminUserUpdateDto dto) {
        User u = userMapper.selectById(id);
        if (u == null) throw new RuntimeException("用户不存在");
        if (dto.getNickname() != null) u.setNickname(dto.getNickname());
        if (dto.getPhone() != null) u.setPhone(dto.getPhone());
        if (dto.getAvatar() != null) u.setAvatar(dto.getAvatar());
        if (dto.getRole() != null) {
            u.setRole(User.Role.valueOf(dto.getRole()));
        }
        userMapper.update(u);
    }

    public Page<DoctorVo> listDoctors(int page, int size, String keyword, String auditStatus) {
        List<DoctorProfile> all = doctorProfileMapper.selectAll();
        if (auditStatus != null && !auditStatus.isBlank()) {
            DoctorProfile.AuditStatus s = DoctorProfile.AuditStatus.valueOf(auditStatus);
            all = all.stream().filter(dp -> dp.getAuditStatus() == s).collect(Collectors.toList());
        }
        int total = all.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<DoctorProfile> pageContent = from < to ? all.subList(from, to) : List.of();
        Pageable p = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<DoctorVo> voList = pageContent.stream().map(dp -> {
            DoctorVo v = new DoctorVo();
            v.setId(dp.getId());
            v.setUserId(dp.getUserId());
            User u = userMapper.selectById(dp.getUserId());
            if (u != null) {
                v.setUsername(u.getUsername());
                v.setPhone(u.getPhone());
                v.setUserStatus(u.getStatus().name());
            }
            v.setRealName(dp.getRealName());
            v.setHospital(dp.getHospital());
            v.setDepartment(dp.getDepartment());
            v.setTitle(dp.getTitle());
            v.setLicenseNo(dp.getLicenseNo());
            v.setAuditStatus(dp.getAuditStatus().name());
            v.setCreatedAt(dp.getCreatedAt());
            v.setConsultCount((long) consultSessionMapper.selectByDoctorIdOrderByUpdatedAtDesc(dp.getUserId()).size());
            return v;
        }).collect(Collectors.toList());
        return new PageImpl<>(voList, p, total);
    }

    public DoctorVo getDoctor(Long id) {
        DoctorProfile dp = doctorProfileMapper.selectById(id);
        if (dp == null) throw new RuntimeException("医生不存在");
        DoctorVo v = new DoctorVo();
        v.setId(dp.getId());
        v.setUserId(dp.getUserId());
        User u = userMapper.selectById(dp.getUserId());
        if (u != null) {
            v.setUsername(u.getUsername());
            v.setPhone(u.getPhone());
            v.setUserStatus(u.getStatus().name());
        }
        v.setRealName(dp.getRealName());
        v.setHospital(dp.getHospital());
        v.setDepartment(dp.getDepartment());
        v.setTitle(dp.getTitle());
        v.setLicenseNo(dp.getLicenseNo());
        v.setAuditStatus(dp.getAuditStatus().name());
        v.setCreatedAt(dp.getCreatedAt());
        v.setConsultCount((long) consultSessionMapper.selectByDoctorIdOrderByUpdatedAtDesc(dp.getUserId()).size());
        return v;
    }

    @Transactional
    public void updateDoctorAudit(Long id, DoctorProfile.AuditStatus status) {
        if (doctorProfileMapper.selectById(id) == null) throw new RuntimeException("医生不存在");
        doctorProfileMapper.updateAuditStatus(id, status.name());
    }

    @Transactional
    public void updateDoctorStatus(Long userId, User.UserStatus status) {
        if (userMapper.selectById(userId) == null) throw new RuntimeException("用户不存在");
        userMapper.updateStatus(userId, status.name());
    }

    /**
     * 管理员编辑医生档案信息
     */
    @Transactional
    public void updateDoctor(Long id, com.healthcare.dto.AdminDoctorUpdateDto dto) {
        DoctorProfile dp = doctorProfileMapper.selectById(id);
        if (dp == null) throw new RuntimeException("医生不存在");
        if (dto.getRealName() != null) dp.setRealName(dto.getRealName());
        if (dto.getHospital() != null) dp.setHospital(dto.getHospital());
        if (dto.getDepartment() != null) dp.setDepartment(dto.getDepartment());
        if (dto.getTitle() != null) dp.setTitle(dto.getTitle());
        if (dto.getLicenseNo() != null) dp.setLicenseNo(dto.getLicenseNo());
        doctorProfileMapper.update(dp);
    }

    public Page<Drug> listDrugs(int page, int size, String keyword) {
        List<Drug> all = drugMapper.selectAll();
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.toLowerCase();
            all = all.stream().filter(d -> d.getName() != null && d.getName().toLowerCase().contains(k))
                    .collect(Collectors.toList());
        }
        int total = all.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<Drug> pageContent = from < to ? all.subList(from, to) : List.of();
        Pageable p = PageRequest.of(page, size, Sort.by("name"));
        return new PageImpl<>(pageContent, p, total);
    }

    @Transactional
    public Drug createDrug(DrugDto dto) {
        Drug d = new Drug();
        d.setName(dto.getName());
        d.setSpec(dto.getSpec());
        d.setUnit(dto.getUnit());
        d.setUsageInstruction(dto.getUsageInstruction());
        d.setStatus(Drug.DrugStatus.ACTIVE);
        drugMapper.insert(d);
        return d;
    }

    @Transactional
    public Drug updateDrug(Long id, DrugDto dto) {
        Drug d = drugMapper.selectById(id);
        if (d == null) throw new RuntimeException("药品不存在");
        if (dto.getName() != null) d.setName(dto.getName());
        if (dto.getSpec() != null) d.setSpec(dto.getSpec());
        if (dto.getUnit() != null) d.setUnit(dto.getUnit());
        if (dto.getUsageInstruction() != null) d.setUsageInstruction(dto.getUsageInstruction());
        if (dto.getStatus() != null) d.setStatus(Drug.DrugStatus.valueOf(dto.getStatus()));
        drugMapper.update(d);
        return d;
    }

    public Map<String, Object> statsOverview() {
        Map<String, Object> m = new HashMap<>();
        m.put("userCount", userMapper.count());
        m.put("doctorCount", doctorProfileMapper.count());
        m.put("drugCount", drugMapper.count());
        m.put("consultCount", consultSessionMapper.count());
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        m.put("todayConsultCount", consultSessionMapper.countCreatedSince(start));
        return m;
    }

    public Map<String, Object> doctorConsultStats(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<ConsultSession> list = consultSessionMapper.selectWithDoctorCreatedSince(since);
        Map<LocalDate, Long> byDate = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            byDate.put(LocalDate.now().minusDays(i), 0L);
        }
        for (ConsultSession c : list) {
            LocalDate d = c.getCreatedAt().toLocalDate();
            byDate.merge(d, 1L, Long::sum);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("dates", new ArrayList<>(byDate.keySet()));
        result.put("counts", new ArrayList<>(byDate.values()));
        Map<Long, Long> byDoctor = new HashMap<>();
        for (ConsultSession c : list) {
            byDoctor.merge(c.getDoctorId(), 1L, Long::sum);
        }
        List<Map<String, Object>> doctorStats = new ArrayList<>();
        for (Map.Entry<Long, Long> e : byDoctor.entrySet()) {
            Long doctorUserId = e.getKey();
            Map<String, Object> row = new HashMap<>();
            row.put("doctorId", doctorUserId);
            // 优先使用医生真实姓名，其次昵称，最后用户名
            String name = null;
            DoctorProfile dp = doctorProfileMapper.selectByUserId(doctorUserId);
            if (dp != null && dp.getRealName() != null && !dp.getRealName().isBlank()) {
                name = dp.getRealName();
            } else {
                User u = userMapper.selectById(doctorUserId);
                if (u != null) {
                    if (u.getNickname() != null && !u.getNickname().isBlank()) {
                        name = u.getNickname();
                    } else {
                        name = u.getUsername();
                    }
                }
            }
            row.put("doctorName", name);
            row.put("count", e.getValue());
            doctorStats.add(row);
        }
        result.put("byDoctor", doctorStats);
        return result;
    }

    private UserVo userToVo(User u) {
        UserVo v = new UserVo();
        v.setId(u.getId());
        v.setUsername(u.getUsername());
        v.setPhone(u.getPhone());
        v.setNickname(u.getNickname());
        v.setRole(u.getRole().name());
        v.setStatus(u.getStatus().name());
        v.setCreatedAt(u.getCreatedAt());
        return v;
    }

    @lombok.Data
    public static class UserVo {
        private Long id;
        private String username;
        private String phone;
        private String nickname;
        private String role;
        private String status;
        private LocalDateTime createdAt;
    }

    @lombok.Data
    public static class DoctorVo {
        private Long id;
        private Long userId;
        private String username;
        private String phone;
        private String userStatus;
        private String realName;
        private String hospital;
        private String department;
        private String title;
        private String licenseNo;
        private String auditStatus;
        private LocalDateTime createdAt;
        private Long consultCount;
    }
}
