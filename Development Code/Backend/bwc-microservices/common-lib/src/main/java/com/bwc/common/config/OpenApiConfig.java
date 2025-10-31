package com.bwc.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Unknown Service}")
    private String serviceName;

    @Value("${info.app.version:1.0.0}")
    private String version;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${swagger.description:REST API for this service}")
    private String apiDescription;

    // Comma-separated list of environments (example: local,http://dev.mycompany.com,http://qa.mycompany.com)
    @Value("${swagger.servers:}")
    private String[] additionalServers;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(createServers())
                .components(securityComponents());
    }

    private Info apiInfo() {
        return new Info()
                .title(serviceName + " API")
                .description(apiDescription)
                .version(version)
                .contact(new Contact()
                        .name("BWC API Support")
                        .email("api-support@bwc.com")
                        .url("https://api.bwc.com/support"))
                .license(new License()
                        .name("Commercial License")
                        .url("https://www.bwc.com/license"));
    }

    private List<Server> createServers() {
        List<Server> servers = new ArrayList<>();

        // Local server
        servers.add(new Server()
                .url("http://localhost:" + serverPort + contextPath)
                .description("Local Development Environment"));

        // LAN server
        servers.add(new Server()
                .url("http://" + getCurrentIpAddress() + ":" + serverPort + contextPath)
                .description("LAN Access (Current Machine IP)"));

        // Additional environments from properties
        if (additionalServers != null) {
            for (String envUrl : additionalServers) {
                if (envUrl != null && !envUrl.isBlank()) {
                    servers.add(new Server().url(envUrl.trim() + contextPath).description("Configured Environment"));
                }
            }
        }

        return servers;
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("BearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT authentication (Future implementation)"));
    }

    private String getCurrentIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "192.168.1.220"; // fallback static IP
        }
    }
}
