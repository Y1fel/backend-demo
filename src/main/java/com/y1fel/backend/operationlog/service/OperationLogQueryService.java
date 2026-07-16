package com.y1fel.backend.operationlog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.y1fel.backend.common.exception.BizException;
import com.y1fel.backend.common.response.PageResult;
import com.y1fel.backend.operationlog.entity.SysLog;
import com.y1fel.backend.operationlog.mapper.SysLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.time.format.*;

@Service
@RequiredArgsConstructor
public class OperationLogQueryService {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final SysLogMapper logMapper;
    public PageResult<SysLog> page(int page, int pageSize, String operator, String type, String startTime, String endTime) {
        LambdaQueryWrapper<SysLog> query = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(operator)) query.like(SysLog::getOperator, operator);
        if (StringUtils.hasText(type)) query.eq(SysLog::getType, type);
        try {
            if (StringUtils.hasText(startTime)) query.ge(SysLog::getCreateTime, LocalDateTime.parse(startTime, FORMAT));
            if (StringUtils.hasText(endTime)) query.le(SysLog::getCreateTime, LocalDateTime.parse(endTime, FORMAT));
        } catch (DateTimeParseException exception) {
            throw new BizException("时间格式应为 yyyy-MM-dd HH:mm:ss");
        }
        query.orderByDesc(SysLog::getCreateTime);
        Page<SysLog> result = logMapper.selectPage(new Page<>(page, pageSize), query);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }
}
