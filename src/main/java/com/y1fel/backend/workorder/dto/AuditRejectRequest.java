package com.y1fel.backend.workorder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuditRejectRequest {
    @NotBlank(message = "驳回原因不能为空")
    private String reason;
}
