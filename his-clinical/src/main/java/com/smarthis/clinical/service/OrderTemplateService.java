package com.smarthis.clinical.service;

import com.smarthis.clinical.dto.response.OrderTemplateVo;
import com.smarthis.clinical.entity.OrderTemplate;
import com.smarthis.clinical.entity.OrderTemplateItem;

import java.util.List;

public interface OrderTemplateService {

    OrderTemplateVo create(OrderTemplate template, List<OrderTemplateItem> items);

    OrderTemplateVo update(Long id, OrderTemplate template, List<OrderTemplateItem> items);

    OrderTemplateVo getById(Long id);

    void delete(Long id);

    List<OrderTemplateVo> listByDept(Long deptId);
}
