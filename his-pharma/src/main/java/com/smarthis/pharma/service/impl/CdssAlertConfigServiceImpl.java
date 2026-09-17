package com.smarthis.pharma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.converter.CdssAlertConfigConverter;
import com.smarthis.pharma.dto.request.CdssAlertConfigCreateRequest;
import com.smarthis.pharma.dto.request.CdssAlertConfigQueryRequest;
import com.smarthis.pharma.dto.response.CdssAlertConfigVo;
import com.smarthis.pharma.entity.CdssAlertConfig;
import com.smarthis.pharma.mapper.CdssAlertConfigMapper;
import com.smarthis.pharma.service.CdssAlertConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CdssAlertConfigServiceImpl implements CdssAlertConfigService {

    private final CdssAlertConfigMapper cdssAlertConfigMapper;

    @Override
    @Transactional
    public CdssAlertConfigVo create(CdssAlertConfigCreateRequest request) {
        CdssAlertConfig entity = CdssAlertConfigConverter.toEntity(request);
        cdssAlertConfigMapper.insert(entity);
        log.info("CdssAlertConfig created: id={}, alertType={}, alertName={}", entity.getId(), entity.getAlertType(), entity.getAlertName());
        return CdssAlertConfigConverter.toVo(entity);
    }

    @Override
    public CdssAlertConfigVo getById(Long id) {
        CdssAlertConfig config = cdssAlertConfigMapper.selectById(id);
        if (config == null || config.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.CDSS_ALERT_NOT_FOUND);
        }
        return CdssAlertConfigConverter.toVo(config);
    }

    @Override
    public PageResult<CdssAlertConfigVo> query(CdssAlertConfigQueryRequest request) {
        LambdaQueryWrapper<CdssAlertConfig> query = new LambdaQueryWrapper<>();
        query.eq(CdssAlertConfig::getDeleted, 0);
        if (StringUtils.hasText(request.getAlertType())) {
            query.eq(CdssAlertConfig::getAlertType, request.getAlertType());
        }
        if (request.getDeptId() != null) {
            query.eq(CdssAlertConfig::getDeptId, request.getDeptId());
        }
        query.orderByAsc(CdssAlertConfig::getSortOrder);

        Page<CdssAlertConfig> page = request.toPage();
        IPage<CdssAlertConfig> result = cdssAlertConfigMapper.selectPage(page, query);
        List<CdssAlertConfigVo> vos = result.getRecords().stream()
                .map(CdssAlertConfigConverter::toVo)
                .toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public CdssAlertConfigVo update(Long id, CdssAlertConfigCreateRequest request) {
        CdssAlertConfig existing = cdssAlertConfigMapper.selectById(id);
        if (existing == null || existing.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.CDSS_ALERT_NOT_FOUND);
        }
        existing.setAlertType(request.getAlertType());
        existing.setAlertName(request.getAlertName());
        existing.setAlertLevel(request.getAlertLevel());
        existing.setDeptId(request.getDeptId());
        existing.setDescription(request.getDescription());
        existing.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : existing.getSortOrder());
        cdssAlertConfigMapper.updateById(existing);
        return CdssAlertConfigConverter.toVo(existing);
    }

    @Override
    @Transactional
    public CdssAlertConfigVo toggle(Long id) {
        CdssAlertConfig existing = cdssAlertConfigMapper.selectById(id);
        if (existing == null || existing.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.CDSS_ALERT_NOT_FOUND);
        }
        existing.setIsEnabled(existing.getIsEnabled() == 1 ? 0 : 1);
        cdssAlertConfigMapper.updateById(existing);
        log.info("CdssAlertConfig toggled: id={}, isEnabled={}", id, existing.getIsEnabled());
        return CdssAlertConfigConverter.toVo(existing);
    }
}
