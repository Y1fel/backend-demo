package com.y1fel.backend.workorder.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("work_order")
public class WorkOrder {
    @TableId
    private Long id;

    private String reporter;
    private String phone;
    private String address;
    private String description;
    private String images;
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
    private LocalDateTime updateTime;
}
