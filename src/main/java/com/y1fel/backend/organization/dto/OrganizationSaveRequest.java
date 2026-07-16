package com.y1fel.backend.organization.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrganizationSaveRequest {
    private Long parentId;
    @NotBlank(message = "组织名称不能为空")
    private String name;
    @NotBlank(message = "组织编码不能为空")
    private String code;
    private Integer sort = 0;
}
