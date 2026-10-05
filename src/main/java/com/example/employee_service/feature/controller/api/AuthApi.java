package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.model.response.AuthResponse;
import com.example.employee_service.feature.model.response.TokenResponse;
import com.example.employee_service.feature.model.request.LoginRequest;
import com.example.employee_service.feature.model.request.RefreshTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/auth")
public interface AuthApi {
    /**
     * Xác thực thông tin đăng nhập và cấp access token để client gửi trong
     * header Authorization của các request tiếp theo.
     */
    @Operation(summary = "Đăng nhập")
    @PostMapping("/login")
    ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request);

    /**
     * Đổi refresh token hợp lệ lấy trong lần đăng nhập trước thành một cặp
     * access/refresh token mới mà không yêu cầu người dùng nhập lại mật khẩu.
     */
    @Operation(summary = "Làm mới phiên đăng nhập")
    @PostMapping("/refresh")
    ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request);

    /**
     * Thu hồi refresh token hiện tại; access token đã phát trước đó vẫn chỉ
     * sống đến thời điểm hết hạn vì hệ thống đang xác thực JWT stateless.
     */
    @Operation(summary = "Đăng xuất")
    @PostMapping("/logout")
    ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request);

    /**
     * Đọc thông tin user từ JWT đã được Spring Security xác thực; endpoint này
     * không nhận user ID từ client để tránh tin vào dữ liệu định danh do client tự cung cấp.
     */
    @Operation(summary = "Lấy thông tin người dùng")
    @GetMapping("/me")
    ApiResponse<AuthResponse> getCurrentUser(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt);


    @Operation(summary = "Tạo người dùng nội bộ")
    @PostMapping("/register")
    ApiResponse<String> createUser(@Valid @RequestBody UserCreateRequest request);
}
