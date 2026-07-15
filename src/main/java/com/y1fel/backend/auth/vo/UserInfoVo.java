package com.y1fel.backend.auth.vo;

import com.y1fel.backend.user.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfoVo {
    private Long id;
    private String username;
    private String realname;
    private String role;
    private Long orgId;
    private String orgName;

    public static UserInfoVo from(SysUser sysUser, String orgName) {
        return new UserInfoVo(
                sysUser.getId(),
                sysUser.getUsername(),
                sysUser.getRealName(),
                sysUser.getRole(),
                sysUser.getOrgId(),
                orgName
        );
    }
}
