package com.y1fel.backend.workorder.controller;

import com.y1fel.backend.common.response.PageResult;
import com.y1fel.backend.common.response.Result;
import com.y1fel.backend.workorder.dto.AuditRejectRequest;
import com.y1fel.backend.workorder.dto.HandleWorkOrderRequest;
import com.y1fel.backend.workorder.dto.SubmitWorkOrderRequest;
import com.y1fel.backend.workorder.dto.WorkOrderQuery;
import com.y1fel.backend.workorder.service.WorkOrderService;
import com.y1fel.backend.workorder.vo.WorkOrderVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workorder")
@RequiredArgsConstructor
public class WorkOrderController {
    private final WorkOrderService workOrderService;

    @GetMapping("/todo")
    public Result<PageResult<WorkOrderVo>> todo(WorkOrderQuery query) {
        return Result.success(workOrderService.todo(query));
    }

    @GetMapping("/audit")
    public Result<PageResult<WorkOrderVo>> audit(WorkOrderQuery query) {
        return Result.success(workOrderService.audit(query));
    }

    @GetMapping("/done")
    public Result<PageResult<WorkOrderVo>> done(WorkOrderQuery query) {
        return Result.success(workOrderService.done(query));
    }

    @GetMapping("/query")
    public Result<PageResult<WorkOrderVo>> query(WorkOrderQuery query) {
        return Result.success(workOrderService.query(query));
    }

    @GetMapping("/{id}")
    public Result<WorkOrderVo> detail(@PathVariable Long id) {
        return Result.success(workOrderService.detail(id));
    }

    @PostMapping("/{id}/handle")
    public Result<Void> handle(@PathVariable Long id, @Valid @RequestBody HandleWorkOrderRequest request) {
        workOrderService.handle(id, request);
        return Result.success();
    }

    @PostMapping("/{id}/audit-pass")
    public Result<Void> auditPass(@PathVariable Long id) {
        workOrderService.auditPass(id);
        return Result.success();
    }

    @PostMapping("/{id}/audit-reject")
    public Result<Void> auditReject(@PathVariable Long id, @Valid @RequestBody AuditRejectRequest request) {
        workOrderService.auditReject(id, request);
        return Result.success();
    }

    @PostMapping("/submit")
    public Result<WorkOrderVo> submit(@Valid @RequestBody SubmitWorkOrderRequest request) {
        return Result.success(workOrderService.submit(request));
    }
}
