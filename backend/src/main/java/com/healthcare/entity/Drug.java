package com.healthcare.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 药品，对应表 drug
 */
@Data
public class Drug {

    private Long id;
    private String name;
    private String spec;
    private String unit;
    private String usageInstruction;
    private DrugStatus status = DrugStatus.ACTIVE;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum DrugStatus { ACTIVE, DISABLED }
}
