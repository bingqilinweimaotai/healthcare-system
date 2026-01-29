package com.healthcare.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限实体，对应表 sys_permission
 */
@Data
public class Permission {
    private Long id;
    private String code;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
