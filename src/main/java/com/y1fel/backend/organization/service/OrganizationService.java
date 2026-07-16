package com.y1fel.backend.organization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.y1fel.backend.common.exception.BizException;
import com.y1fel.backend.organization.dto.OrganizationSaveRequest;
import com.y1fel.backend.organization.entity.SysOrg;
import com.y1fel.backend.organization.mapper.SysOrgMapper;
import com.y1fel.backend.organization.vo.OrganizationVo;
import com.y1fel.backend.user.entity.SysUser;
import com.y1fel.backend.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final SysOrgMapper orgMapper;
    private final SysUserMapper userMapper;

    public List<OrganizationVo> tree(String keyword) {
        List<SysOrg> all = orgMapper.selectList(new LambdaQueryWrapper<SysOrg>()
                .orderByAsc(SysOrg::getSort).orderByAsc(SysOrg::getId));
        List<OrganizationVo> tree = buildTree(all);
        if (!StringUtils.hasText(keyword)) return tree;
        return tree.stream().map(node -> filter(node, keyword.trim())).filter(Objects::nonNull).toList();
    }

    public void add(OrganizationSaveRequest request) {
        checkCode(request.getCode(), null);
        checkParent(request.getParentId());
        SysOrg org = new SysOrg();
        copy(request, org);
        orgMapper.insert(org);
    }

    public void update(Long id, OrganizationSaveRequest request) {
        SysOrg org = orgMapper.selectById(id);
        if (org == null) throw new BizException("组织不存在");
        checkCode(request.getCode(), id);
        Long parentId = request.getParentId();
        if (Objects.equals(parentId, id)) throw new BizException("不能将组织移动到自身下");
        if (parentId != null && descendantIds(id).contains(parentId)) {
            throw new BizException("不能将组织移动到自身子节点下");
        }
        checkParent(parentId);
        copy(request, org);
        orgMapper.updateById(org);
    }

    public void delete(Long id) {
        if (orgMapper.selectById(id) == null) throw new BizException("组织不存在");
        if (orgMapper.selectCount(new LambdaQueryWrapper<SysOrg>().eq(SysOrg::getParentId, id)) > 0) {
            throw new BizException("存在子组织，不允许删除");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getOrgId, id)) > 0) {
            throw new BizException("组织下仍有用户，不允许删除");
        }
        orgMapper.deleteById(id);
    }

    private void copy(OrganizationSaveRequest request, SysOrg org) {
        org.setParentId(request.getParentId());
        org.setName(request.getName());
        org.setCode(request.getCode());
        org.setSort(request.getSort() == null ? 0 : request.getSort());
    }

    private void checkCode(String code, Long excludedId) {
        LambdaQueryWrapper<SysOrg> query = new LambdaQueryWrapper<SysOrg>().eq(SysOrg::getCode, code);
        if (excludedId != null) query.ne(SysOrg::getId, excludedId);
        if (orgMapper.selectCount(query) > 0) throw new BizException("组织编码已存在");
    }

    private void checkParent(Long parentId) {
        if (parentId != null && orgMapper.selectById(parentId) == null) throw new BizException("上级组织不存在");
    }

    private Set<Long> descendantIds(Long id) {
        Map<Long, List<Long>> children = new HashMap<>();
        for (SysOrg org : orgMapper.selectList(null)) {
            if (org.getParentId() != null) children.computeIfAbsent(org.getParentId(), k -> new ArrayList<>()).add(org.getId());
        }
        Set<Long> result = new HashSet<>();
        collect(id, children, result);
        return result;
    }

    private void collect(Long id, Map<Long, List<Long>> children, Set<Long> result) {
        for (Long child : children.getOrDefault(id, List.of())) {
            if (result.add(child)) collect(child, children, result);
        }
    }

    private List<OrganizationVo> buildTree(List<SysOrg> all) {
        Map<Long, OrganizationVo> map = new HashMap<>();
        for (SysOrg org : all) {
            OrganizationVo vo = new OrganizationVo();
            BeanUtils.copyProperties(org, vo);
            map.put(org.getId(), vo);
        }
        List<OrganizationVo> roots = new ArrayList<>();
        for (SysOrg org : all) {
            OrganizationVo vo = map.get(org.getId());
            OrganizationVo parent = map.get(org.getParentId());
            if (parent == null) roots.add(vo); else parent.getChildren().add(vo);
        }
        return roots;
    }

    private OrganizationVo filter(OrganizationVo node, String keyword) {
        List<OrganizationVo> children = node.getChildren().stream()
                .map(child -> filter(child, keyword)).filter(Objects::nonNull).toList();
        boolean matches = node.getName().contains(keyword) || node.getCode().contains(keyword);
        if (!matches && children.isEmpty()) return null;
        OrganizationVo copy = new OrganizationVo();
        BeanUtils.copyProperties(node, copy, "children");
        copy.setChildren(matches ? node.getChildren() : new ArrayList<>(children));
        return copy;
    }
}
