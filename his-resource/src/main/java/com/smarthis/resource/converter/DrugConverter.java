package com.smarthis.resource.converter;

import com.smarthis.resource.dto.request.DrugCreateRequest;
import com.smarthis.resource.dto.request.DrugUpdateRequest;
import com.smarthis.resource.dto.response.DrugVo;
import com.smarthis.resource.entity.Drug;

public final class DrugConverter {

    private DrugConverter() {
    }

    public static Drug toEntity(DrugCreateRequest req) {
        Drug e = new Drug();
        e.setDrugCode(req.getDrugCode());
        e.setDrugName(req.getDrugName());
        e.setNamePinyin(req.getNamePinyin());
        e.setGenericName(req.getGenericName());
        e.setDosageForm(req.getDosageForm());
        e.setSpec(req.getSpec());
        e.setUnit(req.getUnit());
        e.setPackUnit(req.getPackUnit());
        e.setPackQty(req.getPackQty());
        e.setManufacturer(req.getManufacturer());
        e.setApprovalNo(req.getApprovalNo());
        e.setBarCode(req.getBarCode());
        e.setDrugType(req.getDrugType() != null ? req.getDrugType() : "WESTERN");
        e.setIsInsurance(req.getIsInsurance() != null ? req.getIsInsurance() : 0);
        e.setInsuranceRatio(req.getInsuranceRatio());
        e.setIsNarcotic(req.getIsNarcotic() != null ? req.getIsNarcotic() : 0);
        e.setIsPsychotropic(req.getIsPsychotropic() != null ? req.getIsPsychotropic() : 0);
        e.setIsAntibiotic(req.getIsAntibiotic() != null ? req.getIsAntibiotic() : 0);
        e.setAntibioticLevel(req.getAntibioticLevel());
        e.setNeedSkinTest(req.getNeedSkinTest() != null ? req.getNeedSkinTest() : 0);
        e.setMaxSingleDose(req.getMaxSingleDose());
        e.setMaxDailyDose(req.getMaxDailyDose());
        e.setStorageCondition(req.getStorageCondition());
        e.setSortOrder(req.getSortOrder());
        e.setDrugStatus("ACTIVE");
        return e;
    }

    public static void applyUpdate(DrugUpdateRequest req, Drug e) {
        if (req.getDrugName() != null) e.setDrugName(req.getDrugName());
        if (req.getNamePinyin() != null) e.setNamePinyin(req.getNamePinyin());
        if (req.getGenericName() != null) e.setGenericName(req.getGenericName());
        if (req.getDosageForm() != null) e.setDosageForm(req.getDosageForm());
        if (req.getSpec() != null) e.setSpec(req.getSpec());
        if (req.getUnit() != null) e.setUnit(req.getUnit());
        if (req.getPackUnit() != null) e.setPackUnit(req.getPackUnit());
        if (req.getPackQty() != null) e.setPackQty(req.getPackQty());
        if (req.getManufacturer() != null) e.setManufacturer(req.getManufacturer());
        if (req.getApprovalNo() != null) e.setApprovalNo(req.getApprovalNo());
        if (req.getBarCode() != null) e.setBarCode(req.getBarCode());
        if (req.getDrugType() != null) e.setDrugType(req.getDrugType());
        if (req.getIsInsurance() != null) e.setIsInsurance(req.getIsInsurance());
        if (req.getInsuranceRatio() != null) e.setInsuranceRatio(req.getInsuranceRatio());
        if (req.getIsNarcotic() != null) e.setIsNarcotic(req.getIsNarcotic());
        if (req.getIsPsychotropic() != null) e.setIsPsychotropic(req.getIsPsychotropic());
        if (req.getIsAntibiotic() != null) e.setIsAntibiotic(req.getIsAntibiotic());
        if (req.getAntibioticLevel() != null) e.setAntibioticLevel(req.getAntibioticLevel());
        if (req.getNeedSkinTest() != null) e.setNeedSkinTest(req.getNeedSkinTest());
        if (req.getMaxSingleDose() != null) e.setMaxSingleDose(req.getMaxSingleDose());
        if (req.getMaxDailyDose() != null) e.setMaxDailyDose(req.getMaxDailyDose());
        if (req.getStorageCondition() != null) e.setStorageCondition(req.getStorageCondition());
        if (req.getSortOrder() != null) e.setSortOrder(req.getSortOrder());
        if (req.getDrugStatus() != null) e.setDrugStatus(req.getDrugStatus());
    }

    public static DrugVo toVo(Drug e) {
        DrugVo vo = new DrugVo();
        vo.setId(e.getId());
        vo.setDrugCode(e.getDrugCode());
        vo.setDrugName(e.getDrugName());
        vo.setNamePinyin(e.getNamePinyin());
        vo.setGenericName(e.getGenericName());
        vo.setDosageForm(e.getDosageForm());
        vo.setSpec(e.getSpec());
        vo.setUnit(e.getUnit());
        vo.setPackUnit(e.getPackUnit());
        vo.setPackQty(e.getPackQty());
        vo.setManufacturer(e.getManufacturer());
        vo.setApprovalNo(e.getApprovalNo());
        vo.setBarCode(e.getBarCode());
        vo.setDrugType(e.getDrugType());
        vo.setIsInsurance(e.getIsInsurance());
        vo.setInsuranceRatio(e.getInsuranceRatio());
        vo.setIsNarcotic(e.getIsNarcotic());
        vo.setIsPsychotropic(e.getIsPsychotropic());
        vo.setIsAntibiotic(e.getIsAntibiotic());
        vo.setAntibioticLevel(e.getAntibioticLevel());
        vo.setNeedSkinTest(e.getNeedSkinTest());
        vo.setMaxSingleDose(e.getMaxSingleDose());
        vo.setMaxDailyDose(e.getMaxDailyDose());
        vo.setStorageCondition(e.getStorageCondition());
        vo.setSortOrder(e.getSortOrder());
        vo.setDrugStatus(e.getDrugStatus());
        vo.setCreatedTime(e.getCreatedTime());
        return vo;
    }
}
