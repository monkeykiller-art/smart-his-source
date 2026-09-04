package com.smarthis.patient.controller;

import ca.uhn.fhir.context.FhirContext;
import com.smarthis.patient.fhir.FhirPatientMapper;
import com.smarthis.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * FHIR R4 interoperability boundary for the enterprise patient master index.
 * This controller deliberately returns standard FHIR JSON rather than the internal ApiResponse wrapper.
 */
@RestController
@RequestMapping(value = "/fhir/R4/Patient", produces = "application/fhir+json")
@RequiredArgsConstructor
public class FhirPatientController {

    private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();

    private final PatientService patientService;

    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable Long id) {
        return fhirResponse(FhirPatientMapper.toResource(patientService.getById(id)));
    }

    /**
     * Supports the FHIR identifier search form: identifier=system|value. The optional system is accepted
     * for interoperability and the local identifier type is required to select the matching identifier.
     */
    @GetMapping
    public ResponseEntity<String> getByIdentifier(@RequestParam("identifier") String identifier,
                                                  @RequestParam("identifierType") String identifierType) {
        String value = identifier.contains("|")
                ? identifier.substring(identifier.indexOf('|') + 1)
                : identifier;
        return fhirResponse(FhirPatientMapper.toResource(patientService.getByIdNo(identifierType, value)));
    }

    private ResponseEntity<String> fhirResponse(Patient patient) {
        String payload = FHIR_CONTEXT.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/fhir+json"))
                .body(payload);
    }
}
