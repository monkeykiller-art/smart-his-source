package com.smarthis.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.resource.converter.WardConverter;
import com.smarthis.resource.dto.request.WardCreateRequest;
import com.smarthis.resource.dto.request.WardQueryRequest;
import com.smarthis.resource.dto.request.WardUpdateRequest;
import com.smarthis.resource.dto.response.WardVo;
import com.smarthis.resource.entity.Ward;
import com.smarthis.resource.mapper.WardMapper;
import com.smarthis.resource.service.WardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WardServiceImpl implements WardService {

    private final WardMapper wardMapper;

    @Override
    @Transactional
    public WardVo create(WardCreateRequest request) {
        Ward ward = WardConverter.toEntity(request);
        wardMapper.insert(ward);
        return WardConverter.toVo(ward);
    }

    @Override
    @Transactional
    public WardVo update(Long id, WardUpdateRequest request) {
        Ward ward = requireWard(id);
        WardConverter.applyUpdate(request, ward);
        wardMapper.updateById(ward);
        return WardConverter.toVo(ward);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Ward ward = requireWard(id);
        if ("ACTIVE".equals(ward.getWardStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "Cannot delete an active ward");
        }
        ward.setDeleted(1);
        wardMapper.updateById(ward);
    }

    @Override
    public WardVo getById(Long id) {
        return WardConverter.toVo(requireWard(id));
    }

    @Override
    public PageResult<WardVo> list(WardQueryRequest request) {
        Page<Ward> page = request.toPage();
        LambdaQueryWrapper<Ward> query = new LambdaQueryWrapper<>();
        query.eq(Ward::getDeleted, 0);
        if (StringUtils.hasText(request.getWardName())) {
            query.like(Ward::getWardName, request.getWardName());
        }
        if (request.getDeptId() != null) {
            query.eq(Ward::getDeptId, request.getDeptId());
        }
        if (StringUtils.hasText(request.getWardType())) {
            query.eq(Ward::getWardType, request.getWardType());
        }
        if (StringUtils.hasText(request.getWardStatus())) {
            query.eq(Ward::getWardStatus, request.getWardStatus());
        }
        query.orderByAsc(Ward::getWardCode);
        Page<Ward> result = wardMapper.selectPage(page, query);
        List<WardVo> records = result.getRecords().stream().map(WardConverter::toVo).toList();
        return new PageResult<>(records, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    public List<WardVo> listAll() {
        LambdaQueryWrapper<Ward> query = new LambdaQueryWrapper<>();
        query.eq(Ward::getDeleted, 0)
                .eq(Ward::getWardStatus, "ACTIVE")
                .orderByAsc(Ward::getWardCode);
        return wardMapper.selectList(query).stream().map(WardConverter::toVo).toList();
    }

    private Ward requireWard(Long id) {
        Ward ward = wardMapper.selectById(id);
        if (ward == null || ward.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.WARD_NOT_FOUND);
        }
        return ward;
    }
}
