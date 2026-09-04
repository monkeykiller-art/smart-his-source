package com.smarthis.patient.service;

import com.smarthis.patient.dto.request.AppointmentCreateRequest;
import com.smarthis.patient.dto.response.AppointmentVo;

import java.util.List;

public interface AppointmentService {
    AppointmentVo create(AppointmentCreateRequest request);
    AppointmentVo getById(Long id);
    void confirm(Long id);
    void cancel(Long id, String reason);
    List<AppointmentVo> listByPatient(Long patientId);
}
