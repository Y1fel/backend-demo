package com.y1fel.backend.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String password;
    private Long orgId;
    private String role;
    private String status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
