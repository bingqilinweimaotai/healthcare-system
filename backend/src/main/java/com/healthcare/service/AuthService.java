package com.healthcare.service;

import cn.dev33.satoken.stp.StpUtil;
import com.healthcare.dto.AuthDto;
import com.healthcare.dto.RegisterDto;
import com.healthcare.entity.DoctorProfile;
import com.healthcare.entity.User;
import com.healthcare.mapper.DoctorProfileMapper;
import com.healthcare.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Map<String, Object> login(AuthDto dto) {
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) throw new RuntimeException("用户不存在");
        if (user.getStatus() == User.UserStatus.DISABLED) throw new RuntimeException("账号已被禁用");
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) throw new RuntimeException("用户名或密码错误");
        if (user.getRole() == User.Role.DOCTOR) {
            DoctorProfile dp = doctorProfileMapper.selectByUserId(user.getId());
            if (dp != null && dp.getAuditStatus() != DoctorProfile.AuditStatus.APPROVED) {
                throw new RuntimeException("您的账号尚未通过管理员审核，请耐心等待");
            }
        }
        StpUtil.login(user.getId());
        StpUtil.getSession().set("role", user.getRole().name());
        StpUtil.getSession().set("username", user.getUsername());
        Map<String, Object> result = new HashMap<>();
        result.put("token", StpUtil.getTokenValue());
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole().name());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        return result;
    }

    @Transactional
    public void register(RegisterDto dto) {
        if (userMapper.countByUsername(dto.getUsername()) > 0) throw new RuntimeException("用户名已存在");
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setRole(User.Role.valueOf(dto.getRole().toUpperCase()));
        user.setStatus(User.UserStatus.NORMAL);
        userMapper.insert(user);
        if (user.getRole() == User.Role.DOCTOR) {
            DoctorProfile dp = new DoctorProfile();
            dp.setUserId(user.getId());
            dp.setRealName(user.getNickname() != null ? user.getNickname() : "待填写");
            dp.setAuditStatus(DoctorProfile.AuditStatus.PENDING);
            doctorProfileMapper.insert(dp);
        }
    }
}
