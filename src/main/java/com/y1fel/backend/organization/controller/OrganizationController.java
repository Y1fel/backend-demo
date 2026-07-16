package com.y1fel.backend.organization.controller;

import com.y1fel.backend.common.response.Result;
import com.y1fel.backend.common.util.UserContext;
import com.y1fel.backend.operationlog.service.OperationLogService;
import com.y1fel.backend.organization.dto.OrganizationSaveRequest;
import com.y1fel.backend.organization.service.OrganizationService;
import com.y1fel.backend.organization.vo.OrganizationVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/system/org")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;
    private final OperationLogService logService;

    @GetMapping
    public Result<List<OrganizationVo>> tree(@RequestParam(required = false) String keyword) {
        return Result.success(organizationService.tree(keyword));
    }
    @PostMapping
    public Result<Void> add(@Valid @RequestBody OrganizationSaveRequest body, HttpServletRequest request) {
        organizationService.add(body);
        logService.record(operator(), "operation", "组织管理", "新增组织：" + body.getName(), request.getRemoteAddr());
        return Result.success();
    }
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody OrganizationSaveRequest body, HttpServletRequest request) {
        organizationService.update(id, body);
        logService.record(operator(), "operation", "组织管理", "修改组织：" + body.getName(), request.getRemoteAddr());
        return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        organizationService.delete(id);
        logService.record(operator(), "operation", "组织管理", "删除组织 id=" + id, request.getRemoteAddr());
        return Result.success();
    }
    private String operator() {
        UserContext.LoginUser user = UserContext.get();
        return user == null ? "unknown" : user.realName();
    }
}
