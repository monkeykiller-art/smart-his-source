package com.smarthis.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.clinical.converter.DiagnosisConverter;
import com.smarthis.clinical.dto.request.DiagnosisCreateRequest;
import com.smarthis.clinical.dto.response.DiagnosisVo;
import com.smarthis.clinical.entity.Diagnosis;
import com.smarthis.clinical.mapper.DiagnosisMapper;
import com.smarthis.clinical.service.DiagnosisService;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisServiceImpl implements DiagnosisService {

    private final DiagnosisMapper diagnosisMapper;

    @Override
    public DiagnosisVo create(DiagnosisCreateRequest request) {
        Diagnosis entity = DiagnosisConverter.toEntity(request);
        diagnosisMapper.insert(entity);
        log.info("Created diagnosis {} for patient {}", entity.getDiagnosisName(), entity.getPatientId());
        return DiagnosisConverter.toVo(entity);
    }

    @Override
    public DiagnosisVo update(Long id, DiagnosisCreateRequest request) {
        Diagnosis entity = getEntity(id);
        entity.setDoctorId(request.getDoctorId());
        entity.setIcd10Id(request.getIcd10Id());
        entity.setIcdCode(request.getIcdCode());
        entity.setDiagnosisName(request.getDiagnosisName());
        entity.setDiagnosisType(request.getDiagnosisType());
        if (request.getIsPrimary() != null) entity.setIsPrimary(request.getIsPrimary());
        if (request.getIsConfirmed() != null) entity.setIsConfirmed(request.getIsConfirmed());
        if (request.getDiagnosisSeq() != null) entity.setDiagnosisSeq(request.getDiagnosisSeq());
        entity.setOnsetDate(request.getOnsetDate());
        entity.setDiagnosisDesc(request.getDiagnosisDesc());
        diagnosisMapper.updateById(entity);
        return DiagnosisConverter.toVo(entity);
    }

    @Override
    public void delete(Long id) {
        Diagnosis entity = getEntity(id);
        diagnosisMapper.deleteById(entity);
    }

    @Override
    public List<DiagnosisVo> listByEncounter(Long encounterId) {
        LambdaQueryWrapper<Diagnosis> query = new LambdaQueryWrapper<>();
        query.eq(Diagnosis::getEncounterId, encounterId)
                .eq(Diagnosis::getDeleted, 0)
                .orderByAsc(Diagnosis::getDiagnosisSeq);
        return diagnosisMapper.selectList(query).stream()
                .map(DiagnosisConverter::toVo).toList();
    }

    @Override
    public List<DiagnosisVo> listByAdmission(Long admissionId) {
        LambdaQueryWrapper<Diagnosis> query = new LambdaQueryWrapper<>();
        query.eq(Diagnosis::getAdmissionId, admissionId)
                .eq(Diagnosis::getDeleted, 0)
                .orderByAsc(Diagnosis::getDiagnosisSeq);
        return diagnosisMapper.selectList(query).stream()
                .map(DiagnosisConverter::toVo).toList();
    }

    private Diagnosis getEntity(Long id) {
        Diagnosis entity = diagnosisMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return entity;
    }
}
