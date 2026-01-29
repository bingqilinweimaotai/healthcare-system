package com.healthcare.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.healthcare.dto.AuthDto;
import com.healthcare.dto.RegisterDto;
import com.healthcare.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody AuthDto dto) {
        return authService.login(dto);
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterDto dto) {
        authService.register(dto);
        Map<String, Object> r = new HashMap<>();
        r.put("code", 200);
        r.put("message", "注册成功");
        return r;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout() {
        StpUtil.logout();
        Map<String, Object> r = new HashMap<>();
        r.put("code", 200);
        r.put("message", "已退出");
        return r;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        if (!StpUtil.isLogin()) {
            Map<String, Object> r = new HashMap<>();
            r.put("code", 401);
            r.put("message", "未登录");
            return r;
        }
        long userId = StpUtil.getLoginIdAsLong();
        String role = (String) StpUtil.getSession().get("role");
        String username = (String) StpUtil.getSession().get("username");
        Map<String, Object> r = new HashMap<>();
        r.put("userId", userId);
        r.put("username", username);
        r.put("role", role);
        r.put("token", StpUtil.getTokenValue());
        return r;
    }
}
