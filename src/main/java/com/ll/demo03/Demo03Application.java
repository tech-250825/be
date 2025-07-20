package com.ll.demo03;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EntityScan(basePackages = {"com.ll.demo03.UGC.infrastructure"})
@EnableAsync
@EnableCaching
@EnableJpaAuditing
@SpringBootApplication
public class Demo03Application {

    public static void main (String[] args) {
        SpringApplication.run(Demo03Application.class, args);
    }

}