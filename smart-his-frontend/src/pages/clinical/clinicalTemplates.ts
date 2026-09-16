export interface MedicalRecordTemplate {
  title: string
  chiefComplaint: string
  presentIllness: string
  physicalExam: string
  diagnosisDesc: string
  treatmentPlan: string
}

export const recordTemplates: Record<string, MedicalRecordTemplate> = {
  COMMON_COLD: { title: '门诊病历', chiefComplaint: '咳嗽、咽痛', presentIllness: '患者诉咳嗽、咽痛，症状持续 3 天，无明显加重。', physicalExam: '咽部充血，双肺呼吸音清，未闻及明显干湿性啰音。', diagnosisDesc: '急性上呼吸道感染', treatmentPlan: '对症治疗，充分休息，必要时复诊。' },
  HYPERTENSION: { title: '高血压复诊病历', chiefComplaint: '高血压复诊', presentIllness: '患者规律服药后复诊，自测血压较前平稳，无头晕、胸闷等不适。', physicalExam: '一般情况可，心肺查体未见明显异常。', diagnosisDesc: '原发性高血压', treatmentPlan: '继续当前方案，监测血压，低盐饮食，定期复诊。' },
  DIABETES: { title: '糖尿病复诊病历', chiefComplaint: '糖尿病复诊', presentIllness: '患者糖尿病复诊，近期饮食及用药基本规律，无明显低血糖症状。', physicalExam: '一般情况可，双下肢无明显水肿。', diagnosisDesc: '2 型糖尿病', treatmentPlan: '继续控糖治疗，监测血糖，控制饮食并适量运动。' },
}
