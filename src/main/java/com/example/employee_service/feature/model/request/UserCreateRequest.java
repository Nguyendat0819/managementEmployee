package com.example.employee_service.feature.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class UserCreateRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(max = 100, message = "Username tối đa 100 ký tự")
    String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 255, message = "Email tối đa 255 ký tự")
    String email;

    @Size(max = 100, message = "FirstName tối đa 100 ký tự")
    String firstName;

    @Size(max = 100, message = "LastName tối đa 100 ký tự")
    String lastName;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu từ 6 đến 100 ký tự")
    String password;

    String roleCode;
}
