package com.healthcare.mapper;

import com.healthcare.entity.Drug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DrugMapper {

    int insert(Drug entity);

    int update(Drug entity);

    Drug selectById(Long id);

    List<Drug> selectByStatusOrderByNameAsc(@Param("status") String status);

    List<Drug> selectAll();

    long count();
}
