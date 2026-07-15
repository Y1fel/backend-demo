package com.y1fel.backend.auth.controller;

import com.y1fel.backend.auth.dto.LoginRequest;
import com.y1fel.backend.auth.service.AuthService;
import com.y1fel.backend.auth.vo.LoginResponseVo;
import com.y1fel.backend.auth.vo.UserInfoVo;
import com.y1fel.backend.common.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponseVo> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return Result.success(authService.login(request, servletRequest.getRemoteAddr()));
    }

    @GetMapping("/profile")
    public Result<UserInfoVo> profile() {
        return Result.success(authService.profile());
    }
}
