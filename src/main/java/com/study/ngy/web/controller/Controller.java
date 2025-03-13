package com.study.ngy.web.controller;

import com.study.ngy.web.model.ReservationDTO;
import com.study.ngy.web.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// @Controller는 주로 View를 반환하기 위해 사용합니다
// @RestController는 데이터를 반환하기 위해 ResponseBody 어노테이션을 활용해주어야 합니다 이를 통해 Controller도 Json 형태로 데이터를 반환 함

@RestController
@RequestMapping("/api/v1")
public class Controller {

    @Autowired
    private ReservationService reservationService;

    @RequestMapping("/reservation.do")
    public String samplePost(HttpServletRequest httpServletRequest, @RequestBody ReservationDTO reservationDTO){
        System.out.println("@@@@@@@@@@@@@@@@ /samplePost");
        String rs = "N";

        reservationDTO.setIp(httpServletRequest.getRemoteAddr());
        rs = reservationService.insertReservation(reservationDTO);

        // JPA 예제
        //List<Sample> list =  repository.findAll();
        //mav.addObject("list",list);

        return rs;
    }
}
