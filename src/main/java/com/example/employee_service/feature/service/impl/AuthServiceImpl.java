package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.config.JwtProperties;
import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.common.response.DomainCode;
import com.example.employee_service.common.util.Const;
import com.example.employee_service.feature.entity.Users;
import com.example.employee_service.feature.entity.Employees;
import com.example.employee_service.feature.entity.PasswordResetRequest;
import com.example.employee_service.feature.entity.Roles;
import com.example.employee_service.feature.constant.enums.UsersEnum;
import com.example.employee_service.feature.model.request.ChangePasswordRequest;
import com.example.employee_service.feature.model.request.LoginRequest;
import com.example.employee_service.feature.model.request.RefreshTokenRequest;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.model.response.AuthResponse;
import com.example.employee_service.feature.model.response.TokenResponse;
import com.example.employee_service.feature.model.response.UserResponse;
import com.example.employee_service.feature.model.response.PasswordResetRequestResponse;
import com.example.employee_service.feature.repository.UsersRepository;
import com.example.employee_service.feature.repository.EmployeesRepository;
import com.example.employee_service.feature.repository.PasswordResetRequestRepository;
import com.example.employee_service.feature.repository.RolesRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.employee_service.common.util.Const.IS_ACTIVE;
import static com.example.employee_service.common.util.Const.IS_INACTIVE;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsersRepository usersRepository;
    private final EmployeesRepository employeesRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final RolesRepository rolesRepository;
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
            if (user == null || !Const.IS_ACTIVE.equals(user.getStatus())
                    || accountChangedAfter(user, jwt.getIssuedAt())) {
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
    @Transactional
    public UserResponse create(UserCreateRequest request){
        String roleCode = request.getRoleCode() == null || request.getRoleCode().isBlank()
                ? "USER"
                : request.getRoleCode().trim().toUpperCase();
        String status = request.getStatus() == null || request.getStatus().isBlank()
                ? IS_ACTIVE
                : request.getStatus();
        return createWithRole(request, roleCode, status);
    }

    @Override
    @Transactional
    public UserResponse register(UserCreateRequest request) {
        return createWithRole(request, "USER", IS_ACTIVE);
    }

    @Override
    @Transactional
    public void changePassword(Jwt jwt, ChangePasswordRequest request) {
        if (jwt == null) {
            throw new BusinessException(DomainCode.UNAUTHORIZED);
        }

        Long userId;
        try {
            userId = Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new BusinessException(DomainCode.UNAUTHORIZED);
        }

        Users user = usersRepository.findById(userId)
                .filter(account -> !Boolean.TRUE.equals(account.getIsDeleted()))
                .orElseThrow(() -> new BusinessException(DomainCode.UNAUTHORIZED));
        if (!IS_ACTIVE.equals(user.getStatus())) {
            throw new BusinessException(DomainCode.FORBIDDEN);
        }
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException(DomainCode.UNAUTHORIZED);
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(DomainCode.VALIDATION_ERROR, "mật khẩu mới phải khác mật khẩu hiện tại");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setAuthStateChangedAt(Instant.now());
        usersRepository.save(user);
        resolvePendingPasswordResetRequests(user.getId());
        revokeRefreshTokensForUser(user.getId());
    }

    @Override
    @Transactional
    public void adminResetPassword(String userName, String newPassword) {
        Users user = usersRepository.findByUserNameAndIsDeletedFalse(userName);
        if (user == null) {
            throw new BusinessException(DomainCode.NOT_FOUND, "tài khoản " + userName);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setAuthStateChangedAt(Instant.now());
        usersRepository.save(user);
        resolvePendingPasswordResetRequests(user.getId());
        revokeRefreshTokensForUser(user.getId());
    }

    @Override
    @Transactional
    public UserResponse adminUpdateUserRole(String userName, String roleCode) {
        Users user = usersRepository.findByUserNameAndIsDeletedFalse(userName);
        if (user == null) {
            throw new BusinessException(DomainCode.NOT_FOUND, "tài khoản " + userName);
        }

        String normalizedRoleCode = roleCode.trim().toUpperCase(Locale.ROOT);
        Roles role = rolesRepository.findByRoleCodeIgnoreCaseAndIsDeletedFalse(normalizedRoleCode)
                .orElseThrow(() -> new BusinessException(DomainCode.NOT_FOUND, "vai trò " + normalizedRoleCode));
        String nextRoleCode = role.getRoleCode();
        if (nextRoleCode.equalsIgnoreCase(user.getRoleCode())) {
            return buildUser(user);
        }

        if ("ADMIN".equalsIgnoreCase(user.getRoleCode())
                && !"ADMIN".equalsIgnoreCase(nextRoleCode)
                && IS_ACTIVE.equals(user.getStatus())
                && usersRepository.countByRoleCodeAndStatusAndIsDeletedFalse("ADMIN", IS_ACTIVE) <= 1) {
            throw new BusinessException(DomainCode.CONFLICT, "không thể đổi vai trò của quản trị viên đang hoạt động cuối cùng");
        }

        user.setRoleCode(nextRoleCode);
        user.setAuthStateChangedAt(Instant.now());
        usersRepository.save(user);
        revokeRefreshTokensForUser(user.getId());
        return buildUser(user);
    }

    @Override
    @Transactional
    public UserResponse adminUpdateUserStatus(String userName, UsersEnum action) {
        Users user = usersRepository.findByUserNameAndIsDeletedFalse(userName);
        if (user == null) {
            throw new BusinessException(DomainCode.NOT_FOUND, "tài khoản " + userName);
        }

        if (action == UsersEnum.REVOKE) {
            if (userName.equalsIgnoreCase(com.example.employee_service.common.util.SecurityUtil.getCurrentUsername())) {
                throw new BusinessException(DomainCode.CONFLICT, "không thể tự khóa tài khoản đang đăng nhập");
            }
            if ("ADMIN".equalsIgnoreCase(user.getRoleCode()) && IS_ACTIVE.equals(user.getStatus())
                    && usersRepository.countByRoleCodeAndStatusAndIsDeletedFalse("ADMIN", IS_ACTIVE) <= 1) {
                throw new BusinessException(DomainCode.CONFLICT, "không thể khóa quản trị viên đang hoạt động cuối cùng");
            }
            user.setStatus(IS_INACTIVE);
            user.setAuthStateChangedAt(Instant.now());
            revokeRefreshTokensForUser(user.getId());
        } else if (action == UsersEnum.RESTORE) {
            user.setStatus(IS_ACTIVE);
            user.setAuthStateChangedAt(Instant.now());
        } else {
            throw new BusinessException(DomainCode.INVALID_PARAMETER, "action");
        }

        usersRepository.save(user);
        return buildUser(user);
    }

    @Override
    @Transactional
    public void requestAdminPasswordReset(String email) {
        Users user = findPasswordResetAccount(email.trim());
        if (user == null || !IS_ACTIVE.equals(user.getStatus())) {
            return;
        }

        LocalDateTime requestedAt = LocalDateTime.now();
        PasswordResetRequest resetRequest = passwordResetRequestRepository
                .findFirstByUserIdAndStatus(user.getId(), "PENDING")
                .orElseGet(PasswordResetRequest::new);
        resetRequest.setUserId(user.getId());
        resetRequest.setRequestedAt(requestedAt);
        resetRequest.setStatus("PENDING");
        passwordResetRequestRepository.save(resetRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PasswordResetRequestResponse> getPasswordResetRequests() {
        List<PasswordResetRequestResponse> requests = new ArrayList<>();
        for (PasswordResetRequest resetRequest : passwordResetRequestRepository
                .findAllByStatusOrderByRequestedAtAsc("PENDING")) {
            usersRepository.findById(resetRequest.getUserId())
                    .filter(user -> !Boolean.TRUE.equals(user.getIsDeleted()))
                    .ifPresent(user -> requests.add(new PasswordResetRequestResponse(
                            resetRequest.getId(),
                            user.getId(),
                            user.getUserName(),
                            user.getFullName() == null || user.getFullName().isBlank()
                                    ? user.getUserName() : user.getFullName(),
                            user.getEmail(),
                            resetRequest.getRequestedAt())));
        }
        return requests;
    }

    private Users findPasswordResetAccount(String email) {
        List<Employees> employees = employeesRepository.findAllByEmailIgnoreCaseAndIsDeletedFalse(email);
        List<Users> accounts = usersRepository.findAllByEmailIgnoreCaseAndIsDeletedFalse(email);
        if (employees.size() > 1 || accounts.size() > 1) {
            return null;
        }

        if (employees.size() == 1 && employees.get(0).getUserId() != null) {
            Long userId = employees.get(0).getUserId();
            if (accounts.size() == 1 && !Objects.equals(accounts.get(0).getId(), userId)) {
                return null;
            }
            return usersRepository.findById(userId)
                    .filter(account -> !Boolean.TRUE.equals(account.getIsDeleted()))
                    .orElse(null);
        }

        if (accounts.size() != 1) {
            return null;
        }
        Users account = accounts.get(0);
        return employeesRepository.findByUserIdAndIsDeletedFalse(account.getId()).isPresent()
                ? null : account;
    }

    private UserResponse createWithRole(UserCreateRequest request, String roleCode, String status) {
        if (usersRepository.existsByUserNameIgnoreCaseAndIsDeletedFalse(request.getUsername().trim())) {
            throw new BusinessException(DomainCode.CONFLICT, "tên đăng nhập đã được sử dụng");
        }
        if (usersRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(request.getEmail().trim())) {
            throw new BusinessException(DomainCode.CONFLICT, "email đã được sử dụng");
        }

        // Tạo entity từ request tại service để áp dụng các giá trị mặc định trước khi lưu:
        // các giá trị quyền và trạng thái đã được cố định theo điểm gọi (đăng ký hoặc admin).
        Users user = new Users();
        user.setEmail(request.getEmail().trim());
        user.setUserName(request.getUsername().trim());
        String fullName = ((request.getFirstName() == null ? "" : request.getFirstName().trim()) + " "
                + (request.getLastName() == null ? "" : request.getLastName().trim())).trim();
        user.setFullName(fullName.isBlank() ? request.getUsername().trim() : fullName);
        // Chỉ lưu password đã mã hóa; password gốc không được ghi vào entity/database.
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setAuthStateChangedAt(Instant.now());
        user.setRoleCode(roleCode);
        user.setStatus(status);
        user.setIsDeleted(false);

        // Repository chịu trách nhiệm persist entity; sau khi save, dữ liệu được map
        // thành UserResponse để trả về lớp controller.
        Users saved = usersRepository.save(user);

        List<Employees> employees = employeesRepository.findAllByEmailIgnoreCaseAndIsDeletedFalse(saved.getEmail());
        if (employees.size() > 1) {
            throw new BusinessException(DomainCode.CONFLICT, "email được dùng bởi nhiều hồ sơ nhân viên");
        }
        if (!employees.isEmpty()) {
            Employees employee = employees.get(0);
            if (employee.getUserId() != null || employeesRepository.findByUserIdAndIsDeletedFalse(saved.getId()).isPresent()) {
                throw new BusinessException(DomainCode.CONFLICT, "hồ sơ nhân viên đã được liên kết với tài khoản khác");
            }
            // Email chỉ dùng để tạo liên kết ban đầu; các lần đọc hồ sơ sau đó dựa trên subject ổn định của JWT.
            employee.setUserId(saved.getId());
            employeesRepository.save(employee);
        }

        // Response chỉ chứa thông tin user cần công khai, không chứa passwordHash.
        return buildUser(saved);
    }

    private UserResponse buildUser(Users user){
        String fullName = employeesRepository.findAllByEmailIgnoreCaseAndIsDeletedFalse(user.getEmail())
                .stream().findFirst().map(Employees::getFullName).orElse(user.getFullName());
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .userName(user.getUserName())
                .fullName(fullName)
                .roleCode(user.getRoleCode())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private void resolvePendingPasswordResetRequests(Long userId) {
        List<PasswordResetRequest> requests = passwordResetRequestRepository
                .findAllByUserIdAndStatus(userId, "PENDING");
        if (requests.isEmpty()) return;
        LocalDateTime resolvedAt = LocalDateTime.now();
        requests.forEach(request -> {
            request.setStatus("RESOLVED");
            request.setResolvedAt(resolvedAt);
        });
        passwordResetRequestRepository.saveAll(requests);
    }

    private void revokeRefreshTokensForUser(Long userId) {
        for (String token : Set.copyOf(activeRefreshTokens)) {
            try {
                if (String.valueOf(userId).equals(jwtDecoder.decode(token).getSubject())) {
                    activeRefreshTokens.remove(token);
                }
            } catch (JwtException exception) {
                activeRefreshTokens.remove(token);
            }
        }
    }

    private boolean accountChangedAfter(Users user, Instant issuedAt) {
        return user.getAuthStateChangedAt() != null
                && (issuedAt == null || user.getAuthStateChangedAt().isAfter(issuedAt));
    }
}
