package com.smarthis.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.smarthis.patient.entity.InpatientBed;
import com.smarthis.patient.mapper.InpatientBedMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InpatientBedServiceImplTest {
    @Test void listsBedsUsingWardAndStatusQuery() {
        InpatientBedMapper mapper = mock(InpatientBedMapper.class);
        InpatientBed bed = new InpatientBed(); bed.setId(1L); bed.setBedStatus("AVAILABLE");
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(bed));
        var result = new InpatientBedServiceImpl(mapper).list(1101L, "AVAILABLE");
        assertEquals(List.of(bed), result);
        verify(mapper).selectList(any(Wrapper.class));
    }
}
