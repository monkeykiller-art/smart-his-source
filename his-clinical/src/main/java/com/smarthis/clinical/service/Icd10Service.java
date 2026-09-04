package com.smarthis.clinical.service;

import com.smarthis.clinical.dto.request.Icd10QueryRequest;
import com.smarthis.clinical.dto.response.Icd10Vo;
import com.smarthis.common.model.PageResult;

public interface Icd10Service {

    PageResult<Icd10Vo> search(Icd10QueryRequest request);

    Icd10Vo getById(Long id);
}
