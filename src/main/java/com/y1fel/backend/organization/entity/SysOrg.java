package com.y1fel.backend.organization.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_org")
public class SysOrg {
    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
