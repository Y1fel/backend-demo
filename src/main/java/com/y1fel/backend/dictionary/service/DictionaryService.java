package com.y1fel.backend.dictionary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.y1fel.backend.common.exception.BizException;
import com.y1fel.backend.common.response.PageResult;
import com.y1fel.backend.dictionary.dto.DictionarySaveRequest;
import com.y1fel.backend.dictionary.entity.SysDict;
import com.y1fel.backend.dictionary.mapper.SysDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DictionaryService {
    private final SysDictMapper dictMapper;

    public PageResult<SysDict> page(int page, int pageSize, String keyword) {
        LambdaQueryWrapper<SysDict> query = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) query.and(q -> q.like(SysDict::getDictType, keyword)
                .or().like(SysDict::getDictName, keyword).or().like(SysDict::getDictCode, keyword));
        query.orderByAsc(SysDict::getSort).orderByDesc(SysDict::getId);
        Page<SysDict> result = dictMapper.selectPage(new Page<>(page, pageSize), query);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }
    public void add(DictionarySaveRequest request) {
        checkUnique(request, null);
        SysDict dict = new SysDict(); copy(request, dict); dictMapper.insert(dict);
    }
    public void update(Long id, DictionarySaveRequest request) {
        SysDict dict = dictMapper.selectById(id);
        if (dict == null) throw new BizException("字典不存在");
        checkUnique(request, id); copy(request, dict); dictMapper.updateById(dict);
    }
    public void delete(Long id) {
        if (dictMapper.selectById(id) == null) throw new BizException("字典不存在");
        dictMapper.deleteById(id);
    }
    private void checkUnique(DictionarySaveRequest request, Long excludedId) {
        LambdaQueryWrapper<SysDict> query = new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictType, request.getDictType()).eq(SysDict::getDictCode, request.getDictCode());
        if (excludedId != null) query.ne(SysDict::getId, excludedId);
        if (dictMapper.selectCount(query) > 0) throw new BizException("同一字典类型下编码已存在");
    }
    private void copy(DictionarySaveRequest request, SysDict dict) {
        dict.setDictType(request.getDictType()); dict.setDictName(request.getDictName());
        dict.setDictCode(request.getDictCode()); dict.setSort(request.getSort() == null ? 0 : request.getSort());
        dict.setRemark(request.getRemark());
    }
}
