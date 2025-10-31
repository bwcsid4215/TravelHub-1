// src/main/java/com/bwc/authservice/dto/AuthRequest.java
package com.bwc.authservice.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthRequest {
    private String email;
    private String password;
}
