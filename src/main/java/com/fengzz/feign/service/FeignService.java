package com.fengzz.feign.service;


import com.fengzz.feign.annotation.FeginClinet;
import com.fengzz.feign.annotation.FeignGet;
import org.springframework.stereotype.Component;

/**
 * @author fengzz
 */
@Component
@FeginClinet(baseUrl = "http://www.baidu.com:80")
public interface FeignService {

    @FeignGet(url = "/index.html")
    Object getSomeThing();
}
