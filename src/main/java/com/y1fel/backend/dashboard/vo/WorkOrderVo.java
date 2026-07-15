package com.y1fel.backend.dashboard.vo;

import com.y1fel.backend.workorder.entity.WorkOrder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkOrderVo {
    private Long id;
    private String reporter;
    private String phone;
    private String address;
    private String description;
    private String status;
    private String handleType;
    private String handleResult;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;

    public static WorkOrderVo from(WorkOrder workOrder) {
        WorkOrderVo workOrderVo = new WorkOrderVo();
        workOrderVo.setId(workOrder.getId());
        workOrderVo.setReporter(workOrder.getReporter());
        workOrderVo.setPhone(workOrder.getPhone());
        workOrderVo.setAddress(workOrder.getAddress());
        workOrderVo.setDescription(workOrder.getDescription());
        workOrderVo.setStatus(workOrder.getStatus());
        workOrderVo.setHandleType(workOrder.getHandleType());
        workOrderVo.setHandleResult(workOrder.getHandleResult());
        workOrderVo.setCompleteTime(workOrder.getCompleteTime());
        workOrderVo.setCreateTime(workOrder.getCreateTime());
        return workOrderVo;
    }
}
