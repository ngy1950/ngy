package com.study.ngy.web;

import com.study.ngy.domain.reservation.Reservation;
import com.study.ngy.domain.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationRepository reservationRepository;

    @PostMapping("/api/v1/reservation.do")
    public String reservation(@RequestBody Map<String, String> body) {
        try {
            String name = body.getOrDefault("name", "").trim();
            String phone = body.getOrDefault("phone", "").trim();
            if (name.isBlank() || phone.isBlank()) return "N";

            Reservation r = new Reservation();
            r.setName(name);
            r.setPhone(phone);
            r.setMemo(body.getOrDefault("memo", "").trim());
            reservationRepository.save(r);
            return "Y";
        } catch (Exception e) {
            return "N";
        }
    }
}
