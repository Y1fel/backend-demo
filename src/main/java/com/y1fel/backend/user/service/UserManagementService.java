package com.y1fel.backend.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.y1fel.backend.common.exception.BizException;
import com.y1fel.backend.common.response.PageResult;
import com.y1fel.backend.organization.entity.SysOrg;
import com.y1fel.backend.organization.mapper.SysOrgMapper;
import com.y1fel.backend.user.dto.UserSaveRequest;
import com.y1fel.backend.user.entity.SysUser;
import com.y1fel.backend.user.mapper.SysUserMapper;
import com.y1fel.backend.user.vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private final SysUserMapper userMapper;
    private final SysOrgMapper orgMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResult<UserVo> page(int page, int pageSize, String keyword) {
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) query.and(q -> q.like(SysUser::getUsername, keyword)
                .or().like(SysUser::getRealName, keyword).or().like(SysUser::getPhone, keyword));
        query.orderByDesc(SysUser::getId);
        Page<SysUser> result = userMapper.selectPage(new Page<>(page, pageSize), query);
        Set<Long> orgIds = result.getRecords().stream().map(SysUser::getOrgId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> names = orgIds.isEmpty() ? Map.of() : orgMapper.selectBatchIds(orgIds).stream()
                .collect(Collectors.toMap(SysOrg::getId, SysOrg::getName));
        List<UserVo> list = result.getRecords().stream().map(user -> toVo(user, names.get(user.getOrgId()))).toList();
        return new PageResult<>(list, result.getTotal());
    }

    public void add(UserSaveRequest request) {
        if (!StringUtils.hasText(request.getPassword())) throw new BizException("密码不能为空");
        checkUsername(request.getUsername());
        checkOrg(request.getOrgId());
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        updateFields(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("enabled");
        user.setDeleted(0);
        userMapper.insert(user);
    }

    public void update(Long id, UserSaveRequest request) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BizException("用户不存在");
        if (!Objects.equals(user.getUsername(), request.getUsername())) throw new BizException("用户名不允许修改");
        checkOrg(request.getOrgId());
        updateFields(user, request);
        if (StringUtils.hasText(request.getStatus())) {
            if (!Set.of("enabled", "disabled").contains(request.getStatus())) throw new BizException("状态只能是 enabled 或 disabled");
            user.setStatus(request.getStatus());
        }
        userMapper.updateById(user);
    }

    public void delete(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BizException("用户不存在");
        if ("admin".equals(user.getUsername())) throw new BizException("不能删除系统管理员账号");
        userMapper.deleteById(id);
    }

    public void batchDelete(List<Long> ids) { ids.forEach(this::delete); }

    private void updateFields(SysUser user, UserSaveRequest request) {
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setOrgId(request.getOrgId());
        user.setRole(request.getRole());
    }
    private void checkUsername(String username) {
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)) > 0)
            throw new BizException("用户名已存在");
    }
    private void checkOrg(Long id) { if (orgMapper.selectById(id) == null) throw new BizException("所属组织不存在"); }
    private UserVo toVo(SysUser user, String orgName) {
        UserVo vo = new UserVo();
        vo.setId(user.getId()); vo.setUsername(user.getUsername()); vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone()); vo.setOrgId(user.getOrgId()); vo.setOrgName(orgName);
        vo.setRole(user.getRole()); vo.setStatus(user.getStatus()); vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
