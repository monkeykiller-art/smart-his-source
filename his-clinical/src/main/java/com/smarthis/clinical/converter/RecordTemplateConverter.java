package com.smarthis.clinical.converter;

import com.smarthis.clinical.dto.request.RecordTemplateCreateRequest;
import com.smarthis.clinical.dto.response.RecordTemplateVo;
import com.smarthis.clinical.entity.RecordTemplate;

public final class RecordTemplateConverter {

    private RecordTemplateConverter() {
    }

    public static RecordTemplate toEntity(RecordTemplateCreateRequest req) {
        RecordTemplate e = new RecordTemplate();
        e.setTemplateName(req.getTemplateName());
        e.setTemplateType(req.getTemplateType());
        e.setDeptId(req.getDeptId());
        e.setDiseaseCode(req.getDiseaseCode());
        e.setRecordType(req.getRecordType());
        e.setTemplateContent(req.getTemplateContent());
        e.setSortOrder(req.getSortOrder());
        e.setTemplateStatus(req.getTemplateStatus() != null ? req.getTemplateStatus() : 1);
        return e;
    }

    public static RecordTemplateVo toVo(RecordTemplate e) {
        RecordTemplateVo vo = new RecordTemplateVo();
        vo.setId(e.getId());
        vo.setTemplateName(e.getTemplateName());
        vo.setTemplateType(e.getTemplateType());
        vo.setDeptId(e.getDeptId());
        vo.setDiseaseCode(e.getDiseaseCode());
        vo.setRecordType(e.getRecordType());
        vo.setTemplateContent(e.getTemplateContent());
        vo.setSortOrder(e.getSortOrder());
        vo.setTemplateStatus(e.getTemplateStatus());
        return vo;
    }
}
