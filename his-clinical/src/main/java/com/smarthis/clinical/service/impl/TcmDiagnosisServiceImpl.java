package com.smarthis.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.clinical.converter.TcmDiagnosisConverter;
import com.smarthis.clinical.dto.request.TcmDiagnosisQueryRequest;
import com.smarthis.clinical.dto.response.TcmDiagnosisVo;
import com.smarthis.clinical.entity.TcmDiagnosis;
import com.smarthis.clinical.mapper.TcmDiagnosisMapper;
import com.smarthis.clinical.service.TcmDiagnosisService;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcmDiagnosisServiceImpl implements TcmDiagnosisService {

    private final TcmDiagnosisMapper tcmDiagnosisMapper;

    @Override
    public TcmDiagnosisVo getById(Long id) {
        TcmDiagnosis entity = tcmDiagnosisMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return TcmDiagnosisConverter.toVo(entity);
    }

    @Override
    public PageResult<TcmDiagnosisVo> query(TcmDiagnosisQueryRequest request) {
        LambdaQueryWrapper<TcmDiagnosis> query = new LambdaQueryWrapper<>();
        query.eq(TcmDiagnosis::getDeleted, 0);
        if (StringUtils.hasText(request.getKeyword())) {
            String kw = request.getKeyword();
            query.and(w -> w.like(TcmDiagnosis::getTcmName, kw)
                    .or().like(TcmDiagnosis::getNamePinyin, kw)
                    .or().like(TcmDiagnosis::getTcmCode, kw));
        }
        if (StringUtils.hasText(request.getCategory())) {
            query.eq(TcmDiagnosis::getCategory, request.getCategory());
        }
        if (request.getDictStatus() != null) {
            query.eq(TcmDiagnosis::getDictStatus, request.getDictStatus());
        }
        query.orderByAsc(TcmDiagnosis::getSortOrder);
        Page<TcmDiagnosis> page = request.toPage();
        IPage<TcmDiagnosis> result = tcmDiagnosisMapper.selectPage(page, query);
        List<TcmDiagnosisVo> vos = result.getRecords().stream().map(TcmDiagnosisConverter::toVo).toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }
}
