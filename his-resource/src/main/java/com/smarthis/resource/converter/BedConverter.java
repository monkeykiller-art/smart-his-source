package com.smarthis.resource.converter;

import com.smarthis.resource.dto.request.BedCreateRequest;
import com.smarthis.resource.dto.request.BedUpdateRequest;
import com.smarthis.resource.dto.response.BedVo;
import com.smarthis.resource.entity.Bed;

public final class BedConverter {

    private BedConverter() {
    }

    public static Bed toEntity(BedCreateRequest req) {
        Bed e = new Bed();
        e.setBedNo(req.getBedNo());
        e.setWardId(req.getWardId());
        e.setRoomNo(req.getRoomNo());
        e.setBedType(req.getBedType() != null ? req.getBedType() : "NORMAL");
        e.setBedRank(req.getBedRank() != null ? req.getBedRank() : "EXTRA");
        e.setFloorNo(req.getFloorNo());
        e.setBedStatus("AVAILABLE");
        e.setIsMale(req.getIsMale() != null ? req.getIsMale() : 0);
        e.setFeeItemId(req.getFeeItemId());
        e.setDailyFee(req.getDailyFee());
        e.setSortOrder(req.getSortOrder());
        e.setRemark(req.getRemark());
        return e;
    }

    public static void applyUpdate(BedUpdateRequest req, Bed e) {
        if (req.getRoomNo() != null) e.setRoomNo(req.getRoomNo());
        if (req.getBedType() != null) e.setBedType(req.getBedType());
        if (req.getBedRank() != null) e.setBedRank(req.getBedRank());
        if (req.getFloorNo() != null) e.setFloorNo(req.getFloorNo());
        if (req.getBedStatus() != null) e.setBedStatus(req.getBedStatus());
        if (req.getIsMale() != null) e.setIsMale(req.getIsMale());
        if (req.getFeeItemId() != null) e.setFeeItemId(req.getFeeItemId());
        if (req.getDailyFee() != null) e.setDailyFee(req.getDailyFee());
        if (req.getSortOrder() != null) e.setSortOrder(req.getSortOrder());
        if (req.getRemark() != null) e.setRemark(req.getRemark());
    }

    public static BedVo toVo(Bed e) {
        BedVo vo = new BedVo();
        vo.setId(e.getId());
        vo.setBedNo(e.getBedNo());
        vo.setWardId(e.getWardId());
        vo.setRoomNo(e.getRoomNo());
        vo.setBedType(e.getBedType());
        vo.setBedRank(e.getBedRank());
        vo.setFloorNo(e.getFloorNo());
        vo.setBedStatus(e.getBedStatus());
        vo.setIsMale(e.getIsMale());
        vo.setFeeItemId(e.getFeeItemId());
        vo.setDailyFee(e.getDailyFee());
        vo.setSortOrder(e.getSortOrder());
        vo.setRemark(e.getRemark());
        vo.setCreatedTime(e.getCreatedTime());
        return vo;
    }
}
