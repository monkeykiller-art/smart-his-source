package com.smarthis.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.patient.converter.PatientConverter;
import com.smarthis.patient.dto.request.PatientCreateRequest;
import com.smarthis.patient.dto.request.PatientQueryRequest;
import com.smarthis.patient.dto.request.PatientUpdateRequest;
import com.smarthis.patient.dto.response.PatientVo;
import com.smarthis.patient.entity.Patient;
import com.smarthis.patient.entity.PatientIdentifier;
import com.smarthis.patient.mapper.PatientIdentifierMapper;
import com.smarthis.patient.mapper.PatientMapper;
import com.smarthis.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientMapper patientMapper;
    private final PatientIdentifierMapper identifierMapper;
    private final BizNoGenerator bizNoGenerator;

    @Override
    @Transactional
    public PatientVo create(PatientCreateRequest request) {
        LambdaQueryWrapper<PatientIdentifier> existingIdentifier = new LambdaQueryWrapper<>();
        existingIdentifier.eq(PatientIdentifier::getIdType, request.getIdType())
                .eq(PatientIdentifier::getIdNo, request.getIdNo())
                .eq(PatientIdentifier::getDeleted, 0);
        if (identifierMapper.selectCount(existingIdentifier) > 0) {
            throw new BusinessException(ErrorCode.PATIENT_DUPLICATE);
        }

        Patient patient = PatientConverter.toEntity(request);
        patient.setEmpiNo(bizNoGenerator.next(BizNoType.REGISTRATION).replace("MZ", "EM"));
        patient.setPatientType("NORMAL");
        patient.setPatientStatus("ACTIVE");
        patient.setSource("SELF");
        patientMapper.insert(patient);

        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setPatientId(patient.getId());
        identifier.setIdType(request.getIdType());
        identifier.setIdNo(request.getIdNo());
        identifier.setIsPrimary(1);
        identifierMapper.insert(identifier);

        log.info("Patient created: id={}, empiNo={}, name={}", patient.getId(), patient.getEmpiNo(), patient.getName());
        return PatientConverter.toVo(patient);
    }

    @Override
    public PatientVo getById(Long id) {
        Patient patient = patientMapper.selectById(id);
        if (patient == null || patient.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }
        return PatientConverter.toVo(patient);
    }

    @Override
    public PatientVo getByIdNo(String idType, String idNo) {
        LambdaQueryWrapper<PatientIdentifier> query = new LambdaQueryWrapper<>();
        query.eq(PatientIdentifier::getIdType, idType)
                .eq(PatientIdentifier::getIdNo, idNo)
                .eq(PatientIdentifier::getDeleted, 0)
                .orderByDesc(PatientIdentifier::getIsPrimary);
        List<PatientIdentifier> identifiers = identifierMapper.selectList(query);
        if (identifiers.isEmpty()) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }
        Patient patient = patientMapper.selectById(identifiers.get(0).getPatientId());
        if (patient == null || patient.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }
        return PatientConverter.toVo(patient);
    }

    @Override
    @Transactional
    public PatientVo update(Long id, PatientUpdateRequest request) {
        Patient patient = patientMapper.selectById(id);
        if (patient == null || patient.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }
        if (StringUtils.hasText(request.getName())) {
            patient.setName(request.getName());
        }
        if (request.getGender() != null) {
            patient.setGender(request.getGender());
        }
        if (request.getBirthDate() != null) {
            patient.setBirthDate(request.getBirthDate());
        }
        if (StringUtils.hasText(request.getNationality())) {
            patient.setNationality(request.getNationality());
        }
        if (StringUtils.hasText(request.getNation())) {
            patient.setNation(request.getNation());
        }
        if (StringUtils.hasText(request.getMaritalStatus())) {
            patient.setMaritalStatus(request.getMaritalStatus());
        }
        if (StringUtils.hasText(request.getOccupation())) {
            patient.setOccupation(request.getOccupation());
        }
        if (StringUtils.hasText(request.getPhone())) {
            patient.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getPhoneBackup())) {
            patient.setPhoneBackup(request.getPhoneBackup());
        }
        if (StringUtils.hasText(request.getAddress())) {
            patient.setAddress(request.getAddress());
        }
        if (StringUtils.hasText(request.getBloodType())) {
            patient.setBloodType(request.getBloodType());
        }
        if (StringUtils.hasText(request.getAllergyHistory())) {
            patient.setAllergyHistory(request.getAllergyHistory());
        }
        if (StringUtils.hasText(request.getInsuranceType())) {
            patient.setInsuranceType(request.getInsuranceType());
        }
        if (StringUtils.hasText(request.getInsuranceNo())) {
            patient.setInsuranceNo(request.getInsuranceNo());
        }
        patientMapper.updateById(patient);
        log.info("Patient updated: id={}", patient.getId());
        return PatientConverter.toVo(patient);
    }

    @Override
    public PageResult<PatientVo> query(PatientQueryRequest request) {
        LambdaQueryWrapper<Patient> query = new LambdaQueryWrapper<>();
        query.eq(Patient::getDeleted, 0);
        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            String literalKeyword = keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            query.and(w -> {
                w.like(Patient::getName, literalKeyword)
                        .or().like(Patient::getNamePinyin, literalKeyword)
                        .or().like(Patient::getPhone, literalKeyword)
                        .or().like(Patient::getEmpiNo, literalKeyword)
                        .or().eq(Patient::getIdNo, keyword);
                if (keyword.matches("[0-9]{1,19}")) {
                    BigInteger patientId = new BigInteger(keyword);
                    if (patientId.signum() > 0 && patientId.bitLength() < 64) {
                        w.or().eq(Patient::getId, patientId.longValue());
                    }
                }
            });
        }
        if (StringUtils.hasText(request.getIdType())) {
            query.eq(Patient::getIdType, request.getIdType());
        }
        if (StringUtils.hasText(request.getIdNo())) {
            query.eq(Patient::getIdNo, request.getIdNo());
        }
        if (StringUtils.hasText(request.getPhone())) {
            query.like(Patient::getPhone, request.getPhone());
        }
        query.orderByDesc(Patient::getCreatedTime);

        Page<Patient> page = request.toPage();
        IPage<Patient> result = patientMapper.selectPage(page, query);
        List<PatientVo> vos = result.getRecords().stream()
                .map(PatientConverter::toVo)
                .toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }
}
