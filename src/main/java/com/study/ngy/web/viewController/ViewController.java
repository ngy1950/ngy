package com.study.ngy.web.viewController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ViewController {

    /*
     * thtmeleaf 일반 호출 에제
     */
    @RequestMapping("/")
    public String index() {
        System.out.println("@@@@@@@@@@@@@@@@@ index");
        return "index";
    }

    // --- 추가된 상세 메뉴 페이지 라우팅 ---
    @GetMapping("/menu/jesa")
    public String menuJesa() {
        return "menu/jesa";
    }

    @GetMapping("/menu/jangji")
    public String menuJangji() {
        return "menu/jangji";
    }

    @GetMapping("/menu/group")
    public String menuGroup() {
        return "menu/group";
    }

    @GetMapping("/menu/gallery")
    public String menuGallery() {
        return "menu/gallery";
    }

}
