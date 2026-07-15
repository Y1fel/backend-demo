package com.y1fel.backend.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class WorkOrderSummaryVo {
    private DashBoardStatsVo stats;
    private List<WorkOrderVo> recentTodo;
    private List<WorkOrderVo> recentAudit;
    private List<WorkOrderVo> recentDone;
}
