package com.smarthis.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.emergency.dto.request.EmergencyTriageCreateRequest;
import com.smarthis.emergency.entity.EmergencyTriage;
import com.smarthis.emergency.mapper.EmergencyTriageMapper;
import com.smarthis.emergency.service.EmergencyTriageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmergencyTriageServiceImpl implements EmergencyTriageService {
    private static final Set<String> STATUSES = Set.of("WAITING", "IN_TREATMENT", "OBSERVATION", "COMPLETED", "CANCELLED");
    private final EmergencyTriageMapper mapper;
    private final BizNoGenerator bizNoGenerator;

    @Override
    @Transactional
    public EmergencyTriage create(EmergencyTriageCreateRequest request) {
        EmergencyTriage triage = new EmergencyTriage();
        triage.setTriageNo(bizNoGenerator.next(BizNoType.EMERGENCY));
        triage.setPatientId(request.getPatientId());
        triage.setTriageLevel(request.getTriageLevel());
        triage.setTriageTime(LocalDateTime.now());
        triage.setChiefComplaint(request.getChiefComplaint());
        triage.setVitalSigns(request.getVitalSigns());
        triage.setTriageNurseId(request.getTriageNurseId());
        triage.setTriageNurseName(request.getTriageNurseName());
        triage.setTargetDeptId(request.getTargetDeptId());
        triage.setTargetDeptName(request.getTargetDeptName());
        triage.setTriageStatus("WAITING");
        mapper.insert(triage);
        return triage;
    }

    @Override
    @Transactional
    public EmergencyTriage updateStatus(Long id, String status) {
        if (!STATUSES.contains(status)) throw new BusinessException(ErrorCode.BAD_REQUEST);
        EmergencyTriage triage = mapper.selectById(id);
        if (triage == null || triage.getDeleted() != 0) throw new BusinessException(ErrorCode.TRIAGE_NOT_FOUND);
        if (Set.of("COMPLETED", "CANCELLED").contains(triage.getTriageStatus())) {
            throw new BusinessException(ErrorCode.TRIAGE_LEVEL_INVALID);
        }
        triage.setTriageStatus(status);
        mapper.updateById(triage);
        return triage;
    }

    @Override
    public List<EmergencyTriage> queue() {
        return mapper.selectList(new LambdaQueryWrapper<EmergencyTriage>()
                .eq(EmergencyTriage::getDeleted, 0)
                .in(EmergencyTriage::getTriageStatus, "WAITING", "IN_TREATMENT", "OBSERVATION")
                .orderByAsc(EmergencyTriage::getTriageLevel)
                .orderByAsc(EmergencyTriage::getTriageTime));
    }
}
