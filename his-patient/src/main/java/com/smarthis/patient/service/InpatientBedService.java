package com.smarthis.patient.service;

import com.smarthis.patient.entity.InpatientBed;
import java.util.List;

public interface InpatientBedService {
    List<InpatientBed> list(Long wardId, String status);
}
