package com.smarthis.patient.fhir;

import com.smarthis.patient.dto.response.PatientVo;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;

import java.sql.Date;

/** Maps the local EMPI record to the FHIR R4 Patient resource used at integration boundaries. */
public final class FhirPatientMapper {

    private static final String IDENTIFIER_SYSTEM = "https://smarthis.cn/fhir/identifier/";

    private FhirPatientMapper() {
    }

    public static Patient toResource(PatientVo source) {
        Patient patient = new Patient();
        patient.setId("Patient/" + source.getId());
        patient.setActive(!"INACTIVE".equalsIgnoreCase(source.getPatientStatus()));

        addIdentifier(patient, "empi", source.getEmpiNo());
        addIdentifier(patient, source.getIdType(), source.getIdNo());

        if (hasText(source.getName())) {
            patient.addName(new HumanName().setText(source.getName()).setUse(HumanName.NameUse.OFFICIAL));
        }
        patient.setGender(toGender(source.getGender()));
        if (source.getBirthDate() != null) {
            patient.setBirthDate(Date.valueOf(source.getBirthDate()));
        }
        if (hasText(source.getPhone())) {
            patient.addTelecom(new ContactPoint()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setUse(ContactPoint.ContactPointUse.MOBILE)
                    .setValue(source.getPhone()));
        }
        if (hasText(source.getAddress())) {
            patient.addAddress(new Address().setUse(Address.AddressUse.HOME).setText(source.getAddress()));
        }
        return patient;
    }

    private static void addIdentifier(Patient patient, String type, String value) {
        if (hasText(type) && hasText(value)) {
            patient.addIdentifier(new Identifier()
                    .setUse(Identifier.IdentifierUse.USUAL)
                    .setSystem(IDENTIFIER_SYSTEM + type.toLowerCase())
                    .setValue(value));
        }
    }

    private static Enumerations.AdministrativeGender toGender(Integer gender) {
        if (gender == null) {
            return Enumerations.AdministrativeGender.UNKNOWN;
        }
        return switch (gender) {
            case 1 -> Enumerations.AdministrativeGender.MALE;
            case 2 -> Enumerations.AdministrativeGender.FEMALE;
            default -> Enumerations.AdministrativeGender.UNKNOWN;
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
