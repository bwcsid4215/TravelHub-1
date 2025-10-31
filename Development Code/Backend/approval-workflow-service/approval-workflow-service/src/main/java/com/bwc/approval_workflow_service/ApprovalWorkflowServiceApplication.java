package com.bwc.approval_workflow_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.bwc.approval_workflow_service.client")
@EnableAsync
@EnableScheduling
@EnableCaching
public class ApprovalWorkflowServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApprovalWorkflowServiceApplication.class, args);
    }
}