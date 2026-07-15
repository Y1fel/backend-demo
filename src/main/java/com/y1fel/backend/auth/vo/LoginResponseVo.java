package com.y1fel.backend.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseVo {
    private String token;
    private String username;
    private UserInfoVo userInfo;
}
