package com.y1fel.backend.operationlog.service;

import com.y1fel.backend.operationlog.entity.SysLog;
import com.y1fel.backend.operationlog.mapper.SysLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OperationLogService {
    private final SysLogMapper sysLogMapper;

    public void record(String operator, String type, String module, String content, String ip) {
        SysLog log = new SysLog();
        log.setOperator(operator);
        log.setType(type);
        log.setModule(module);
        log.setContent(content);
        log.setIp(ip);
        log.setCreateTime(LocalDateTime.now());
        sysLogMapper.insert(log);
    }
}
