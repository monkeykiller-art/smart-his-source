package com.smarthis.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.clinical.converter.OrderConverter;
import com.smarthis.clinical.dto.response.OrderTemplateVo;
import com.smarthis.clinical.entity.OrderTemplate;
import com.smarthis.clinical.entity.OrderTemplateItem;
import com.smarthis.clinical.mapper.OrderTemplateItemMapper;
import com.smarthis.clinical.mapper.OrderTemplateMapper;
import com.smarthis.clinical.service.OrderTemplateService;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTemplateServiceImpl implements OrderTemplateService {

    private final OrderTemplateMapper orderTemplateMapper;
    private final OrderTemplateItemMapper orderTemplateItemMapper;

    @Override
    @Transactional
    public OrderTemplateVo create(OrderTemplate template, List<OrderTemplateItem> items) {
        orderTemplateMapper.insert(template);

        for (OrderTemplateItem item : items) {
            item.setTemplateId(template.getId());
            orderTemplateItemMapper.insert(item);
        }

        log.info("Order template created: id={}, name={}", template.getId(), template.getTemplateName());
        OrderTemplateVo vo = OrderConverter.toTemplateVo(template);
        vo.setItems(items.stream().map(OrderConverter::toTemplateItemVo).toList());
        return vo;
    }

    @Override
    @Transactional
    public OrderTemplateVo update(Long id, OrderTemplate template, List<OrderTemplateItem> items) {
        OrderTemplate existing = getEntity(id);
        existing.setTemplateName(template.getTemplateName());
        existing.setTemplateLevel(template.getTemplateLevel());
        existing.setDeptId(template.getDeptId());
        existing.setTemplateCategory(template.getTemplateCategory());
        existing.setLevelType(template.getLevelType());
        existing.setOrderType(template.getOrderType());
        existing.setSortOrder(template.getSortOrder());
        existing.setTemplateStatus(template.getTemplateStatus());
        orderTemplateMapper.updateById(existing);

        LambdaQueryWrapper<OrderTemplateItem> deleteQuery = new LambdaQueryWrapper<>();
        deleteQuery.eq(OrderTemplateItem::getTemplateId, id);
        orderTemplateItemMapper.delete(deleteQuery);

        for (OrderTemplateItem item : items) {
            item.setId(null);
            item.setTemplateId(id);
            orderTemplateItemMapper.insert(item);
        }

        OrderTemplateVo vo = OrderConverter.toTemplateVo(existing);
        vo.setItems(items.stream().map(OrderConverter::toTemplateItemVo).toList());
        return vo;
    }

    @Override
    public OrderTemplateVo getById(Long id) {
        OrderTemplate existing = getEntity(id);
        OrderTemplateVo vo = OrderConverter.toTemplateVo(existing);

        LambdaQueryWrapper<OrderTemplateItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OrderTemplateItem::getTemplateId, id)
                .eq(OrderTemplateItem::getDeleted, 0)
                .orderByAsc(OrderTemplateItem::getItemSeq);
        List<OrderTemplateItem> items = orderTemplateItemMapper.selectList(itemQuery);
        vo.setItems(items.stream().map(OrderConverter::toTemplateItemVo).toList());
        return vo;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getEntity(id);
        orderTemplateMapper.deleteById(id);

        LambdaQueryWrapper<OrderTemplateItem> deleteQuery = new LambdaQueryWrapper<>();
        deleteQuery.eq(OrderTemplateItem::getTemplateId, id);
        orderTemplateItemMapper.delete(deleteQuery);

        log.info("Order template deleted: id={}", id);
    }

    @Override
    public List<OrderTemplateVo> listByDept(Long deptId) {
        LambdaQueryWrapper<OrderTemplate> query = new LambdaQueryWrapper<>();
        query.eq(OrderTemplate::getDeptId, deptId)
                .eq(OrderTemplate::getDeleted, 0)
                .eq(OrderTemplate::getTemplateStatus, 1)
                .orderByAsc(OrderTemplate::getSortOrder);
        return orderTemplateMapper.selectList(query).stream()
                .map(OrderConverter::toTemplateVo)
                .toList();
    }

    private OrderTemplate getEntity(Long id) {
        OrderTemplate template = orderTemplateMapper.selectById(id);
        if (template == null || template.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.ORDER_INVALID);
        }
        return template;
    }
}
