package com.smarthis.patient.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.patient.entity.Patient;
import com.smarthis.patient.entity.PatientIdentifier;
import com.smarthis.patient.mapper.PatientIdentifierMapper;
import com.smarthis.patient.mapper.PatientMapper;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmpiMatcher {

    private final PatientMapper patientMapper;
    private final PatientIdentifierMapper identifierMapper;

    public static class MatchResult {
        public final Patient patient;
        public final boolean isNew;
        public final double score;

        public MatchResult(Patient patient, boolean isNew, double score) {
            this.patient = patient;
            this.isNew = isNew;
            this.score = score;
        }
    }

    public MatchResult match(String idType, String idNo, String name, String phone,
                             Integer gender, java.time.LocalDate birthDate) {
        // Step 1: Deterministic match by identifier
        if (idType != null && idNo != null) {
            LambdaQueryWrapper<PatientIdentifier> idQuery = new LambdaQueryWrapper<>();
            idQuery.eq(PatientIdentifier::getIdType, idType)
                    .eq(PatientIdentifier::getIdNo, idNo)
                    .eq(PatientIdentifier::getDeleted, 0);
            PatientIdentifier existingId = identifierMapper.selectOne(idQuery);
            if (existingId != null) {
                Patient patient = patientMapper.selectById(existingId.getPatientId());
                if (patient != null && patient.getDeleted() == 0) {
                    log.debug("Deterministic match found: idType={}, idNo={}, patientId={}",
                            idType, idNo, patient.getId());
                    return new MatchResult(patient, false, 100.0);
                }
            }
        }

        // Step 2: Probabilistic match by name + phone
        if (name != null && phone != null) {
            LambdaQueryWrapper<Patient> nameQuery = new LambdaQueryWrapper<>();
            nameQuery.eq(Patient::getName, name)
                    .eq(Patient::getDeleted, 0);
            List<Patient> candidates = patientMapper.selectList(nameQuery);

            for (Patient candidate : candidates) {
                double score = calculateScore(candidate, name, phone, gender, birthDate);
                if (score >= 80) {
                    log.debug("Probabilistic match: patientId={}, score={}", candidate.getId(), score);
                    return new MatchResult(candidate, false, score);
                }
                if (score >= 60) {
                    log.info("Possible duplicate: patientId={}, score={}", candidate.getId(), score);
                    return new MatchResult(candidate, false, score);
                }
            }
        }

        // Step 3: No match - return null to signal new patient creation
        log.debug("No match found, new patient will be created");
        return new MatchResult(null, true, 0);
    }

    private double calculateScore(Patient candidate, String name, String phone,
                                  Integer gender, java.time.LocalDate birthDate) {
        double score = 0;

        // Name exact match: 30 points
        if (candidate.getName() != null && candidate.getName().equals(name)) {
            score += 30;
        }

        // Gender match: 10 points
        if (gender != null && gender.equals(candidate.getGender())) {
            score += 10;
        }

        // Birth date match: 25 points
        if (birthDate != null && birthDate.equals(candidate.getBirthDate())) {
            score += 25;
        }

        // Phone match: 20 points
        if (candidate.getPhone() != null && candidate.getPhone().equals(phone)) {
            score += 20;
        }

        // Pinyin name partial: 15 points
        if (candidate.getNamePinyin() != null && name != null) {
            String candidatePinyin = candidate.getNamePinyin().toUpperCase();
            // Simple check - in production would use pinyin4j
            if (candidatePinyin.contains(name)) {
                score += 15;
            }
        }

        return score;
    }
}
