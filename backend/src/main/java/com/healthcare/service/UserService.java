package com.healthcare.service;

import com.healthcare.dto.PasswordChangeDto;
import com.healthcare.dto.UserProfileDto;
import com.healthcare.entity.DoctorProfile;
import com.healthcare.entity.User;
import com.healthcare.mapper.DoctorProfileMapper;
import com.healthcare.mapper.UserMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final PasswordEncoder passwordEncoder;

    public UserProfileVo getProfile(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new RuntimeException("用户不存在");
        UserProfileVo vo = new UserProfileVo();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        vo.setPhone(u.getPhone());
        vo.setRole(u.getRole().name());
        vo.setAvatar(u.getAvatar());
        if (u.getRole() == User.Role.DOCTOR) {
            DoctorProfile dp = doctorProfileMapper.selectByUserId(userId);
            if (dp != null) {
                vo.setRealName(dp.getRealName());
                vo.setHospital(dp.getHospital());
                vo.setDepartment(dp.getDepartment());
                vo.setTitle(dp.getTitle());
                vo.setAuditStatus(dp.getAuditStatus().name());
            }
        }
        return vo;
    }

    @Transactional
    public void updateProfile(Long userId, UserProfileDto dto) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new RuntimeException("用户不存在");
        if (dto.getNickname() != null) u.setNickname(dto.getNickname());
        if (dto.getPhone() != null) u.setPhone(dto.getPhone());
        if (dto.getAvatar() != null) u.setAvatar(dto.getAvatar());
        userMapper.update(u);

        if (u.getRole() == User.Role.DOCTOR) {
            DoctorProfile dp = doctorProfileMapper.selectByUserId(userId);
            if (dp == null) {
                dp = new DoctorProfile();
                dp.setUserId(userId);
            }
            if (dto.getRealName() != null) dp.setRealName(dto.getRealName());
            if (dto.getHospital() != null) dp.setHospital(dto.getHospital());
            if (dto.getDepartment() != null) dp.setDepartment(dto.getDepartment());
            if (dto.getTitle() != null) dp.setTitle(dto.getTitle());
            if (dp.getId() == null) {
                doctorProfileMapper.insert(dp);
            } else {
                doctorProfileMapper.update(dp);
            }
        }
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeDto dto) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new RuntimeException("用户不存在");
        if (!passwordEncoder.matches(dto.getOldPassword(), u.getPassword())) {
            throw new RuntimeException("原密码不正确");
        }
        u.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.update(u);
    }

    @Data
    public static class UserProfileVo {
        private Long id;
        private String username;
        private String nickname;
        private String phone;
        private String role;
        private String avatar;
        // 医生附加信息
        private String realName;
        private String hospital;
        private String department;
        private String title;
        private String auditStatus;
    }
}

