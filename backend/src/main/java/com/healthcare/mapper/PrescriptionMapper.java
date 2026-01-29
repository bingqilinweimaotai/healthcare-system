package com.healthcare.mapper;

import com.healthcare.entity.Prescription;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PrescriptionMapper {

    int insert(Prescription entity);

    Prescription selectById(Long id);

    List<Prescription> selectByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<Prescription> selectByDoctorIdOrderByCreatedAtDesc(Long doctorId);

    List<Prescription> selectBySessionId(Long sessionId);
}
