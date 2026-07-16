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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String TOKEN_KEY_PREFIX = "auth:token:";
    private static final String LOGIN_FAILURE_KEY_PREFIX = "auth:login:failure:";
    private static final String LOGIN_LOCK_KEY_PREFIX = "auth:login:lock:";
    private static final Duration LOGIN_FAILURE_WINDOW = Duration.ofMinutes(10);
    private static final Duration LOGIN_LOCK_DURATION = Duration.ofMinutes(15);
    private static final int MAX_ACCOUNT_FAILURES = 5;
    private static final int MAX_IP_FAILURES = 20;

    private final SysUserMapper sysUserMapper;
    private final SysOrgMapper sysOrgMapper;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;

    public LoginResponseVo login(LoginRequest request, String ip) {
        String accountSubject = "account:" + normalizeUsername(request.getUsername());
        String ipSubject = "ip:" + normalizeIp(ip);

        ensureLoginAllowed(accountSubject, ipSubject);

        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getDeleted, 0));

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            recordLoginFailure(accountSubject, ipSubject);
            throw new BizException(401, "用户名或密码错误");
        }

        clearLoginFailures(accountSubject, ipSubject);

        if (!"enabled".equals(user.getStatus())) {
            throw new BizException(403, "用户已被禁用");
        }

        if (!"admin".equals(user.getRole())) {
            throw new BizException(403, "当前仅允许管理员登录");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + token, String.valueOf(user.getId()), Duration.ofHours(24));

        String orgName = getOrgName(user.getOrgId());
        operationLogService.record(user.getRealName(), "login", "auth", "用户登录成功", ip);

        return new LoginResponseVo(token, user.getRealName(), UserInfoVo.from(user, orgName));
    }

    private void ensureLoginAllowed(String accountSubject, String ipSubject) {
        if (isLocked(accountSubject) || isLocked(ipSubject)) {
            throw new BizException(429, "登录失败次数过多，请15分钟后再试");
        }
    }

    private void recordLoginFailure(String accountSubject, String ipSubject) {
        boolean accountLocked = incrementFailure(accountSubject, MAX_ACCOUNT_FAILURES);
        boolean ipLocked = incrementFailure(ipSubject, MAX_IP_FAILURES);

        if (accountLocked || ipLocked) {
            throw new BizException(429, "登录失败次数过多，请15分钟后再试");
        }
    }

    private boolean incrementFailure(String subject, int maxFailures) {
        String failureKey = failureKey(subject);
        Long failures = redisTemplate.opsForValue().increment(failureKey);

        if (failures != null && failures == 1L) {
            redisTemplate.expire(failureKey, LOGIN_FAILURE_WINDOW);
        }

        if (failures != null && failures >= maxFailures) {
            redisTemplate.opsForValue().set(lockKey(subject), "1", LOGIN_LOCK_DURATION);
            redisTemplate.delete(failureKey);
            return true;
        }

        return false;
    }

    private boolean isLocked(String subject) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey(subject)));
    }

    private void clearLoginFailures(String accountSubject, String ipSubject) {
        redisTemplate.delete(failureKey(accountSubject));
        redisTemplate.delete(failureKey(ipSubject));
    }

    private String failureKey(String subject) {
        return LOGIN_FAILURE_KEY_PREFIX + fingerprint(subject);
    }

    private String lockKey(String subject) {
        return LOGIN_LOCK_KEY_PREFIX + fingerprint(subject);
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeIp(String ip) {
        return ip == null || ip.isBlank() ? "unknown" : ip.trim();
    }

    private String fingerprint(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
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
