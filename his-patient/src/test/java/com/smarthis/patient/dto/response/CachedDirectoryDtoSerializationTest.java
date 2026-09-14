package com.smarthis.patient.dto.response;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class CachedDirectoryDtoSerializationTest {

    @Test
    void cachedDepartmentAndDoctorValuesSupportJdkSerialization() throws Exception {
        DepartmentVo department = new DepartmentVo();
        department.setId(1L);
        department.setDeptCode("IM");
        department.setDeptName("内科");

        DoctorVo doctor = new DoctorVo();
        doctor.setId(2L);
        doctor.setEmployeeNo("D002");
        doctor.setDoctorName("测试医生");
        doctor.setDeptId(department.getId());

        assertThat(roundTrip(department)).isEqualTo(department);
        assertThat(roundTrip(doctor)).isEqualTo(doctor);
    }

    private Object roundTrip(Object value) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(value);
        }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return input.readObject();
        }
    }
}
