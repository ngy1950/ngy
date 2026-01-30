package com.study.ngy.web.controller;

import com.study.ngy.web.service.KakaoTalkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kakao")
public class kakaoTalkController {

    @Autowired
    private final KakaoTalkService kakaoTalkService;

    public kakaoTalkController(KakaoTalkService kakaoTalkService) {
        this.kakaoTalkService = kakaoTalkService;
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam String phoneNumber, @RequestParam String message) {
        return kakaoTalkService.sendKakaoMessage(phoneNumber, message);
    }
}
