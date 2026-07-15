package com.y1fel.backend.dashboard.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.y1fel.backend.dashboard.vo.DashBoardStatsVo;
import com.y1fel.backend.dashboard.vo.WorkOrderSummaryVo;
import com.y1fel.backend.dashboard.vo.WorkOrderVo;
import com.y1fel.backend.workorder.entity.WorkOrder;
import com.y1fel.backend.workorder.mapper.WorkOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashBoardService {
    private final WorkOrderMapper workOrderMapper;

    public WorkOrderSummaryVo summary() {
        long todo = countByStatus("pending");
        long done = countByStatus("done");
        long today = workOrderMapper.selectCount(Wrappers.<WorkOrder>lambdaQuery()
                .ge(WorkOrder::getCreateTime, LocalDate.now().atStartOfDay()));

        return new WorkOrderSummaryVo(
                new DashBoardStatsVo(todo, today, done),
                recentByStatus("pending", 8),
                recentByStatus("auditing", 8),
                recentByStatus("done", 10)
        );
    }

    private long countByStatus(String status) {
        return workOrderMapper.selectCount(Wrappers.<WorkOrder>lambdaQuery()
                .eq(WorkOrder::getStatus, status));
    }

    private List<WorkOrderVo> recentByStatus(String status, int size) {
        return workOrderMapper.selectPage(
                Page.of(1, size),
                Wrappers.<WorkOrder>lambdaQuery()
                        .eq(WorkOrder::getStatus, status)
                        .orderByDesc(WorkOrder::getCreateTime)
        ).getRecords().stream().map(WorkOrderVo::from).toList();
    }
}
