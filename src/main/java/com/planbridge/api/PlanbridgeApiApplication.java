package com.planbridge.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PlanbridgeApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlanbridgeApiApplication.class, args);
    }
}
