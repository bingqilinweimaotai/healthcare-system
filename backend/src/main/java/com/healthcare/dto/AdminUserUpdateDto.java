package com.healthcare.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员修改用户基础信息
 */
@Data
public class AdminUserUpdateDto {

    @Size(max = 64)
    private String nickname;

    @Size(max = 32)
    private String phone;

    /** 角色：PATIENT / DOCTOR / ADMIN */
    private String role;

    /** 头像 URL，可选 */
    @Size(max = 255)
    private String avatar;
}

