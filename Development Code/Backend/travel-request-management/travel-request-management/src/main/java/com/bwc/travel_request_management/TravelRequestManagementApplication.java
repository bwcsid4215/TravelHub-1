package com.bwc.travel_request_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.bwc.travel_request_management.client")
public class TravelRequestManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelRequestManagementApplication.class, args);
    }
}
