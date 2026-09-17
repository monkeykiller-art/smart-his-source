package com.smarthis.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.emergency.dto.request.*;
import com.smarthis.emergency.entity.ObservationRecord;
import com.smarthis.emergency.entity.ResuscitationRecord;
import com.smarthis.emergency.mapper.ObservationRecordMapper;
import com.smarthis.emergency.mapper.ResuscitationRecordMapper;
import com.smarthis.emergency.service.EmergencyCareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmergencyCareServiceImpl implements EmergencyCareService {
    private final ResuscitationRecordMapper resuscitationMapper;
    private final ObservationRecordMapper observationMapper;

    @Override @Transactional
    public ResuscitationRecord startResuscitation(ResuscitationCreateRequest request) {
        ResuscitationRecord record = new ResuscitationRecord();
        record.setTriageId(request.getTriageId()); record.setPatientId(request.getPatientId());
        record.setProcedures(request.getProcedures()); record.setMedications(request.getMedications());
        record.setStartTime(LocalDateTime.now()); record.setResuscitationStatus("IN_PROGRESS");
        resuscitationMapper.insert(record); return record;
    }

    @Override @Transactional
    public ResuscitationRecord completeResuscitation(Long id, ResuscitationCompleteRequest request) {
        ResuscitationRecord record = resuscitationMapper.selectById(id);
        if (record == null || record.getDeleted() != 0) throw new BusinessException(ErrorCode.RESUSCITATION_NOT_FOUND);
        if (!"IN_PROGRESS".equals(record.getResuscitationStatus())) throw new BusinessException(ErrorCode.BAD_REQUEST);
        record.setOutcome(request.getOutcome()); record.setOutcomeSummary(request.getOutcomeSummary());
        record.setEndTime(LocalDateTime.now()); record.setResuscitationStatus("COMPLETED");
        resuscitationMapper.updateById(record); return record;
    }

    @Override @Transactional
    public ObservationRecord admitObservation(ObservationCreateRequest request) {
        ObservationRecord record = new ObservationRecord();
        record.setTriageId(request.getTriageId()); record.setPatientId(request.getPatientId());
        record.setBedNo(request.getBedNo()); record.setDiagnosis(request.getDiagnosis());
        record.setTreatmentPlan(request.getTreatmentPlan()); record.setAdmitTime(LocalDateTime.now());
        record.setObservationStatus("ADMITTED"); observationMapper.insert(record); return record;
    }

    @Override @Transactional
    public ObservationRecord dischargeObservation(Long id, ObservationDischargeRequest request) {
        ObservationRecord record = observationMapper.selectById(id);
        if (record == null || record.getDeleted() != 0) throw new BusinessException(ErrorCode.NOT_FOUND);
        if (!"ADMITTED".equals(record.getObservationStatus())) throw new BusinessException(ErrorCode.BAD_REQUEST);
        record.setDischargeSummary(request.getDischargeSummary()); record.setDischargeTime(LocalDateTime.now());
        record.setObservationStatus("DISCHARGED"); observationMapper.updateById(record); return record;
    }

    @Override public List<ResuscitationRecord> resuscitations(Long patientId) {
        return resuscitationMapper.selectList(new LambdaQueryWrapper<ResuscitationRecord>()
                .eq(patientId != null, ResuscitationRecord::getPatientId, patientId)
                .eq(ResuscitationRecord::getDeleted, 0).orderByDesc(ResuscitationRecord::getStartTime));
    }
    @Override public List<ObservationRecord> observations(Long patientId) {
        return observationMapper.selectList(new LambdaQueryWrapper<ObservationRecord>()
                .eq(patientId != null, ObservationRecord::getPatientId, patientId)
                .eq(ObservationRecord::getDeleted, 0).orderByDesc(ObservationRecord::getAdmitTime));
    }
}
