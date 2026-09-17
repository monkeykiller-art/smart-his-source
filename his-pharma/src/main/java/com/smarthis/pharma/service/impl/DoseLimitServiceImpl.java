package com.smarthis.pharma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.converter.DoseLimitConverter;
import com.smarthis.pharma.dto.request.DoseLimitCreateRequest;
import com.smarthis.pharma.dto.request.DoseLimitQueryRequest;
import com.smarthis.pharma.dto.response.DoseLimitVo;
import com.smarthis.pharma.entity.DoseLimit;
import com.smarthis.pharma.mapper.DoseLimitMapper;
import com.smarthis.pharma.service.DoseLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoseLimitServiceImpl implements DoseLimitService {

    private final DoseLimitMapper doseLimitMapper;

    @Override
    @Transactional
    public DoseLimitVo create(DoseLimitCreateRequest request) {
        DoseLimit entity = DoseLimitConverter.toEntity(request);
        doseLimitMapper.insert(entity);
        log.info("DoseLimit created: id={}, drugCode={}", entity.getId(), entity.getDrugCode());
        return DoseLimitConverter.toVo(entity);
    }

    @Override
    public PageResult<DoseLimitVo> query(DoseLimitQueryRequest request) {
        LambdaQueryWrapper<DoseLimit> query = new LambdaQueryWrapper<>();
        query.eq(DoseLimit::getDeleted, 0)
                .eq(DoseLimit::getIsActive, 1);
        if (StringUtils.hasText(request.getDrugCode())) {
            query.eq(DoseLimit::getDrugCode, request.getDrugCode());
        }
        if (StringUtils.hasText(request.getPatientType())) {
            query.eq(DoseLimit::getPatientType, request.getPatientType());
        }
        query.orderByDesc(DoseLimit::getCreatedTime);

        Page<DoseLimit> page = request.toPage();
        IPage<DoseLimit> result = doseLimitMapper.selectPage(page, query);
        List<DoseLimitVo> vos = result.getRecords().stream()
                .map(DoseLimitConverter::toVo)
                .toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public DoseLimitVo update(Long id, DoseLimitCreateRequest request) {
        DoseLimit existing = doseLimitMapper.selectById(id);
        if (existing == null || existing.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        existing.setDrugCode(request.getDrugCode());
        existing.setPatientType(request.getPatientType());
        existing.setAgeMin(request.getAgeMin());
        existing.setAgeMax(request.getAgeMax());
        existing.setRoute(request.getRoute());
        existing.setMaxSingleDose(request.getMaxSingleDose());
        existing.setMaxSingleUnit(request.getMaxSingleUnit());
        existing.setMaxDailyDose(request.getMaxDailyDose());
        existing.setMaxDailyUnit(request.getMaxDailyUnit());
        existing.setMaxFreqPerDay(request.getMaxFreqPerDay());
        existing.setDescription(request.getDescription());
        doseLimitMapper.updateById(existing);
        return DoseLimitConverter.toVo(existing);
    }
}
