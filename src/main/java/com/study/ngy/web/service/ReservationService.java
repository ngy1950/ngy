package com.study.ngy.web.service;

import com.study.ngy.web.mapper.ReservationMapper;
import com.study.ngy.web.model.ReservationDTO;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class ReservationService {

    @Autowired
    private ReservationMapper reservationMapper;


    public String insertReservation (ReservationDTO param) {
        String rs = "N";

        int cnt = reservationMapper.insertReservation(param);
        rs = cnt > 0 ? "Y" : "N";

        return rs;

    }
}
