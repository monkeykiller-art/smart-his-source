package com.smarthis.patient.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EncounterVo {

    private Long id;

    private String encounterNo;

    private Long patientId;

    private String patientName;

    private Long regId;

    private String regNo;

    private Long deptId;

    private String deptName;

    private Long doctorId;

    private String doctorName;

    private String encounterType;

    private String encounterStatus;

    private LocalDate visitDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String chiefComplaint;
}
