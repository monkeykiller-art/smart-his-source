package com.smarthis.common.model;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "bad request"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    NOT_FOUND(404, "not found"),
    INTERNAL_ERROR(500, "internal server error"),

    // Auth 5xxx
    AUTH_LOGIN_FAILED(5001, "invalid username or password"),
    AUTH_ACCOUNT_LOCKED(5002, "account locked, please try later"),
    AUTH_TOKEN_EXPIRED(5003, "token expired"),
    AUTH_TOKEN_INVALID(5004, "invalid token"),
    AUTH_NO_PERMISSION(5005, "insufficient permissions"),
    AUTH_REFRESH_FAILED(5006, "refresh token expired or invalid"),
    AUTH_CAPTCHA_ERROR(5007, "captcha verification failed"),
    AUTH_PASSWORD_SAME(5008, "new password cannot be same as old"),

    // Patient 1xxx
    PATIENT_NOT_FOUND(1001, "patient not found"),
    PATIENT_DUPLICATE(1002, "duplicate patient identity"),
    APPOINTMENT_CONFLICT(1003, "appointment time conflict"),
    SCHEDULE_NOT_FOUND(1004, "schedule not found"),
    SCHEDULE_NO_QUOTA(1005, "no available quota"),
    REGISTRATION_NOT_FOUND(1006, "registration not found"),
    REGISTRATION_ALREADY_PAID(1007, "registration already paid"),
    REGISTRATION_CANCELLED(1008, "registration already cancelled"),
    REGISTRATION_DUPLICATE(1017, "patient already has an active registration for this schedule"),
    REGISTRATION_PAYMENT_REQUIRED(1018, "paid registration must be refunded instead of cancelled"),
    ENCOUNTER_NOT_FOUND(1009, "encounter not found"),
    ENCOUNTER_CLOSED(1010, "encounter is closed"),
    TRIAGE_NOT_FOUND(1011, "triage record not found"),
    DEPARTMENT_NOT_FOUND(1012, "department not found"),
    DOCTOR_NOT_FOUND(1013, "doctor not found"),
    ADMISSION_NOT_FOUND(1014, "admission not found"),
    ADMISSION_STATUS_INVALID(1015, "invalid admission status for this operation"),
    BED_NOT_AVAILABLE(1016, "bed not available"),

    // Clinical 2xxx
    ORDER_INVALID(2001, "invalid order"),
    ORDER_DUPLICATE(2002, "duplicate order"),
    PRESCRIPTION_CHECK_FAILED(2003, "prescription safety check failed"),
    CRITICAL_VALUE_UNACK(2004, "critical value not acknowledged"),
    MEDICAL_RECORD_NOT_FOUND(2005, "medical record not found"),
    MEDICAL_RECORD_SIGNED(2006, "medical record already signed"),
    DIAGNOSIS_REQUIRED(2007, "at least one confirmed diagnosis required"),
    DIAGNOSIS_PRIMARY_MISSING(2008, "exactly one primary diagnosis required"),
    PRESCRIPTION_NOT_FOUND(2009, "prescription not found"),
    ANTIBIOTIC_LEVEL_DENIED(2010, "antibiotic prescribing level exceeded"),
    ORDER_NOT_VERIFIED(2011, "order not yet verified by nurse"),
    ORDER_STATUS_INVALID(2012, "invalid order status for this operation"),
    EXAM_REQUEST_NOT_FOUND(2013, "exam request not found"),
    ICD10_NOT_FOUND(2014, "ICD-10 code not found"),
    SKIN_TEST_REQUIRED(2015, "skin test required before this drug"),

    // Operations/Billing 3xxx
    BILLING_ERROR(3001, "billing calculation error"),
    INSURANCE_REJECTED(3002, "insurance claim rejected"),
    DRG_GROUP_FAILED(3003, "DRG grouping failed"),
    BILL_NOT_FOUND(3004, "bill not found"),
    BILL_STATUS_INVALID(3005, "invalid bill status for this operation"),
    BILL_ALREADY_SETTLED(3006, "bill already settled"),
    PAYMENT_AMOUNT_MISMATCH(3007, "payment amount does not match bill"),
    PAYMENT_FAILED(3008, "payment processing failed"),
    INSURANCE_NOT_VERIFIED(3009, "insurance registration not verified"),
    PREPAID_INSUFFICIENT(3010, "prepaid account balance insufficient"),
    INVOICE_ALREADY_ISSUED(3011, "invoice already issued"),
    CHARGE_ITEM_NOT_FOUND(3012, "charge item not found"),
    REFUND_AMOUNT_EXCEEDED(3013, "refund amount exceeds original payment"),

    // Resource/Pharmacy 4xxx
    DRUG_NOT_FOUND(4001, "drug not found in catalog"),
    STOCK_INSUFFICIENT(4002, "insufficient stock quantity"),
    BATCH_EXPIRED(4003, "drug batch expired"),
    DISPENSE_NOT_FOUND(4004, "dispense record not found"),
    DISPENSE_STATUS_INVALID(4005, "invalid dispense status"),
    STORAGE_NOT_FOUND(4006, "storage not found"),
    SUPPLIER_NOT_FOUND(4007, "supplier not found"),
    WARD_NOT_FOUND(4008, "ward not found"),
    BED_NOT_FOUND(4009, "bed not found"),
    BED_OCCUPIED(4010, "bed already occupied"),

    // Pharma/CDSS 6xxx
    RX_REVIEW_REJECTED(6001, "prescription rejected by safety review"),
    RX_REVIEW_WARNING(6002, "prescription has safety warnings"),
    DRUG_INTERACTION(6003, "drug interaction detected"),
    DRUG_ALLERGY(6004, "drug allergy contraindication"),
    DOSE_EXCEEDED(6005, "dose exceeds maximum limit"),
    CDSS_ALERT_NOT_FOUND(6006, "CDSS alert not found"),
    PATHWAY_VARIANCE(6007, "clinical pathway variance detected"),

    // Emergency 7xxx
    TRIAGE_LEVEL_INVALID(7001, "invalid triage level"),
    GREEN_CHANNEL_NOT_FOUND(7002, "green channel record not found"),
    RESUSCITATION_NOT_FOUND(7003, "resuscitation record not found"),

    // Platform 8xxx
    MASTER_DATA_NOT_FOUND(8001, "master data not found"),
    EXCHANGE_MESSAGE_FAILED(8002, "exchange message processing failed"),
    DATA_QUALITY_CHECK_FAILED(8003, "data quality check failed");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
