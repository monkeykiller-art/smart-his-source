package com.smarthis.patient.fhir;

import com.smarthis.patient.dto.response.PatientVo;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FhirPatientMapperTest {

    @Test
    void mapsEmpiRecordToFhirR4Patient() {
        PatientVo source = new PatientVo();
        source.setId(1001L);
        source.setEmpiNo("EMPI-1001");
        source.setName("张三");
        source.setGender(1);
        source.setBirthDate(LocalDate.of(1990, 1, 2));
        source.setIdType("NATIONAL_ID");
        source.setIdNo("ID-1001");
        source.setPhone("13800000000");
        source.setAddress("测试地址");
        source.setPatientStatus("ACTIVE");

        Patient patient = FhirPatientMapper.toResource(source);

        assertEquals("Patient/1001", patient.getId());
        assertEquals(Enumerations.AdministrativeGender.MALE, patient.getGender());
        assertEquals(2, patient.getIdentifier().size());
        assertEquals("EMPI-1001", patient.getIdentifierFirstRep().getValue());
        assertEquals("张三", patient.getNameFirstRep().getText());
        assertEquals("13800000000", patient.getTelecomFirstRep().getValue());
        assertFalse(patient.getAddressFirstRep().getText().isBlank());
    }
}
