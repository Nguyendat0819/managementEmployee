package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.config.JwtProperties;
import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.common.response.DomainCode;
import com.example.employee_service.common.util.Const;
import com.example.employee_service.feature.entity.users;
import com.example.employee_service.feature.model.request.LoginRequest;
import com.example.employee_service.feature.model.response.AuthResponse;
import com.example.employee_service.feature.model.response.TokenResponse;
import com.example.employee_service.feature.repository.UsersRepository;
import com.example.employee_service.feature.service.AuthService;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    @Override
    public TokenResponse login(LoginRequest request) {
        users user = usersRepository.findByUserNameAndIsDeletedFalse(request.getUsername());
        if (user == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(DomainCode.UNAUTHORIZED);
        }
        if (!Const.IS_ACTIVE.equals(user.getStatus())) {
            throw new BusinessException(DomainCode.FORBIDDEN);
        }

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.getAccessTokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(String.valueOf(user.getId()))
                .claim("preferred_username", user.getUserName())
                .claim("email", user.getEmail())
                .claim("roles", List.of(Objects.requireNonNullElse(user.getRoleCode(), "USER")))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        user.setLastLoginAt(java.time.LocalDateTime.now());
        usersRepository.save(user);
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenTtl().toSeconds())
                .build();
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
}
