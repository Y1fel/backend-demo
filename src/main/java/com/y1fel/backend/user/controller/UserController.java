package com.y1fel.backend.user.controller;

import com.y1fel.backend.common.response.*;
import com.y1fel.backend.common.util.UserContext;
import com.y1fel.backend.operationlog.service.OperationLogService;
import com.y1fel.backend.user.dto.*;
import com.y1fel.backend.user.service.UserManagementService;
import com.y1fel.backend.user.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {
    private final UserManagementService userService;
    private final OperationLogService logService;
    @GetMapping
    public Result<PageResult<UserVo>> page(@RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="10") int pageSize, @RequestParam(required=false) String keyword) {
        return Result.success(userService.page(page, pageSize, keyword));
    }
    @PostMapping
    public Result<Void> add(@Valid @RequestBody UserSaveRequest body, HttpServletRequest request) {
        userService.add(body); log("新增用户：" + body.getUsername(), request); return Result.success();
    }
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserSaveRequest body, HttpServletRequest request) {
        userService.update(id, body); log("修改用户：" + body.getUsername(), request); return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        userService.delete(id); log("删除用户 id=" + id, request); return Result.success();
    }
    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@Valid @RequestBody BatchDeleteRequest body, HttpServletRequest request) {
        userService.batchDelete(body.getIds()); log("批量删除用户：" + body.getIds(), request); return Result.success();
    }
    private void log(String content, HttpServletRequest request) {
        UserContext.LoginUser user = UserContext.get();
        logService.record(user == null ? "unknown" : user.realName(), "operation", "用户管理", content, request.getRemoteAddr());
    }
}
