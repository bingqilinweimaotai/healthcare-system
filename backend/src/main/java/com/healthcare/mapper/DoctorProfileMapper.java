package com.healthcare.mapper;

import com.healthcare.entity.DoctorProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DoctorProfileMapper {

    int insert(DoctorProfile entity);

    int update(DoctorProfile entity);

    int updateAuditStatus(@Param("id") Long id, @Param("auditStatus") String auditStatus);

    DoctorProfile selectById(Long id);

    DoctorProfile selectByUserId(Long userId);

    List<DoctorProfile> selectAll();

    long count();
}
