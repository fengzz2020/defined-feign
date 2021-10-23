package com.fengzz.feign.controller;

import com.fengzz.feign.service.FeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fengzz
 */
@RestController
@RequestMapping(path = "/feign/service")
public class TestController {

    @Autowired
    FeignService feignService;

    @GetMapping("/index")
    public Object getIndex(){
        return feignService.getSomeThing();
    }
}
