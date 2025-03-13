package com.study.ngy.web.viewController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ViewController {

    /*
     *  thtmeleaf 일반 호출 에제
     */
    @RequestMapping("/SampleThymeleaf")
    public String sampleThymeleaf(){
        return "thymeleaf/index";
    }

    @RequestMapping("/bootstrapSampleThymeleaf")
    public String bootstrapSampleThymeleaf(){
        System.out.println("@@@@@@@@@@@@@@@@@ /bootstrapSampleThymeleaf");
        return "thymeleaf/bootstrapSample";
    }


}
