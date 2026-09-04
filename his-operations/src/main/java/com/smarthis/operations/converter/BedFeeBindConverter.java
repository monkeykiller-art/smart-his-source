package com.smarthis.operations.converter;

import com.smarthis.operations.dto.request.BedFeeBindCreateRequest;
import com.smarthis.operations.dto.response.BedFeeBindVo;
import com.smarthis.operations.entity.BedFeeBind;

public final class BedFeeBindConverter {

    private BedFeeBindConverter() {
    }

    public static BedFeeBind toEntity(BedFeeBindCreateRequest req) {
        BedFeeBind e = new BedFeeBind();
        e.setBedId(req.getBedId());
        e.setFeeItemId(req.getFeeItemId());
        e.setFeeItemCode(req.getFeeItemCode());
        e.setFeeItemName(req.getFeeItemName());
        e.setDailyFee(req.getDailyFee());
        e.setIsActive(1);
        return e;
    }

    public static BedFeeBindVo toVo(BedFeeBind e) {
        BedFeeBindVo vo = new BedFeeBindVo();
        vo.setId(e.getId());
        vo.setBedId(e.getBedId());
        vo.setFeeItemId(e.getFeeItemId());
        vo.setFeeItemCode(e.getFeeItemCode());
        vo.setFeeItemName(e.getFeeItemName());
        vo.setDailyFee(e.getDailyFee());
        vo.setIsActive(e.getIsActive());
        return vo;
    }
}
