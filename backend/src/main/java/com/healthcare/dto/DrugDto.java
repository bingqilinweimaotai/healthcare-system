package com.healthcare.dto;

import lombok.Data;

@Data
public class DrugDto {

    private Long id;
    private String name;
    private String spec;
    private String unit;
    private String usageInstruction;
    private String imageUrl;
    private String status;
}
