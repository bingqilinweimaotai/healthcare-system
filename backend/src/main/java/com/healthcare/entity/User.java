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
    private Role role = Role.PATIENT;
    private UserStatus status = UserStatus.NORMAL;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Role { PATIENT, DOCTOR, ADMIN }
    public enum UserStatus { NORMAL, DISABLED, PENDING }
}
