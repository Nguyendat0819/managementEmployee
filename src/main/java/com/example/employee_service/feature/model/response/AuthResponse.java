package com.example.employee_service.feature.model.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AuthResponse {
    String user;
    String email;
    String firstName;
    String lastName;
    List<String> roles;
}