package com.y1fel.backend.operationlog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_log")
public class SysLog {
    private Long id;
    private String operator;
    private String type;
    private String module;
    private String content;
    private String ip;
    private LocalDateTime createTime;
}
