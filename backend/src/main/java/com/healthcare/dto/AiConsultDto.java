package com.healthcare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiConsultDto {

    private Long sessionId;

    @NotBlank(message = "咨询内容不能为空")
    private String content;
}
