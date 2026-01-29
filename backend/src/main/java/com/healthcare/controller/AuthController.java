package com.healthcare.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.healthcare.common.Result;
import com.healthcare.dto.AuthDto;
import com.healthcare.dto.RegisterDto;
import com.healthcare.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
        return Result.ok(Map.of(
                "userId", userId,
                "username", username,
                "role", role,
                "token", StpUtil.getTokenValue()
        ));
    }
}
