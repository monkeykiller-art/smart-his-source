package com.smarthis.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.patient.dto.request.SurgeryApplyRequest;
import com.smarthis.patient.dto.request.SurgeryCompleteRequest;
import com.smarthis.patient.dto.request.SurgeryScheduleRequest;
import com.smarthis.patient.entity.Admission;
import com.smarthis.patient.entity.SurgeryCase;
import com.smarthis.patient.mapper.AdmissionMapper;
import com.smarthis.patient.mapper.SurgeryCaseMapper;
import com.smarthis.patient.service.SurgeryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SurgeryServiceImpl implements SurgeryService {
    private final SurgeryCaseMapper surgeryCaseMapper;
    private final AdmissionMapper admissionMapper;
    private final BizNoGenerator bizNoGenerator;

    @Override
    @Transactional
    public SurgeryCase apply(SurgeryApplyRequest request) {
        Admission admission = admissionMapper.selectById(request.getAdmissionId());
        if (admission == null || admission.getDeleted() != 0) throw new BusinessException(ErrorCode.ADMISSION_NOT_FOUND);
        if (!"ADMITTED".equals(admission.getAdmissionStatus())) throw new BusinessException(ErrorCode.ADMISSION_STATUS_INVALID);
        SurgeryCase surgery = new SurgeryCase();
        surgery.setSurgeryNo(bizNoGenerator.next(BizNoType.SURGERY));
        surgery.setAdmissionId(admission.getId());
        surgery.setPatientId(admission.getPatientId());
        surgery.setSurgeryName(request.getSurgeryName());
        surgery.setPlannedStartTime(request.getPlannedStartTime());
        surgery.setSurgeonId(request.getSurgeonId());
        surgery.setSurgeryStatus("APPLIED");
        surgeryCaseMapper.insert(surgery);
        return surgery;
    }

    @Override
    @Transactional
    public SurgeryCase schedule(Long id, SurgeryScheduleRequest request) {
        SurgeryCase surgery = requireStatus(id, "APPLIED");
        surgery.setPlannedStartTime(request.getPlannedStartTime());
        surgery.setOperatingRoom(request.getOperatingRoom());
        surgery.setAnesthetistId(request.getAnesthetistId());
        surgery.setAnesthesiaMethod(request.getAnesthesiaMethod());
        surgery.setSurgeryStatus("SCHEDULED");
        surgeryCaseMapper.updateById(surgery);
        return surgery;
    }

    @Override
    @Transactional
    public SurgeryCase start(Long id) {
        SurgeryCase surgery = requireStatus(id, "SCHEDULED");
        surgery.setSurgeryStatus("IN_PROGRESS");
        surgeryCaseMapper.updateById(surgery);
        return surgery;
    }

    @Override
    @Transactional
    public SurgeryCase complete(Long id, SurgeryCompleteRequest request) {
        SurgeryCase surgery = requireStatus(id, "IN_PROGRESS");
        surgery.setOperativeNote(request.getOperativeNote());
        surgery.setSurgeryStatus("COMPLETED");
        surgeryCaseMapper.updateById(surgery);
        return surgery;
    }

    @Override
    public List<SurgeryCase> list(Long admissionId) {
        return surgeryCaseMapper.selectList(new LambdaQueryWrapper<SurgeryCase>()
                .eq(SurgeryCase::getAdmissionId, admissionId)
                .eq(SurgeryCase::getDeleted, 0)
                .orderByDesc(SurgeryCase::getPlannedStartTime));
    }

    private SurgeryCase requireStatus(Long id, String status) {
        SurgeryCase surgery = surgeryCaseMapper.selectById(id);
        if (surgery == null || (surgery.getDeleted() != null && surgery.getDeleted() != 0)) throw new BusinessException(ErrorCode.NOT_FOUND);
        if (!status.equals(surgery.getSurgeryStatus())) throw new BusinessException(ErrorCode.ADMISSION_STATUS_INVALID);
        return surgery;
    }
}
