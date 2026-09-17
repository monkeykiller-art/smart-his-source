package com.smarthis.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.patient.entity.InpatientBed;
import com.smarthis.patient.mapper.InpatientBedMapper;
import com.smarthis.patient.service.InpatientBedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InpatientBedServiceImpl implements InpatientBedService {
    private final InpatientBedMapper bedMapper;

    @Override
    public List<InpatientBed> list(Long wardId, String status) {
        return bedMapper.selectList(new LambdaQueryWrapper<InpatientBed>()
                .eq(wardId != null, InpatientBed::getWardId, wardId)
                .eq(status != null && !status.isBlank(), InpatientBed::getBedStatus, status)
                .eq(InpatientBed::getDeleted, 0)
                .orderByAsc(InpatientBed::getWardId, InpatientBed::getBedNo));
    }
}
