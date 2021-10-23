package com.fengzz.feign;

import com.fengzz.feign.service.FeignService;
import com.fengzz.feign.util.FeignRegister;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(FeignRegister.class)
public class DefinedFeginApplication {

    public static void main(String[] args) {
       ConfigurableApplicationContext context = SpringApplication.run(DefinedFeginApplication.class, args);
        FeignService feignService = context.getBean(FeignService.class);
        System.out.println(feignService.getSomeThing());
    }

}
