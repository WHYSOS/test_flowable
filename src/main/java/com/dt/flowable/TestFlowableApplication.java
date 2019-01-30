package com.dt.flowable;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.dt.flowable.dao")
public class TestFlowableApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestFlowableApplication.class, args);
    }

}

