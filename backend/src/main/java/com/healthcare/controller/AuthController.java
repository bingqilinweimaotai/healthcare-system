package com.healthcare.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.healthcare.common.Result;
import com.healthcare.dto.AuthDto;
import com.healthcare.dto.RegisterDto;
import com.healthcare.service.AuthService;
import com.healthcare.entity.User;
import com.healthcare.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody AuthDto dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDto dto) {
        authService.register(dto);
        return Result.ok();
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.ok();
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        String role = (String) StpUtil.getSession().get("role");
        String username = (String) StpUtil.getSession().get("username");
        User user = userMapper.selectById(userId);
        return Result.ok(Map.of(
                "userId", userId,
                "username", username,
                "role", role,
                "nickname", user != null && user.getNickname() != null ? user.getNickname() : username,
                "avatar", user != null ? user.getAvatar() : null,
                "token", StpUtil.getTokenValue()
        ));
    }
}
