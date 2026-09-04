package com.smarthis.patient.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleQueryRequest extends PageQuery {

    private Long deptId;

    private Long doctorId;

    private LocalDate scheduleDate;

    private String timePeriod;
}
