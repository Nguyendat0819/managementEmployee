package com.example.employee_service.feature.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CredentialRepresentation {

    @Builder.Default
    String type = "password";

    String value;

    @Builder.Default
    Boolean temporary = false;
}
