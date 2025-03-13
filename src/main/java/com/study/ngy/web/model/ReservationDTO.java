package com.study.ngy.web.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ReservationDTO {
    private int num;

    private String name;

    private String phone;

    private String memo;

    private String ip;

    private String kakaoSendYn;

    public void setIp() {
    }
}
