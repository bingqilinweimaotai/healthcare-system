package com.healthcare.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员修改医生档案信息
 */
@Data
public class AdminDoctorUpdateDto {

    @Size(max = 64)
    private String realName;

    @Size(max = 128)
    private String hospital;

    @Size(max = 64)
    private String department;

    @Size(max = 32)
    private String title;

    @Size(max = 64)
    private String licenseNo;
}

