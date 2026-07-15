package com.y1fel.backend.dashboard.controller;

import com.y1fel.backend.common.response.Result;
import com.y1fel.backend.dashboard.service.DashBoardService;
import com.y1fel.backend.dashboard.vo.WorkOrderSummaryVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashBoardController {
    private final DashBoardService dashboardService;

    @GetMapping("/summary")
    public Result<WorkOrderSummaryVo> summary() {
        return Result.success(dashboardService.summary());
    }
}
