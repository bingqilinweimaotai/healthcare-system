package com.healthcare.entity;

import lombok.Data;

/**
 * 处方明细，对应表 prescription_item
 */
@Data
public class PrescriptionItem {

    private Long id;
    private Long prescriptionId;
    private Long drugId;
    private Integer quantity = 1;
    private String dosage;
    private String frequency;
}
