package com.tmalllet.web;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class FrontPageController {
    @GetMapping({"/", "index", "home"})
    public String home() {
        return "front/home";
    }

    @GetMapping("register")
    public String register() {
        return "front/register";
    }

    @GetMapping("registerSuccess")
    public String registerSuccess() {
        return "front/registerSuccess";
    }

    @GetMapping("login")
    public String login() {
        return "front/login";
    }

    @GetMapping("logout")
    public String logout() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            subject.logout();
        }
        return "redirect:home";
    }

    @GetMapping("/product")
    public String product() {
        return "front/product";
    }

    @GetMapping("/category")
    public String category() {
        return "front/category";
    }

    @GetMapping("/search")
    public String searchResult() {
        return "front/searchResult";
    }

    @GetMapping("/buy")
    public String buy() {
        return "front/buy";
    }

    @GetMapping("/cart")
    public String cart() {
        return "front/cart";
    }

    @GetMapping("/bought")
    public String bought() {
        return "front/bought";
    }


    @GetMapping("/alipay")
    public String alipay() {
        return "front/alipay";
    }

    @GetMapping("/confirmPay")
    public String confirmPay() {
        return "front/confirmPay";
    }


    @GetMapping("/orderConfirmed")
    public String orderConfirmed() {
        return "front/orderConfirmed";
    }

    @GetMapping("/payed")
    public String payed() {
        return "front/payed";
    }



    @GetMapping("/review")
    public String review() {
        return "front/review";
    }

}
