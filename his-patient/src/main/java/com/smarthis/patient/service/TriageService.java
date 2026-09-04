package com.smarthis.patient.service;

import com.smarthis.patient.dto.response.TriageVo;

import java.util.List;

public interface TriageService {
    TriageVo enqueue(Long regId);
    TriageVo callNext(Long deptId, Long doctorId);
    TriageVo finish(Long triageId);
    List<TriageVo> queueByDoctor(Long deptId, Long doctorId);
    int waitingCount(Long deptId, Long doctorId);
}
