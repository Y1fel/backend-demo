package com.y1fel.backend.workorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class SubmitWorkOrderRequest {
    @NotBlank(message = "上报人不能为空")
    private String reporter;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @NotBlank(message = "家庭住址不能为空")
    private String address;

    @NotBlank(message = "问题描述不能为空")
    private String description;

    private List<String> images;
}
