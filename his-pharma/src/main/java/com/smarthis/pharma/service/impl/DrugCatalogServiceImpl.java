package com.smarthis.pharma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.DrugCatalogQueryRequest;
import com.smarthis.pharma.dto.request.DrugCatalogSaveRequest;
import com.smarthis.pharma.dto.response.DrugCatalogVo;
import com.smarthis.pharma.entity.DrugCatalog;
import com.smarthis.pharma.mapper.DrugCatalogMapper;
import com.smarthis.pharma.service.DrugCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DrugCatalogServiceImpl implements DrugCatalogService {
    private final DrugCatalogMapper drugCatalogMapper;

    @Override
    @Transactional
    public DrugCatalogVo create(DrugCatalogSaveRequest request) {
        ensureCodeAvailable(request.getDrugCode(), null);
        DrugCatalog entity = new DrugCatalog();
        copy(request, entity);
        drugCatalogMapper.insert(entity);
        return toVo(entity);
    }

    @Override
    @Transactional
    public DrugCatalogVo update(Long id, DrugCatalogSaveRequest request) {
        DrugCatalog entity = getEntity(id);
        ensureCodeAvailable(request.getDrugCode(), id);
        copy(request, entity);
        drugCatalogMapper.updateById(entity);
        return toVo(entity);
    }

    @Override
    public DrugCatalogVo getById(Long id) {
        return toVo(getEntity(id));
    }

    @Override
    public PageResult<DrugCatalogVo> query(DrugCatalogQueryRequest request) {
        LambdaQueryWrapper<DrugCatalog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugCatalog::getDeleted, 0);
        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            wrapper.and(q -> q.like(DrugCatalog::getDrugCode, keyword)
                    .or().like(DrugCatalog::getGenericName, keyword)
                    .or().like(DrugCatalog::getTradeName, keyword)
                    .or().like(DrugCatalog::getPinyinCode, keyword));
        }
        wrapper.eq(StringUtils.hasText(request.getDosageForm()), DrugCatalog::getDosageForm, request.getDosageForm());
        wrapper.eq(StringUtils.hasText(request.getPrescriptionType()), DrugCatalog::getPrescriptionType, request.getPrescriptionType());
        wrapper.eq(request.getIsActive() != null, DrugCatalog::getIsActive, request.getIsActive());
        wrapper.orderByAsc(DrugCatalog::getDrugCode);
        IPage<DrugCatalog> page = drugCatalogMapper.selectPage(request.toPage(), wrapper);
        List<DrugCatalogVo> records = page.getRecords().stream().map(DrugCatalogServiceImpl::toVo).toList();
        return new PageResult<>(records, page.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public DrugCatalogVo setActive(Long id, boolean active) {
        DrugCatalog entity = getEntity(id);
        entity.setIsActive(active ? 1 : 0);
        drugCatalogMapper.updateById(entity);
        return toVo(entity);
    }

    private DrugCatalog getEntity(Long id) {
        DrugCatalog entity = drugCatalogMapper.selectById(id);
        if (entity == null || Integer.valueOf(1).equals(entity.getDeleted())) {
            throw new BusinessException(ErrorCode.DRUG_NOT_FOUND);
        }
        return entity;
    }

    private void ensureCodeAvailable(String drugCode, Long excludedId) {
        LambdaQueryWrapper<DrugCatalog> query = new LambdaQueryWrapper<>();
        query.eq(DrugCatalog::getDrugCode, drugCode.trim()).eq(DrugCatalog::getDeleted, 0);
        if (excludedId != null) query.ne(DrugCatalog::getId, excludedId);
        if (drugCatalogMapper.selectCount(query) > 0) {
            throw new BusinessException(ErrorCode.DRUG_CODE_DUPLICATE);
        }
    }

    private static void copy(DrugCatalogSaveRequest source, DrugCatalog target) {
        target.setDrugCode(source.getDrugCode().trim());
        target.setGenericName(source.getGenericName().trim());
        target.setTradeName(source.getTradeName());
        target.setPinyinCode(source.getPinyinCode());
        target.setDosageForm(source.getDosageForm());
        target.setStrength(source.getStrength());
        target.setManufacturer(source.getManufacturer());
        target.setApprovalNo(source.getApprovalNo());
        target.setPackageUnit(source.getPackageUnit());
        target.setMinUnit(source.getMinUnit());
        target.setConversionFactor(source.getConversionFactor());
        target.setPurchasePrice(source.getPurchasePrice());
        target.setRetailPrice(source.getRetailPrice());
        target.setPrescriptionType(source.getPrescriptionType());
        target.setAntibioticLevel(source.getAntibioticLevel());
        target.setIsActive(source.getIsActive() == null ? 1 : source.getIsActive());
    }

    private static DrugCatalogVo toVo(DrugCatalog e) {
        DrugCatalogVo vo = new DrugCatalogVo();
        vo.setId(e.getId()); vo.setDrugCode(e.getDrugCode()); vo.setGenericName(e.getGenericName());
        vo.setTradeName(e.getTradeName()); vo.setPinyinCode(e.getPinyinCode()); vo.setDosageForm(e.getDosageForm());
        vo.setStrength(e.getStrength()); vo.setManufacturer(e.getManufacturer()); vo.setApprovalNo(e.getApprovalNo());
        vo.setPackageUnit(e.getPackageUnit()); vo.setMinUnit(e.getMinUnit()); vo.setConversionFactor(e.getConversionFactor());
        vo.setPurchasePrice(e.getPurchasePrice()); vo.setRetailPrice(e.getRetailPrice());
        vo.setPrescriptionType(e.getPrescriptionType()); vo.setAntibioticLevel(e.getAntibioticLevel()); vo.setIsActive(e.getIsActive());
        return vo;
    }
}
