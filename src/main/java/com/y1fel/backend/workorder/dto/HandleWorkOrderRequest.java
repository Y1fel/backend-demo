package com.y1fel.backend.workorder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HandleWorkOrderRequest {
    @NotBlank(message = "处理方式不能为空")
    private String handleType;

    @NotBlank(message = "处理结果不能为空")
    private String result;
}
