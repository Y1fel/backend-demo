package com.y1fel.backend.workorder.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.y1fel.backend.common.exception.BizException;
import com.y1fel.backend.common.response.PageResult;
import com.y1fel.backend.common.util.UserContext;
import com.y1fel.backend.operationlog.service.OperationLogService;
import com.y1fel.backend.workorder.dto.AuditRejectRequest;
import com.y1fel.backend.workorder.dto.HandleWorkOrderRequest;
import com.y1fel.backend.workorder.dto.WorkOrderQuery;
import com.y1fel.backend.workorder.entity.WorkOrder;
import com.y1fel.backend.workorder.mapper.WorkOrderMapper;
import com.y1fel.backend.workorder.vo.WorkOrderVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkOrderService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WorkOrderMapper workOrderMapper;
    private final OperationLogService operationLogService;

    public PageResult<WorkOrderVo> todo(WorkOrderQuery query) {
        return page(query, "pending");
    }

    public PageResult<WorkOrderVo> audit(WorkOrderQuery query) {
        return page(query, "auditing");
    }

    public PageResult<WorkOrderVo> done(WorkOrderQuery query) {
        return page(query, "done");
    }

    public PageResult<WorkOrderVo> query(WorkOrderQuery query) {
        return page(query, query.getStatus());
    }

    public WorkOrderVo detail(Long id) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BizException("工单不存在");
        }
        return WorkOrderVo.from(order);
    }

    @Transactional
    public void handle(Long id, HandleWorkOrderRequest request) {
        if (!"phone".equals(request.getHandleType()) && !"visit".equals(request.getHandleType())) {
            throw new BizException("处理方式只能是 phone 或 visit");
        }

        LocalDateTime now = LocalDateTime.now();
        WorkOrder update = new WorkOrder();
        update.setStatus("auditing");
        update.setHandleType(request.getHandleType());
        update.setHandleResult(request.getResult());
        update.setHandleTime(now);
        update.setHandlerId(UserContext.getUserId());
        update.setHandlerName(UserContext.getRealName());
        update.setUpdateTime(now);

        int affected = workOrderMapper.update(
                update,
                Wrappers.<WorkOrder>lambdaUpdate()
                        .eq(WorkOrder::getId, id)
                        .eq(WorkOrder::getStatus, "pending")
        );

        if (affected != 1) {
            throw new BizException(409, "工单不存在或已被其他请求处理");
        }

        operationLogService.record(
                UserContext.getRealName(),
                "operation",
                "workorder",
                "办理工单：" + id,
                null
        );
    }

    @Transactional
    public void auditPass(Long id) {
        requireAdmin();

        LocalDateTime now = LocalDateTime.now();
        WorkOrder update = new WorkOrder();
        update.setStatus("done");
        update.setAuditTime(now);
        update.setCompleteTime(now);
        update.setAuditorId(UserContext.getUserId());
        update.setAuditorName(UserContext.getRealName());
        update.setUpdateTime(now);

        int affected = workOrderMapper.update(
                update,
                Wrappers.<WorkOrder>lambdaUpdate()
                        .eq(WorkOrder::getId, id)
                        .eq(WorkOrder::getStatus, "auditing")
        );

        if (affected != 1) {
            throw new BizException(409, "工单不存在或审核状态已发生变化");
        }

        operationLogService.record(
                UserContext.getRealName(),
                "operation",
                "workorder",
                "审核通过工单：" + id,
                null
        );
    }

    @Transactional
    public void auditReject(Long id, AuditRejectRequest request) {
        requireAdmin();

        LocalDateTime now = LocalDateTime.now();
        WorkOrder update = new WorkOrder();
        update.setStatus("pending");
        update.setRejectReason(request.getReason());
        update.setAuditTime(now);
        update.setAuditorId(UserContext.getUserId());
        update.setAuditorName(UserContext.getRealName());
        update.setUpdateTime(now);

        int affected = workOrderMapper.update(
                update,
                Wrappers.<WorkOrder>lambdaUpdate()
                        .eq(WorkOrder::getId, id)
                        .eq(WorkOrder::getStatus, "auditing")
        );

        if (affected != 1) {
            throw new BizException(409, "工单不存在或审核状态已发生变化");
        }

        operationLogService.record(
                UserContext.getRealName(),
                "operation",
                "workorder",
                "审核驳回工单：" + id,
                null
        );
    }

    private PageResult<WorkOrderVo> page(WorkOrderQuery query, String fixedStatus) {
        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();

        if (fixedStatus != null && !fixedStatus.isBlank()) {
            wrapper.eq(WorkOrder::getStatus, fixedStatus);
        }

        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.like(WorkOrder::getDescription, query.getKeyword());
        }

        if (query.getReporter() != null && !query.getReporter().isBlank()) {
            wrapper.like(WorkOrder::getReporter, query.getReporter());
        }

        if (query.getPhone() != null && !query.getPhone().isBlank()) {
            wrapper.like(WorkOrder::getPhone, query.getPhone());
        }

        LocalDateTime startTime = parseStartTime(query.getStartTime());
        if (startTime != null) {
            wrapper.ge(WorkOrder::getCreateTime, startTime);
        }

        LocalDateTime endTime = parseEndTime(query.getEndTime());
        if (endTime != null) {
            wrapper.le(WorkOrder::getCreateTime, endTime);
        }

        LocalDateTime completeStartTime = parseStartTime(query.getCompleteStartTime());
        if (completeStartTime != null) {
            wrapper.ge(WorkOrder::getCompleteTime, completeStartTime);
        }

        LocalDateTime completeEndTime = parseEndTime(query.getCompleteEndTime());
        if (completeEndTime != null) {
            wrapper.le(WorkOrder::getCompleteTime, completeEndTime);
        }

        if ("done".equals(fixedStatus)) {
            wrapper.orderByDesc(WorkOrder::getCompleteTime);
        } else {
            wrapper.orderByDesc(WorkOrder::getCreateTime);
        }

        Page<WorkOrder> result = workOrderMapper.selectPage(Page.of(page, pageSize), wrapper);

        List<WorkOrderVo> list = result.getRecords()
                .stream()
                .map(WorkOrderVo::from)
                .toList();

        return new PageResult<>(list, result.getTotal());
    }

    private void requireAdmin() {
        if (!"admin".equals(UserContext.getRole())) {
            throw new BizException(403, "无审核权限");
        }
    }

    private LocalDateTime parseStartTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() == 10) {
            return LocalDate.parse(value).atStartOfDay();
        }
        return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
    }

    private LocalDateTime parseEndTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() == 10) {
            return LocalDate.parse(value).atTime(23, 59, 59);
        }
        return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
    }
}
