package com.example.yiliaoxitong.controller;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class wangye {
    @GetMapping("/hello")
    public String hello(){
        return "医疗系统 Hello World";
    }
}


