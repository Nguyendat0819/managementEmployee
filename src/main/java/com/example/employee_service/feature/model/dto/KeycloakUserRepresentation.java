package com.example.employee_service.feature.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakUserRepresentation {

    String id;
    String username;
    String email;
    String firstName;
    String lastName;

    @Builder.Default
    Boolean enabled = true;

    @Builder.Default
    Boolean emailVerified = true;

    List<CredentialRepresentation> credentials;
    List<String> realmRoles;
    Map<String, List<String>> attributes;
}
