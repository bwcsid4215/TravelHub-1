package com.bwc.approval_workflow_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI workflowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Approval Workflow Service API")
                        .description("Handles approval workflows for Travel Requests and integrates with Employee, Policy, and Notification services.")
                        .version("1.0.0"));
    }
}
