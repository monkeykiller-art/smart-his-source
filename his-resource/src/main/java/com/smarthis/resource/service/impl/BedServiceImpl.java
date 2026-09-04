package com.smarthis.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.resource.converter.BedConverter;
import com.smarthis.resource.converter.BedRecordConverter;
import com.smarthis.resource.dto.request.BedAdmitRequest;
import com.smarthis.resource.dto.request.BedCreateRequest;
import com.smarthis.resource.dto.request.BedDischargeRequest;
import com.smarthis.resource.dto.request.BedQueryRequest;
import com.smarthis.resource.dto.request.BedUpdateRequest;
import com.smarthis.resource.dto.response.BedOverviewVo;
import com.smarthis.resource.dto.response.BedRecordVo;
import com.smarthis.resource.dto.response.BedVo;
import com.smarthis.resource.entity.Bed;
import com.smarthis.resource.entity.BedRecord;
import com.smarthis.resource.enums.BedStatus;
import com.smarthis.resource.enums.BedRecordStatus;
import com.smarthis.resource.mapper.BedMapper;
import com.smarthis.resource.mapper.BedRecordMapper;
import com.smarthis.resource.service.BedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BedServiceImpl implements BedService {

    private final BedMapper bedMapper;
    private final BedRecordMapper bedRecordMapper;

    @Override
    @Transactional
    public BedVo create(BedCreateRequest request) {
        Bed bed = BedConverter.toEntity(request);
        bedMapper.insert(bed);
        return BedConverter.toVo(bed);
    }

    @Override
    @Transactional
    public BedVo update(Long id, BedUpdateRequest request) {
        Bed bed = requireBed(id);
        BedConverter.applyUpdate(request, bed);
        bedMapper.updateById(bed);
        return BedConverter.toVo(bed);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Bed bed = requireBed(id);
        if (BedStatus.OCCUPIED.name().equals(bed.getBedStatus())) {
            throw new BusinessException(ErrorCode.BED_OCCUPIED);
        }
        bed.setDeleted(1);
        bedMapper.updateById(bed);
    }

    @Override
    public BedVo getById(Long id) {
        return BedConverter.toVo(requireBed(id));
    }

    @Override
    public PageResult<BedVo> list(BedQueryRequest request) {
        Page<Bed> page = request.toPage();
        LambdaQueryWrapper<Bed> query = new LambdaQueryWrapper<>();
        query.eq(Bed::getDeleted, 0);
        if (request.getWardId() != null) {
            query.eq(Bed::getWardId, request.getWardId());
        }
        if (StringUtils.hasText(request.getBedStatus())) {
            query.eq(Bed::getBedStatus, request.getBedStatus());
        }
        if (StringUtils.hasText(request.getBedType())) {
            query.eq(Bed::getBedType, request.getBedType());
        }
        if (StringUtils.hasText(request.getBedRank())) {
            query.eq(Bed::getBedRank, request.getBedRank());
        }
        query.orderByAsc(Bed::getSortOrder);
        Page<Bed> result = bedMapper.selectPage(page, query);
        List<BedVo> records = result.getRecords().stream().map(BedConverter::toVo).toList();
        return new PageResult<>(records, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public BedRecordVo admit(BedAdmitRequest request) {
        Bed bed = requireBed(request.getBedId());
        if (!BedStatus.AVAILABLE.name().equals(bed.getBedStatus())) {
            throw new BusinessException(ErrorCode.BED_OCCUPIED);
        }
        BedRecord record = new BedRecord();
        record.setBedId(bed.getId());
        record.setPatientId(request.getPatientId());
        record.setAdmissionId(request.getAdmissionId());
        record.setWardId(request.getWardId());
        record.setBedNo(bed.getBedNo());
        record.setAdmitTime(LocalDateTime.now());
        record.setExpectedStay(request.getExpectedStay());
        record.setRecordStatus(BedRecordStatus.OCCUPIED.name());
        bedRecordMapper.insert(record);

        bed.setBedStatus(BedStatus.OCCUPIED.name());
        bedMapper.updateById(bed);

        return BedRecordConverter.toVo(record);
    }

    @Override
    @Transactional
    public BedRecordVo discharge(Long bedRecordId, BedDischargeRequest request) {
        BedRecord record = bedRecordMapper.selectById(bedRecordId);
        if (record == null || record.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.BED_NOT_FOUND);
        }
        if (!BedRecordStatus.OCCUPIED.name().equals(record.getRecordStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "Bed record is not in OCCUPIED status");
        }
        record.setDischargeTime(request.getDischargeTime() != null ? request.getDischargeTime() : LocalDateTime.now());
        record.setRecordStatus(BedRecordStatus.DISCHARGED.name());
        bedRecordMapper.updateById(record);

        Bed bed = requireBed(record.getBedId());
        bed.setBedStatus(BedStatus.AVAILABLE.name());
        bedMapper.updateById(bed);

        return BedRecordConverter.toVo(record);
    }

    @Override
    public List<BedOverviewVo> overview(Long wardId) {
        LambdaQueryWrapper<Bed> bedQuery = new LambdaQueryWrapper<>();
        bedQuery.eq(Bed::getDeleted, 0).eq(Bed::getWardId, wardId).orderByAsc(Bed::getSortOrder);
        List<Bed> beds = bedMapper.selectList(bedQuery);

        List<BedOverviewVo> result = new ArrayList<>();
        for (Bed bed : beds) {
            BedOverviewVo vo = new BedOverviewVo();
            vo.setBedId(bed.getId());
            vo.setBedNo(bed.getBedNo());
            vo.setRoomNo(bed.getRoomNo());
            vo.setBedType(bed.getBedType());
            vo.setBedRank(bed.getBedRank());
            vo.setFloorNo(bed.getFloorNo());
            vo.setBedStatus(bed.getBedStatus());
            vo.setIsMale(bed.getIsMale());
            vo.setDailyFee(bed.getDailyFee());
            vo.setSortOrder(bed.getSortOrder());

            if (BedStatus.OCCUPIED.name().equals(bed.getBedStatus())) {
                LambdaQueryWrapper<BedRecord> recQuery = new LambdaQueryWrapper<>();
                recQuery.eq(BedRecord::getBedId, bed.getId())
                        .eq(BedRecord::getRecordStatus, BedRecordStatus.OCCUPIED.name())
                        .orderByDesc(BedRecord::getAdmitTime)
                        .last("LIMIT 1");
                BedRecord activeRecord = bedRecordMapper.selectOne(recQuery);
                if (activeRecord != null) {
                    vo.setCurrentPatientId(activeRecord.getPatientId());
                    vo.setCurrentBedNo(activeRecord.getBedNo());
                    vo.setAdmissionId(activeRecord.getAdmissionId());
                    vo.setAdmitTime(activeRecord.getAdmitTime());
                    vo.setExpectedStay(activeRecord.getExpectedStay());
                    vo.setRecordStatus(activeRecord.getRecordStatus());
                }
            }
            result.add(vo);
        }
        return result;
    }

    private Bed requireBed(Long id) {
        Bed bed = bedMapper.selectById(id);
        if (bed == null || bed.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.BED_NOT_FOUND);
        }
        return bed;
    }
}
