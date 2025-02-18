package com.study.ngy.web.controller;

import com.study.ngy.web.model.Sample;
import com.study.ngy.web.repository.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// @Controller는 주로 View를 반환하기 위해 사용합니다
// @RestController는 데이터를 반환하기 위해 ResponseBody 어노테이션을 활용해주어야 합니다 이를 통해 Controller도 Json 형태로 데이터를 반환 함
@Controller
public class SampleController {

    @Autowired
    private SampleRepository sampleRepository;

    @GetMapping(value = "/test")
    public String index() {
        return "index.html";
    }

    /*
     *  thtmeleaf 일반 호출 에제
     */
    @RequestMapping("/SampleThymeleaf")
    public String sampleThymeleaf(Model model){
        System.out.println("@@@@@ SampleThymeleaf start @@@@@");
        String text = "타임리프에 전달되는 데이터입니다.";
        model.addAttribute("text",text);

        List<Sample> list =  sampleRepository.findAll();
        model.addAttribute("list",list);

        return "thymeleaf/index";
    }
}
