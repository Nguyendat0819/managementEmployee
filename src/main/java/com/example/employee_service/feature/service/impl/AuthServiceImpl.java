package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.config.JwtProperties;
import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.common.response.DomainCode;
import com.example.employee_service.common.util.Const;
import com.example.employee_service.feature.entity.Users;
import com.example.employee_service.feature.model.request.LoginRequest;
import com.example.employee_service.feature.model.request.RefreshTokenRequest;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.model.response.AuthResponse;
import com.example.employee_service.feature.model.response.TokenResponse;
import com.example.employee_service.feature.model.response.UserResponse;
import com.example.employee_service.feature.repository.UsersRepository;
import com.example.employee_service.feature.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.employee_service.common.util.Const.IS_ACTIVE;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;
    // In-memory storage makes refresh-token rotation and logout effective for this instance;
    // a shared store is required when the service runs on multiple instances.
    private final Set<String> activeRefreshTokens = ConcurrentHashMap.newKeySet();

    @Override
    public TokenResponse login(LoginRequest request) {
        // Lấy user hiện tại trước khi phát token để xác thực mật khẩu và áp dụng
        // trạng thái tài khoản trước khi tạo một phiên đăng nhập mới.
        Users user = usersRepository.findByUserNameAndIsDeletedFalse(request.getUsername());
        if (user == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(DomainCode.UNAUTHORIZED);
        }
        if (!Const.IS_ACTIVE.equals(user.getStatus())) {
            throw new BusinessException(DomainCode.FORBIDDEN);
        }

        // Chỉ cập nhật thời điểm đăng nhập sau khi cả username, password và status đều hợp lệ.
        user.setLastLoginAt(java.time.LocalDateTime.now());
        usersRepository.save(user);
        return issueTokens(
                String.valueOf(user.getId()),
                user.getUserName(),
                user.getEmail(),
                List.of(Objects.requireNonNullElse(user.getRoleCode(), "USER")));
    }

    @Override
    public TokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Xóa trước khi xác thực và cấp token mới để cùng một refresh token không
        // thể được dùng lại sau khi rotation đã xảy ra.
        if (!activeRefreshTokens.remove(refreshToken)) {
            throw new BusinessException(DomainCode.UNAUTHORIZED);
        }

        try {
            // JwtDecoder đồng thời kiểm tra chữ ký và thời hạn; token_type phân biệt
            // refresh token với access token, tránh dùng nhầm access token để refresh.
            Jwt jwt = jwtDecoder.decode(refreshToken);
            if (!"refresh".equals(jwt.getClaimAsString("token_type"))) {
                throw new BusinessException(DomainCode.UNAUTHORIZED);
            }

            Users user = usersRepository.findByUserNameAndIsDeletedFalse(
                    jwt.getClaimAsString(Const.PREFERRED_USERNAME));
            // User có thể đã bị vô hiệu hóa sau khi refresh token được phát.
            if (user == null || !Const.IS_ACTIVE.equals(user.getStatus())) {
                throw new BusinessException(DomainCode.UNAUTHORIZED);
            }

            return issueTokens(
                    String.valueOf(user.getId()),
                    user.getUserName(),
                    user.getEmail(),
                    List.of(Objects.requireNonNullElse(user.getRoleCode(), "USER")));
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(DomainCode.UNAUTHORIZED);
        }
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        // Access token là JWT stateless nên chỉ refresh token có thể bị thu hồi
        // ngay tại service này; access token cũ vẫn chờ đến khi hết hạn.
        activeRefreshTokens.remove(request.getRefreshToken());
    }

    private TokenResponse issueTokens(String subject, String username, String email, List<String> roles) {
        Instant issuedAt = Instant.now();
        // Access token ngắn hạn dùng cho API; refresh token dài hạn chỉ dùng để
        // xin cặp token mới và được lưu để hỗ trợ rotation/revoke.
        String accessToken = encodeToken(subject, username, email, roles, "access", issuedAt,
                issuedAt.plus(jwtProperties.getAccessTokenTtl()));
        String refreshToken = encodeToken(subject, username, email, roles, "refresh", issuedAt,
                issuedAt.plus(jwtProperties.getRefreshTokenTtl()));
        activeRefreshTokens.add(refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenTtl().toSeconds())
                .build();
    }

    private String encodeToken(
            String subject,
            String username,
            String email,
            List<String> roles,
            String tokenType,
            Instant issuedAt,
            Instant expiresAt) {
        // Cùng một bộ claim được ký bằng secret của service, nhưng token_type
        // giúp các luồng access và refresh không bị sử dụng lẫn mục đích.
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .subject(subject)
                .claim("preferred_username", username)
                .claim("email", email)
                .claim("roles", roles)
                .claim("token_type", tokenType)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public AuthResponse getCurrentUser(Jwt jwt) {
        if (jwt == null) {
            throw new BusinessException(DomainCode.UNAUTHORIZED);
        }

        String userId = jwt.getSubject();
        String username = jwt.getClaimAsString(Const.PREFERRED_USERNAME);
        String email = jwt.getClaimAsString("email");
        List<String> roles = jwt.getClaimAsStringList(Const.ROLES);

        return AuthResponse.builder()
                .userId(userId)
                .user(username)
                .email(email)
                .roles(roles == null ? List.of() : roles)
                .build();
    }

    @Override
    public UserResponse create(UserCreateRequest request){
        // Tạo entity từ request tại service để áp dụng các giá trị mặc định trước khi lưu:
        // role USER khi thiếu role, trạng thái active và bản ghi chưa bị soft-delete.
        Users user = new Users();
        user.setEmail(request.getEmail());
        user.setUserName(request.getUsername());
        // Chỉ lưu password đã mã hóa; password gốc không được ghi vào entity/database.
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoleCode(request.getRoleCode() == null || request.getRoleCode().isBlank() ? "USER" : request.getRoleCode());
        user.setStatus(IS_ACTIVE);
        user.setIsDeleted(false);

        // Repository chịu trách nhiệm persist entity; sau khi save, dữ liệu được map
        // thành UserResponse để trả về lớp controller.
        Users saved = usersRepository.save(user);

        // Response chỉ chứa thông tin user cần công khai, không chứa passwordHash.
        return buildUser(saved);
    }

    private  UserResponse buildUser(Users user){
        return UserResponse.builder()
                .email(user.getEmail())
                .userName(user.getUserName())
                .roleCode(user.getRoleCode())
                .status(user.getStatus())
                .build();
    }
}
