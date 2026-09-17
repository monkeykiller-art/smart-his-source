package com.smarthis.emergency.service;

import com.smarthis.emergency.dto.request.EmergencyTriageCreateRequest;
import com.smarthis.emergency.entity.EmergencyTriage;

import java.util.List;

public interface EmergencyTriageService {
    EmergencyTriage create(EmergencyTriageCreateRequest request);
    EmergencyTriage updateStatus(Long id, String status);
    List<EmergencyTriage> queue();
}
