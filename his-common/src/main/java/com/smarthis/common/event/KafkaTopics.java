package com.smarthis.common.event;

public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String PATIENT_REGISTERED = "his.patient.registered";
    public static final String PATIENT_ADMITTED = "his.patient.admitted";
    public static final String PATIENT_DISCHARGED = "his.patient.discharged";

    public static final String CLINICAL_PRESCRIPTION_CREATED = "his.clinical.prescription.created";
    public static final String CLINICAL_ORDER_PLACED = "his.clinical.order.placed";
    public static final String CLINICAL_CRITICAL_VALUE = "his.clinical.critical.value";
    public static final String CLINICAL_EXAM_REPORTED = "his.clinical.exam.reported";

    public static final String OPERATIONS_BILL_CREATED = "his.operations.bill.created";
    public static final String OPERATIONS_BILL_SETTLED = "his.operations.bill.settled";
    public static final String OPERATIONS_BILL_REFUNDED = "his.operations.bill.refunded";

    public static final String RESOURCE_DISPENSE_COMPLETED = "his.resource.dispense.completed";
    public static final String RESOURCE_STOCK_LOW = "his.resource.stock.low";

    public static final String PHARMA_REVIEW_COMPLETED = "his.pharma.review.completed";

    public static final String EMERGENCY_TRIAGE_ASSIGNED = "his.emergency.triage.assigned";
    public static final String EMERGENCY_GREENCHANNEL_ACTIVATED = "his.emergency.greenchannel.activated";

    public static final String DRG_CASE_GROUPED = "his.drg.case.grouped";

    public static final String PLATFORM_AUDIT = "his.platform.audit";
    public static final String PLATFORM_EXCHANGE_DLQ = "his.platform.exchange.dlq";
}
