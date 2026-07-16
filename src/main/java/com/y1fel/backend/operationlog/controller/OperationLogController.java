package com.y1fel.backend.operationlog.controller;

import com.y1fel.backend.common.response.*;
import com.y1fel.backend.operationlog.entity.SysLog;
import com.y1fel.backend.operationlog.service.OperationLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/log")
@RequiredArgsConstructor
public class OperationLogController {
    private final OperationLogQueryService queryService;
    @GetMapping
    public Result<PageResult<SysLog>> page(@RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="10") int pageSize, @RequestParam(required=false) String operator,
            @RequestParam(required=false) String type, @RequestParam(required=false) String startTime,
            @RequestParam(required=false) String endTime) {
        return Result.success(queryService.page(page, pageSize, operator, type, startTime, endTime));
    }
}
