package com.smarthis.pharma.service.impl;

import com.smarthis.common.exception.BusinessException;
import com.smarthis.pharma.dto.request.DrugCatalogSaveRequest;
import com.smarthis.pharma.dto.response.DrugCatalogVo;
import com.smarthis.pharma.entity.DrugCatalog;
import com.smarthis.pharma.mapper.DrugCatalogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrugCatalogServiceImplTest {
    @Mock
    private DrugCatalogMapper mapper;
    private DrugCatalogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DrugCatalogServiceImpl(mapper);
    }

    @Test
    void createsActiveDrugWithNormalizedCode() {
        when(mapper.selectCount(any())).thenReturn(0L);
        doAnswer(invocation -> {
            DrugCatalog entity = invocation.getArgument(0);
            entity.setId(10L);
            return 1;
        }).when(mapper).insert(any());

        DrugCatalogVo result = service.create(request(" D001 "));

        assertEquals(10L, result.getId());
        assertEquals("D001", result.getDrugCode());
        assertEquals(1, result.getIsActive());
        ArgumentCaptor<DrugCatalog> captor = ArgumentCaptor.forClass(DrugCatalog.class);
        verify(mapper).insert(captor.capture());
        assertEquals(new BigDecimal("12.5000"), captor.getValue().getRetailPrice());
    }

    @Test
    void rejectsDuplicateDrugCode() {
        when(mapper.selectCount(any())).thenReturn(1L);

        BusinessException error = assertThrows(BusinessException.class, () -> service.create(request("D001")));

        assertEquals(4011, error.getCode());
        verify(mapper, never()).insert(any());
    }

    @Test
    void deactivatesExistingDrug() {
        DrugCatalog entity = new DrugCatalog();
        entity.setId(10L);
        entity.setDrugCode("D001");
        entity.setDeleted(0);
        entity.setIsActive(1);
        when(mapper.selectById(10L)).thenReturn(entity);

        DrugCatalogVo result = service.setActive(10L, false);

        assertEquals(0, result.getIsActive());
        verify(mapper).updateById(entity);
    }

    private static DrugCatalogSaveRequest request(String code) {
        DrugCatalogSaveRequest request = new DrugCatalogSaveRequest();
        request.setDrugCode(code);
        request.setGenericName("阿莫西林");
        request.setDosageForm("胶囊");
        request.setStrength("0.25g");
        request.setManufacturer("示例药厂");
        request.setPackageUnit("盒");
        request.setMinUnit("粒");
        request.setConversionFactor(new BigDecimal("24"));
        request.setPurchasePrice(new BigDecimal("8.0000"));
        request.setRetailPrice(new BigDecimal("12.5000"));
        request.setPrescriptionType("RX");
        return request;
    }
}
