package com.smarthis.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.resource.converter.BedRecordConverter;
import com.smarthis.resource.dto.request.BedRecordCreateRequest;
import com.smarthis.resource.dto.response.BedOverviewVo;
import com.smarthis.resource.dto.response.BedRecordVo;
import com.smarthis.resource.entity.Bed;
import com.smarthis.resource.entity.BedRecord;
import com.smarthis.resource.mapper.BedMapper;
import com.smarthis.resource.mapper.BedRecordMapper;
import com.smarthis.resource.service.BedRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BedRecordServiceImpl implements BedRecordService {

    private final BedRecordMapper bedRecordMapper;
    private final BedMapper bedMapper;

    @Override
    @Transactional
    public BedRecordVo admit(BedRecordCreateRequest request) {
        Bed bed = bedMapper.selectById(request.getBedId());
        if (bed == null) {
            throw new BusinessException(ErrorCode.BED_NOT_FOUND);
        }
        if (!"AVAILABLE".equals(bed.getBedStatus())) {
            throw new BusinessException(ErrorCode.BED_NOT_AVAILABLE);
        }

        BedRecord record = BedRecordConverter.toEntity(request, bed.getWardId(), bed.getBedNo());
        record.setAdmitTime(LocalDateTime.now());
        bedRecordMapper.insert(record);

        bed.setBedStatus("OCCUPIED");
        bedMapper.updateById(bed);

        log.info("Bed admitted: recordId={}, bedId={}, patientId={}", record.getId(), bed.getId(), request.getPatientId());
        return BedRecordConverter.toVo(record);
    }

    @Override
    public BedRecordVo getById(Long id) {
        BedRecord record = bedRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.BED_NOT_FOUND);
        }
        return BedRecordConverter.toVo(record);
    }

    @Override
    public PageResult<BedRecordVo> query(Long wardId, String recordStatus, int page, int size) {
        LambdaQueryWrapper<BedRecord> wrapper = new LambdaQueryWrapper<>();
        if (wardId != null) {
            wrapper.eq(BedRecord::getWardId, wardId);
        }
        if (recordStatus != null && !recordStatus.isBlank()) {
            wrapper.eq(BedRecord::getRecordStatus, recordStatus);
        }
        wrapper.orderByDesc(BedRecord::getAdmitTime);

        Page<BedRecord> pageObj = bedRecordMapper.selectPage(new Page<>(page, size), wrapper);
        var records = pageObj.getRecords().stream()
                .map(BedRecordConverter::toVo)
                .toList();
        return new PageResult<>(records, pageObj.getTotal(), page, size);
    }

    @Override
    @Transactional
    public BedRecordVo discharge(Long id) {
        BedRecord record = bedRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.BED_NOT_FOUND);
        }
        if (!"OCCUPIED".equals(record.getRecordStatus())) {
            throw new BusinessException(ErrorCode.BED_NOT_AVAILABLE);
        }

        record.setDischargeTime(LocalDateTime.now());
        record.setRecordStatus("DISCHARGED");
        bedRecordMapper.updateById(record);

        Bed bed = bedMapper.selectById(record.getBedId());
        if (bed != null) {
            bed.setBedStatus("AVAILABLE");
            bedMapper.updateById(bed);
        }

        log.info("Bed discharged: recordId={}, bedId={}", id, record.getBedId());
        return BedRecordConverter.toVo(record);
    }

    @Override
    public List<BedOverviewVo> bedOverview(Long wardId) {
        LambdaQueryWrapper<Bed> bedWrapper = new LambdaQueryWrapper<>();
        if (wardId != null) {
            bedWrapper.eq(Bed::getWardId, wardId);
        }
        bedWrapper.orderByAsc(Bed::getSortOrder).orderByAsc(Bed::getBedNo);
        List<Bed> beds = bedMapper.selectList(bedWrapper);

        if (beds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> bedIds = beds.stream().map(Bed::getId).toList();
        LambdaQueryWrapper<BedRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.in(BedRecord::getBedId, bedIds)
                .eq(BedRecord::getRecordStatus, "OCCUPIED");
        List<BedRecord> activeRecords = bedRecordMapper.selectList(recordWrapper);

        Map<Long, BedRecord> recordMap = activeRecords.stream()
                .collect(Collectors.toMap(BedRecord::getBedId, r -> r, (a, b) -> a));

        List<BedOverviewVo> result = new ArrayList<>();
        for (Bed bed : beds) {
            result.add(BedRecordConverter.toOverviewVo(bed, recordMap.get(bed.getId())));
        }
        return result;
    }
}
