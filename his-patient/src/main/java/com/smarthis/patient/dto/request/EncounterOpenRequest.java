package com.smarthis.patient.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EncounterOpenRequest {

    @NotNull(message = "registration id is required")
    private Long regId;

    private String chiefComplaint;
}
