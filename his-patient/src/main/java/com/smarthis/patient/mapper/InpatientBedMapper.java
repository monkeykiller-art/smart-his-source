package com.smarthis.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.patient.entity.InpatientBed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InpatientBedMapper extends BaseMapper<InpatientBed> {
    @Update("UPDATE pat_inpatient_bed SET bed_status = 'OCCUPIED', current_admission_id = #{admissionId}, "
            + "updated_time = CURRENT_TIMESTAMP WHERE id = #{bedId} AND deleted = 0 AND bed_status = 'AVAILABLE'")
    int occupy(@Param("bedId") Long bedId, @Param("admissionId") Long admissionId);

    @Update("UPDATE pat_inpatient_bed SET bed_status = 'AVAILABLE', current_admission_id = NULL, "
            + "updated_time = CURRENT_TIMESTAMP WHERE id = #{bedId} AND deleted = 0 AND current_admission_id = #{admissionId}")
    int release(@Param("bedId") Long bedId, @Param("admissionId") Long admissionId);
}
