package com.healthcare.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.healthcare.common.Result;
import com.healthcare.dto.PasswordChangeDto;
import com.healthcare.dto.UserProfileDto;
import com.healthcare.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<UserService.UserProfileVo> getProfile() {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UserProfileDto dto) {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        userService.updateProfile(userId, dto);
        return Result.ok();
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeDto dto) {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        userService.changePassword(userId, dto);
        return Result.ok();
    }
}

