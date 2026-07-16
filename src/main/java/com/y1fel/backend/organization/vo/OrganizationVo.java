package com.y1fel.backend.organization.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrganizationVo {
    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private Integer sort;
    private List<OrganizationVo> children = new ArrayList<>();
}
