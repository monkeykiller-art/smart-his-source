package com.smarthis.resource.converter;

import com.smarthis.resource.dto.request.BedRecordCreateRequest;
import com.smarthis.resource.dto.response.BedOverviewVo;
import com.smarthis.resource.dto.response.BedRecordVo;
import com.smarthis.resource.entity.Bed;
import com.smarthis.resource.entity.BedRecord;

public final class BedRecordConverter {

    private BedRecordConverter() {
    }

    public static BedRecord toEntity(BedRecordCreateRequest req, Long wardId, String bedNo) {
        BedRecord e = new BedRecord();
        e.setBedId(req.getBedId());
        e.setPatientId(req.getPatientId());
        e.setAdmissionId(req.getAdmissionId());
        e.setWardId(wardId);
        e.setBedNo(bedNo);
        e.setRecordStatus("OCCUPIED");
        e.setExpectedStay(req.getExpectedStay());
        return e;
    }

    public static BedRecordVo toVo(BedRecord e) {
        BedRecordVo vo = new BedRecordVo();
        vo.setId(e.getId());
        vo.setBedId(e.getBedId());
        vo.setPatientId(e.getPatientId());
        vo.setAdmissionId(e.getAdmissionId());
        vo.setWardId(e.getWardId());
        vo.setBedNo(e.getBedNo());
        vo.setAdmitTime(e.getAdmitTime());
        vo.setDischargeTime(e.getDischargeTime());
        vo.setExpectedStay(e.getExpectedStay());
        vo.setRecordStatus(e.getRecordStatus());
        vo.setCreatedTime(e.getCreatedTime());
        return vo;
    }

    public static BedOverviewVo toOverviewVo(Bed bed, BedRecord record) {
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
        if (record != null) {
            vo.setCurrentPatientId(record.getPatientId());
            vo.setCurrentBedNo(record.getBedNo());
            vo.setAdmissionId(record.getAdmissionId());
            vo.setAdmitTime(record.getAdmitTime());
            vo.setExpectedStay(record.getExpectedStay());
            vo.setRecordStatus(record.getRecordStatus());
        }
        return vo;
    }
}
