package com.smarthis.operations.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.operations.converter.FeeTemplateConverter;
import com.smarthis.operations.dto.response.FeeTemplateItemVo;
import com.smarthis.operations.dto.response.FeeTemplateVo;
import com.smarthis.operations.entity.FeeTemplate;
import com.smarthis.operations.entity.FeeTemplateItem;
import com.smarthis.operations.mapper.FeeTemplateItemMapper;
import com.smarthis.operations.mapper.FeeTemplateMapper;
import com.smarthis.operations.service.FeeTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeTemplateServiceImpl implements FeeTemplateService {

    private final FeeTemplateMapper feeTemplateMapper;
    private final FeeTemplateItemMapper feeTemplateItemMapper;

    @Override
    public FeeTemplateVo getById(Long id) {
        FeeTemplate template = feeTemplateMapper.selectById(id);
        if (template == null || template.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        List<FeeTemplateItem> items = feeTemplateItemMapper.selectList(
                new LambdaQueryWrapper<FeeTemplateItem>()
                        .eq(FeeTemplateItem::getTemplateId, id)
                        .orderByAsc(FeeTemplateItem::getItemSeq));
        return FeeTemplateConverter.toVoWithItems(template, items);
    }

    @Override
    public List<FeeTemplateVo> listByDept(Long deptId) {
        LambdaQueryWrapper<FeeTemplate> query = new LambdaQueryWrapper<>();
        if (deptId != null) {
            query.eq(FeeTemplate::getDeptId, deptId);
        }
        query.eq(FeeTemplate::getTemplateStatus, 1)
                .orderByAsc(FeeTemplate::getSortOrder);
        return feeTemplateMapper.selectList(query).stream()
                .map(t -> {
                    List<FeeTemplateItem> items = feeTemplateItemMapper.selectList(
                            new LambdaQueryWrapper<FeeTemplateItem>()
                                    .eq(FeeTemplateItem::getTemplateId, t.getId())
                                    .orderByAsc(FeeTemplateItem::getItemSeq));
                    return FeeTemplateConverter.toVoWithItems(t, items);
                })
                .toList();
    }

    @Override
    @Transactional
    public FeeTemplateVo create(String templateName, String templateCategory, String templateLevel, Long deptId) {
        FeeTemplate template = new FeeTemplate();
        template.setTemplateName(templateName);
        template.setTemplateCategory(templateCategory);
        template.setTemplateLevel(templateLevel);
        template.setDeptId(deptId);
        template.setSortOrder(0);
        template.setTemplateStatus(1);
        feeTemplateMapper.insert(template);

        log.info("Fee template created: id={}, name={}", template.getId(), templateName);
        return FeeTemplateConverter.toVoWithItems(template, List.of());
    }

    @Override
    @Transactional
    public FeeTemplateVo update(Long id, String templateName, String templateCategory, Integer sortOrder) {
        FeeTemplate template = feeTemplateMapper.selectById(id);
        if (template == null || template.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (StringUtils.hasText(templateName)) {
            template.setTemplateName(templateName);
        }
        if (StringUtils.hasText(templateCategory)) {
            template.setTemplateCategory(templateCategory);
        }
        if (sortOrder != null) {
            template.setSortOrder(sortOrder);
        }
        feeTemplateMapper.updateById(template);

        List<FeeTemplateItem> items = feeTemplateItemMapper.selectList(
                new LambdaQueryWrapper<FeeTemplateItem>()
                        .eq(FeeTemplateItem::getTemplateId, id)
                        .orderByAsc(FeeTemplateItem::getItemSeq));
        return FeeTemplateConverter.toVoWithItems(template, items);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        FeeTemplate template = feeTemplateMapper.selectById(id);
        if (template == null || template.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        feeTemplateMapper.deleteById(id);

        LambdaQueryWrapper<FeeTemplateItem> query = new LambdaQueryWrapper<>();
        query.eq(FeeTemplateItem::getTemplateId, id);
        feeTemplateItemMapper.delete(query);

        log.info("Fee template deleted: id={}", id);
    }

    @Override
    @Transactional
    public FeeTemplateItemVo addItem(Long templateId, String itemType, String itemCode, String itemName,
                                      BigDecimal quantity, String unit, Long executeDeptId, Integer itemSeq) {
        FeeTemplate template = feeTemplateMapper.selectById(templateId);
        if (template == null || template.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        FeeTemplateItem item = new FeeTemplateItem();
        item.setTemplateId(templateId);
        item.setItemType(itemType != null ? itemType : "FEE");
        item.setItemCode(itemCode);
        item.setItemName(itemName);
        item.setQuantity(quantity != null ? quantity : BigDecimal.ONE);
        item.setUnit(unit);
        item.setExecuteDeptId(executeDeptId);
        item.setItemSeq(itemSeq != null ? itemSeq : nextSeq(templateId));
        feeTemplateItemMapper.insert(item);

        return FeeTemplateConverter.toItemVo(item);
    }

    @Override
    @Transactional
    public void removeItem(Long templateId, Long itemId) {
        FeeTemplateItem item = feeTemplateItemMapper.selectById(itemId);
        if (item == null || item.getDeleted() != 0 || !item.getTemplateId().equals(templateId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        feeTemplateItemMapper.deleteById(itemId);
    }

    private int nextSeq(Long templateId) {
        LambdaQueryWrapper<FeeTemplateItem> query = new LambdaQueryWrapper<>();
        query.eq(FeeTemplateItem::getTemplateId, templateId)
                .orderByDesc(FeeTemplateItem::getItemSeq)
                .last("LIMIT 1");
        FeeTemplateItem last = feeTemplateItemMapper.selectOne(query);
        return last != null ? last.getItemSeq() + 1 : 1;
    }
}
