package com.healthcare.mapper;

import com.healthcare.entity.ConsultSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ConsultSessionMapper {

    int insert(ConsultSession entity);

    int update(ConsultSession entity);

    ConsultSession selectById(Long id);

    ConsultSession selectByIdForUpdate(Long id);

    List<ConsultSession> selectByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<ConsultSession> selectByDoctorIdOrderByUpdatedAtDesc(Long doctorId);

    List<ConsultSession> selectByStatusOrderByCreatedAtAsc(@Param("status") String status);

    List<ConsultSession> selectAll();

    long count();

    List<ConsultSession> selectWithDoctorCreatedSince(@Param("since") LocalDateTime since);

    long countCreatedSince(@Param("since") LocalDateTime since);
}
