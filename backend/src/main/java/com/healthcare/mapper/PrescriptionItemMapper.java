package com.healthcare.mapper;

import com.healthcare.entity.PrescriptionItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PrescriptionItemMapper {

    int insert(PrescriptionItem entity);

    List<PrescriptionItem> selectByPrescriptionId(Long prescriptionId);
}
