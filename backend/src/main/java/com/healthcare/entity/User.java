package com.healthcare.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应表 sys_user
 */
@Data
public class User {

    private Long id;
    private String username;
    private String password;
    private String phone;
    private String nickname;
    /**
     * 头像 URL，可为空，前端使用网络地址展示头像
     */
    private String avatar;
    private Role role = Role.PATIENT;
    private UserStatus status = UserStatus.NORMAL;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Role { PATIENT, DOCTOR, ADMIN }
    public enum UserStatus { NORMAL, DISABLED, PENDING }
}
