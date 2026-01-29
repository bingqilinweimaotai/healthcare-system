package com.healthcare.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PrescriptionDto {

    @NotNull
    private Long sessionId;
    private String diagnosis;

    @NotEmpty(message = "处方明细不能为空")
    @Valid
    private List<PrescriptionItemDto> items;

    @Data
    public static class PrescriptionItemDto {
        @NotNull
        private Long drugId;
        private Integer quantity = 1;
        private String dosage;
        private String frequency;
    }
}
