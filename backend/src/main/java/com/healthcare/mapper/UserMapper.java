package com.healthcare.mapper;

import com.healthcare.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表数据访问（Mapper 层，替代原 JPA Repository）
 */
@Mapper
public interface UserMapper {

    int insert(User user);

    int update(User user);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    User selectById(Long id);

    User selectByUsername(String username);

    int countByUsername(String username);

    List<User> selectAll();

    long count();
}
