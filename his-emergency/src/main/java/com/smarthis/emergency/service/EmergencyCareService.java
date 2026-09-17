package com.smarthis.emergency.service;
import com.smarthis.emergency.dto.request.*;
import com.smarthis.emergency.entity.ObservationRecord;
import com.smarthis.emergency.entity.ResuscitationRecord;
import java.util.List;
public interface EmergencyCareService {
    ResuscitationRecord startResuscitation(ResuscitationCreateRequest request);
    ResuscitationRecord completeResuscitation(Long id, ResuscitationCompleteRequest request);
    ObservationRecord admitObservation(ObservationCreateRequest request);
    ObservationRecord dischargeObservation(Long id, ObservationDischargeRequest request);
    List<ResuscitationRecord> resuscitations(Long patientId);
    List<ObservationRecord> observations(Long patientId);
}
