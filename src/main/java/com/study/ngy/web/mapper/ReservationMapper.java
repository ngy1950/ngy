package com.study.ngy.web.mapper;

import com.study.ngy.web.model.ReservationDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReservationMapper {
    int insertReservation (ReservationDTO param);
}
