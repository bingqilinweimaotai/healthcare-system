package com.healthcare.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户个人信息修改入参
 */
@Data
public class UserProfileDto {

    @Size(max = 64)
    private String nickname;

    @Size(max = 32)
    private String phone;

    /** 头像 URL */
    @Size(max = 255)
    private String avatar;

    // 以下字段仅医生角色会使用，对应 doctor_profile
    @Size(max = 64)
    private String realName;

    @Size(max = 128)
    private String hospital;

    @Size(max = 64)
    private String department;

    @Size(max = 32)
    private String title;
}

