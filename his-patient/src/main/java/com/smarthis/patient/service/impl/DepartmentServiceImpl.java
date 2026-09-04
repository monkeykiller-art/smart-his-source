package com.smarthis.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.patient.dto.response.DepartmentVo;
import com.smarthis.patient.entity.Department;
import com.smarthis.patient.mapper.DepartmentMapper;
import com.smarthis.patient.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    @Override
    @Cacheable(value = "deptList", unless = "#result == null || #result.isEmpty()")
    public List<DepartmentVo> listAll() {
        LambdaQueryWrapper<Department> query = new LambdaQueryWrapper<>();
        query.eq(Department::getDeleted, 0)
                .eq(Department::getDeptStatus, "ACTIVE")
                .orderByAsc(Department::getSortOrder);
        return departmentMapper.selectList(query).stream()
                .map(this::toVo)
                .toList();
    }

    @Override
    public DepartmentVo getById(Long id) {
        Department dept = departmentMapper.selectById(id);
        if (dept == null || dept.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }
        return toVo(dept);
    }

    private DepartmentVo toVo(Department d) {
        DepartmentVo vo = new DepartmentVo();
        vo.setId(d.getId());
        vo.setDeptCode(d.getDeptCode());
        vo.setDeptName(d.getDeptName());
        vo.setDeptType(d.getDeptType());
        vo.setParentId(d.getParentId());
        vo.setSortOrder(d.getSortOrder());
        vo.setDeptStatus(d.getDeptStatus());
        vo.setDescription(d.getDescription());
        return vo;
    }
}
