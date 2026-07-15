package com.y1fel.backend.workorder.vo;

import com.y1fel.backend.workorder.entity.WorkOrder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
public class WorkOrderVo {
    private Long id;
    private String reporter;
    private String phone;
    private String address;
    private String description;
    private List<String> images;
    private String status;

    private String handleType;
    private String handleResult;
    private LocalDateTime handleTime;
    private Long handlerId;
    private String handlerName;

    private LocalDateTime completeTime;
    private String rejectReason;
    private LocalDateTime auditTime;
    private Long auditorId;
    private String auditorName;

    private LocalDateTime createTime;

    public static WorkOrderVo from(WorkOrder order) {
        WorkOrderVo vo = new WorkOrderVo();
        vo.setId(order.getId());
        vo.setReporter(order.getReporter());
        vo.setPhone(order.getPhone());
        vo.setAddress(order.getAddress());
        vo.setDescription(order.getDescription());
        vo.setImages(parseImages(order.getImages()));
        vo.setStatus(order.getStatus());
        vo.setHandleType(order.getHandleType());
        vo.setHandleResult(order.getHandleResult());
        vo.setHandleTime(order.getHandleTime());
        vo.setHandlerId(order.getHandlerId());
        vo.setHandlerName(order.getHandlerName());
        vo.setCompleteTime(order.getCompleteTime());
        vo.setRejectReason(order.getRejectReason());
        vo.setAuditTime(order.getAuditTime());
        vo.setAuditorId(order.getAuditorId());
        vo.setAuditorName(order.getAuditorName());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    private static List<String> parseImages(String images) {
        if (images == null || images.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(images.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}
