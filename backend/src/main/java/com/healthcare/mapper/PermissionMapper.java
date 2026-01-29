package com.healthcare.mapper;

import com.healthcare.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper {

    List<Permission> selectByRole(@Param("role") String role);

    Permission selectByCode(@Param("code") String code);

    List<Permission> selectAll();
}
