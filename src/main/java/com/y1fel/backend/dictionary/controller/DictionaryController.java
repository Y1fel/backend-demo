package com.y1fel.backend.dictionary.controller;

import com.y1fel.backend.common.response.*;
import com.y1fel.backend.common.util.UserContext;
import com.y1fel.backend.dictionary.dto.DictionarySaveRequest;
import com.y1fel.backend.dictionary.entity.SysDict;
import com.y1fel.backend.dictionary.service.DictionaryService;
import com.y1fel.backend.operationlog.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class DictionaryController {
    private final DictionaryService dictionaryService;
    private final OperationLogService logService;
    @GetMapping
    public Result<PageResult<SysDict>> page(@RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="10") int pageSize, @RequestParam(required=false) String keyword) {
        return Result.success(dictionaryService.page(page, pageSize, keyword));
    }
    @PostMapping
    public Result<Void> add(@Valid @RequestBody DictionarySaveRequest body, HttpServletRequest request) {
        dictionaryService.add(body); log("新增字典：" + body.getDictType() + "/" + body.getDictCode(), request); return Result.success();
    }
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DictionarySaveRequest body, HttpServletRequest request) {
        dictionaryService.update(id, body); log("修改字典 id=" + id, request); return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        dictionaryService.delete(id); log("删除字典 id=" + id, request); return Result.success();
    }
    private void log(String content, HttpServletRequest request) {
        UserContext.LoginUser user = UserContext.get();
        logService.record(user == null ? "unknown" : user.realName(), "operation", "数据字典", content, request.getRemoteAddr());
    }
}
