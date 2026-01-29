package com.healthcare.mapper;

import com.healthcare.entity.AiSession;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AiSessionMapper {

    int insert(AiSession entity);

    AiSession selectById(Long id);

    List<AiSession> selectByUserIdOrderByCreatedAtDesc(Long userId);
}
