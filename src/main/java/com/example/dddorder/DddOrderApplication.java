package com.example.dddorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Spring Boot 启动类，扫描当前包及子包下所有 @Component/@Service/@Repository 等注解
@SpringBootApplication
public class DddOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DddOrderApplication.class, args);
    }
}
