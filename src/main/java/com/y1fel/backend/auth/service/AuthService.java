package com.y1fel.backend.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.y1fel.backend.auth.dto.LoginRequest;
import com.y1fel.backend.auth.vo.LoginResponseVo;
import com.y1fel.backend.auth.vo.UserInfoVo;
import com.y1fel.backend.common.exception.BizException;
import com.y1fel.backend.common.util.UserContext;
import com.y1fel.backend.operationlog.service.OperationLogService;
import com.y1fel.backend.organization.entity.SysOrg;
import com.y1fel.backend.organization.mapper.SysOrgMapper;
import com.y1fel.backend.user.entity.SysUser;
import com.y1fel.backend.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String TOKEN_KEY_PREFIX = "auth:token:";

    private final SysUserMapper sysUserMapper;
    private final SysOrgMapper sysOrgMapper;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;

    public LoginResponseVo login(LoginRequest request, String ip) {
        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getDeleted, 0));

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }

        if (!"enabled".equals(user.getStatus())) {
            throw new BizException("用户已被禁用");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + token, String.valueOf(user.getId()), Duration.ofHours(24));

        String orgName = getOrgName(user.getOrgId());
        operationLogService.record(user.getRealName(), "login", "auth", "用户登录成功", ip);

        return new LoginResponseVo(token, user.getRealName(), UserInfoVo.from(user, orgName));
    }

    public UserInfoVo profile() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(401, "未登录");
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BizException(401, "用户不存在");
        }

        if (!"enabled".equals(user.getStatus())) {
            throw new BizException(401, "用户已被禁用");
        }

        if(!"admin".equals(user.getRole())) {
            throw new BizException(403, "当前仅允许管理员登录");
        }

        return UserInfoVo.from(user, getOrgName(user.getOrgId()));
    }

    private String getOrgName(Long orgId) {
        if (orgId == null) {
            return null;
        }
        SysOrg org = sysOrgMapper.selectById(orgId);
        return org == null ? null : org.getName();
    }
}
