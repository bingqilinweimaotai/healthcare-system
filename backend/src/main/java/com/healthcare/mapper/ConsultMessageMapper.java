package com.healthcare.mapper;

import com.healthcare.entity.ConsultMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ConsultMessageMapper {

    int insert(ConsultMessage entity);

    List<ConsultMessage> selectBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
