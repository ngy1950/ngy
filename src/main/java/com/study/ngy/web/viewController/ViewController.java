package com.study.ngy.web.viewController;

import com.study.ngy.domain.review.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ViewController {

    @Autowired
    private ReviewRepository reviewRepository;

    /*
     * thtmeleaf 일반 호출 에제
     */
    @RequestMapping("/")
    public String index(Model model) {
        System.out.println("@@@@@@@@@@@@@@@@@ index");
        model.addAttribute("recentReviews", reviewRepository.findTop3ByApprovedTrueOrderByCreatedAtDesc());
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

    @GetMapping("/menu/sije")
    public String menuSije() {
        return "menu/sije";
    }

}
