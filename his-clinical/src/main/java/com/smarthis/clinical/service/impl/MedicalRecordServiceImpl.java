package com.smarthis.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.clinical.converter.MedicalRecordConverter;
import com.smarthis.clinical.dto.request.MedicalRecordCreateRequest;
import com.smarthis.clinical.dto.request.MedicalRecordUpdateRequest;
import com.smarthis.clinical.dto.response.MedicalRecordVo;
import com.smarthis.clinical.entity.MedicalRecord;
import com.smarthis.clinical.enums.RecordStatus;
import com.smarthis.clinical.mapper.MedicalRecordMapper;
import com.smarthis.clinical.service.MedicalRecordService;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordMapper medicalRecordMapper;
    private final BizNoGenerator bizNoGenerator;

    @Override
    public MedicalRecordVo create(MedicalRecordCreateRequest request) {
        MedicalRecord entity = MedicalRecordConverter.toEntity(request);
        entity.setRecordNo(bizNoGenerator.next(BizNoType.CLINICAL_RECORD));
        medicalRecordMapper.insert(entity);
        log.info("Created medical record {} for patient {}", entity.getRecordNo(), entity.getPatientId());
        return MedicalRecordConverter.toVo(entity);
    }

    @Override
    public MedicalRecordVo update(Long id, MedicalRecordUpdateRequest request) {
        MedicalRecord entity = getEntity(id);
        if (RecordStatus.SIGNED.name().equals(entity.getRecordStatus())
                || RecordStatus.ARCHIVED.name().equals(entity.getRecordStatus())) {
            throw new BusinessException(ErrorCode.MEDICAL_RECORD_SIGNED);
        }
        MedicalRecordConverter.applyUpdate(entity, request);
        medicalRecordMapper.updateById(entity);
        return MedicalRecordConverter.toVo(entity);
    }

    @Override
    public MedicalRecordVo getById(Long id) {
        return MedicalRecordConverter.toVo(getEntity(id));
    }

    @Override
    public void sign(Long id) {
        MedicalRecord entity = getEntity(id);
        if (RecordStatus.SIGNED.name().equals(entity.getRecordStatus())) {
            throw new BusinessException(ErrorCode.MEDICAL_RECORD_SIGNED);
        }
        entity.setRecordStatus(RecordStatus.SIGNED.name());
        entity.setSignTime(LocalDateTime.now());
        medicalRecordMapper.updateById(entity);
        log.info("Signed medical record {}", entity.getRecordNo());
    }

    @Override
    public List<MedicalRecordVo> listByPatient(Long patientId) {
        LambdaQueryWrapper<MedicalRecord> query = new LambdaQueryWrapper<>();
        query.eq(MedicalRecord::getPatientId, patientId)
                .eq(MedicalRecord::getDeleted, 0)
                .orderByDesc(MedicalRecord::getCreatedTime);
        return medicalRecordMapper.selectList(query).stream()
                .map(MedicalRecordConverter::toVo).toList();
    }

    @Override
    public List<MedicalRecordVo> listByEncounter(Long encounterId) {
        LambdaQueryWrapper<MedicalRecord> query = new LambdaQueryWrapper<>();
        query.eq(MedicalRecord::getEncounterId, encounterId)
                .eq(MedicalRecord::getDeleted, 0)
                .orderByDesc(MedicalRecord::getCreatedTime);
        return medicalRecordMapper.selectList(query).stream()
                .map(MedicalRecordConverter::toVo).toList();
    }

    private MedicalRecord getEntity(Long id) {
        MedicalRecord entity = medicalRecordMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.MEDICAL_RECORD_NOT_FOUND);
        }
        return entity;
    }
}
