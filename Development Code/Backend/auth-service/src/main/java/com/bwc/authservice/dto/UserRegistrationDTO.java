// src/main/java/com/bwc/authservice/dto/UserRegistrationDTO.java
package com.bwc.authservice.dto;

import lombok.Data;

@Data
public class UserRegistrationDTO {
    private String email;
    private String password;
    private String employeeId;
}
