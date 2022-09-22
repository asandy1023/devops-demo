package org.example.devops.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author JQ
 */
@RestController
@RequestMapping("/")
public class DevopsTestController {

    @GetMapping("/test")
    public String devopsTest() {
        return "自動化發佈示範工程測試接口->OK!";
    }
}
