package com.y1fel.backend.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashBoardStatsVo {
    private long todo;
    private long today;
    private long done;
}
