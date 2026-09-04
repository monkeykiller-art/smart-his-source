package com.smarthis.common.fhir;

import org.hl7.fhir.r4.model.*;

import java.util.Date;

/**
 * FHIR R4 resource factory for common HIS resources.
 */
public class FhirResourceFactory {

    public static Patient createPatient(String id, String name, String gender, Date birthDate) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.addName().setText(name);
        if ("M".equalsIgnoreCase(gender)) {
            patient.setGender(Enumerations.AdministrativeGender.MALE);
        } else if ("F".equalsIgnoreCase(gender)) {
            patient.setGender(Enumerations.AdministrativeGender.FEMALE);
        } else {
            patient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
        }
        patient.setBirthDate(birthDate);
        return patient;
    }

    public static Encounter createEncounter(String patientId, String status) {
        Encounter encounter = new Encounter();
        encounter.setStatus(Encounter.EncounterStatus.fromCode(status));
        encounter.getSubject().setReference("Patient/" + patientId);
        encounter.setPeriod(new Period().setStart(new Date()));
        return encounter;
    }

    public static Observation createObservation(String patientId, String code, String display, String value) {
        Observation observation = new Observation();
        observation.setStatus(Observation.ObservationStatus.FINAL);
        observation.getSubject().setReference("Patient/" + patientId);
        observation.getCode().addCoding()
                .setSystem("http://loinc.org")
                .setCode(code)
                .setDisplay(display);
        observation.setValue(new StringType(value));
        return observation;
    }

    public static Practitioner createPractitioner(String id, String name, String department) {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(id);
        practitioner.addName().setText(name);
        practitioner.addQualification().getCode().setText(department);
        return practitioner;
    }
}
