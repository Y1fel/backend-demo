package com.y1fel.backend.config;

import com.y1fel.backend.common.util.UserContext;
import com.y1fel.backend.user.entity.SysUser;
import com.y1fel.backend.user.mapper.SysUserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {
    private static final String TOKEN_KEY_PREFIX = "auth:token:";

    private final StringRedisTemplate redisTemplate;
    private final SysUserMapper sysUserMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || "/auth/login".equals(path)
                || "/error".equals(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String authorization = request.getHeader("Authorization");

            if (authorization == null || !authorization.startsWith("Bearer ")) {
                writeUnauthorized(response, "未登录");
                return;
            }

            String token = authorization.substring(7);
            String userId = redisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + token);

            if (userId == null) {
                writeUnauthorized(response, "登录已过期");
                return;
            }

            SysUser user = sysUserMapper.selectById(Long.valueOf(userId));

            if (user == null || Integer.valueOf(1).equals(user.getDeleted()) || !"enabled".equals(user.getStatus())
               || !"admin".equals(user.getRole())) {
                writeUnauthorized(response, "用户不可用");
                return;
            }

            UserContext.set(new UserContext.LoginUser(
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    user.getRole(),
                    user.getOrgId()
            ));

            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String json = """
                {"code":401,"message":"%s","data":null}
                """.formatted(escapeJson(message));

        response.getWriter().write(json);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
