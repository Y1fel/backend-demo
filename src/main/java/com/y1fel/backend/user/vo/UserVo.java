package com.y1fel.backend.user.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVo {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private Long orgId;
    private String orgName;
    private String role;
    private String status;
    private LocalDateTime createTime;
}
