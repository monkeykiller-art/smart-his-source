package com.smarthis.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.clinical.converter.SkinTestConverter;
import com.smarthis.clinical.dto.request.SkinTestCreateRequest;
import com.smarthis.clinical.dto.request.SkinTestJudgeRequest;
import com.smarthis.clinical.dto.request.SkinTestQueryRequest;
import com.smarthis.clinical.dto.response.SkinTestVo;
import com.smarthis.clinical.entity.SkinTest;
import com.smarthis.clinical.enums.SkinTestResult;
import com.smarthis.clinical.mapper.SkinTestMapper;
import com.smarthis.clinical.service.SkinTestService;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkinTestServiceImpl implements SkinTestService {

    private final SkinTestMapper skinTestMapper;

    @Override
    public SkinTestVo create(SkinTestCreateRequest request) {
        SkinTest entity = SkinTestConverter.toEntity(request);
        entity.setTestResult(SkinTestResult.PENDING.name());
        skinTestMapper.insert(entity);
        log.info("Created skin test for drug {} patient {}", entity.getDrugName(), entity.getPatientId());
        return SkinTestConverter.toVo(entity);
    }

    @Override
    public SkinTestVo getById(Long id) {
        SkinTest entity = skinTestMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return SkinTestConverter.toVo(entity);
    }

    @Override
    public PageResult<SkinTestVo> query(SkinTestQueryRequest request) {
        LambdaQueryWrapper<SkinTest> query = new LambdaQueryWrapper<>();
        query.eq(SkinTest::getDeleted, 0);
        if (request.getPatientId() != null) {
            query.eq(SkinTest::getPatientId, request.getPatientId());
        }
        if (request.getAdmissionId() != null) {
            query.eq(SkinTest::getAdmissionId, request.getAdmissionId());
        }
        if (StringUtils.hasText(request.getTestResult())) {
            query.eq(SkinTest::getTestResult, request.getTestResult());
        }
        query.orderByDesc(SkinTest::getCreatedTime);
        Page<SkinTest> page = request.toPage();
        IPage<SkinTest> result = skinTestMapper.selectPage(page, query);
        List<SkinTestVo> vos = result.getRecords().stream()
                .map(SkinTestConverter::toVo).toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    public SkinTestVo judge(Long id, SkinTestJudgeRequest request) {
        SkinTest entity = skinTestMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        entity.setTestResult(request.getTestResult());
        entity.setJudgeTime(LocalDateTime.now());
        entity.setJudgeNurseId(request.getJudgeNurseId());
        skinTestMapper.updateById(entity);
        log.info("Skin test {} judged as {}", id, request.getTestResult());
        return SkinTestConverter.toVo(entity);
    }
}
