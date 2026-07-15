package com.y1fel.backend.workorder.dto;

import lombok.Data;

@Data
public class WorkOrderQuery {
    private Integer page = 1;
    private Integer pageSize = 10;

    private String keyword;
    private String reporter;
    private String phone;
    private String status;

    private String startTime;
    private String endTime;
    private String completeStartTime;
    private String completeEndTime;
}
