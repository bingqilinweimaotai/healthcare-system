package com.healthcare.mapper;

import com.healthcare.entity.AiMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AiMessageMapper {

    int insert(AiMessage entity);

    List<AiMessage> selectBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
