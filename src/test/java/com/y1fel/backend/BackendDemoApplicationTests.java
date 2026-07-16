package com.y1fel.backend;

import com.y1fel.backend.auth.dto.LoginRequest;
import com.y1fel.backend.auth.service.AuthService;
import com.y1fel.backend.common.exception.BizException;
import com.y1fel.backend.common.exception.GlobalExceptionHandler;
import com.y1fel.backend.operationlog.service.OperationLogService;
import com.y1fel.backend.organization.mapper.SysOrgMapper;
import com.y1fel.backend.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class BackendDemoApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void locksAccountAfterFiveFailedLogins() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        SysOrgMapper orgMapper = mock(SysOrgMapper.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        OperationLogService operationLogService = mock(OperationLogService.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(valueOperations.increment(anyString())).thenReturn(
                1L, 1L,
                2L, 2L,
                3L, 3L,
                4L, 4L,
                5L, 5L
        );
        when(userMapper.selectOne(any())).thenReturn(null);

        AuthService authService = new AuthService(
                userMapper,
                orgMapper,
                redisTemplate,
                passwordEncoder,
                operationLogService
        );

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong-password");

        for (int i = 0; i < 4; i++) {
            BizException exception = assertThrows(
                    BizException.class,
                    () -> authService.login(request, "127.0.0.1")
            );
            assertEquals(401, exception.getCode());
        }

        BizException locked = assertThrows(
                BizException.class,
                () -> authService.login(request, "127.0.0.1")
        );
        assertEquals(429, locked.getCode());
        verify(valueOperations).set(anyString(), eq("1"), eq(Duration.ofMinutes(15)));
    }

    @Test
    void returnsHttpStatusMatchingBusinessErrorCode() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<?> response = handler.handleBiz(new BizException(409, "状态冲突"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody() == null
                ? null
                : ((com.y1fel.backend.common.response.Result<?>) response.getBody()).getCode());
    }

}
