package com.example.employee_service.common.security;

import com.example.employee_service.feature.entity.Users;
import com.example.employee_service.feature.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static com.example.employee_service.common.util.Const.IS_ACTIVE;
import static com.example.employee_service.common.util.Const.ROLE_PREFIX;
import static com.example.employee_service.common.util.SecurityUtil.getRolesFromJwt;

/**
 * Chuyển {@link Jwt} nội bộ thành {@link AbstractAuthenticationToken}: đọc roles +
 * permission theo role (qua {@link PermissionResolver}) rồi gắn vào {@code authorities}.
 *
 * <p>Đây là điểm QUYẾT ĐỊNH user có những quyền gì — chạy 1 lần mỗi request tại filter,
 * sau đó {@code @PreAuthorize} chỉ đọc lại để so khớp.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final PermissionResolver permissionResolver;
    private final UsersRepository usersRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Long userId;
        try {
            userId = Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new DisabledException("Account is unavailable");
        }
        // JWT còn hạn sau khi khóa hoặc đổi mật khẩu; đối chiếu thời điểm trạng thái đổi để thu hồi ngay phiên cũ.
        Users user = usersRepository.findById(userId).orElse(null);
        Instant issuedAt = jwt.getIssuedAt();
        boolean changedAfterToken = user != null && user.getAuthStateChangedAt() != null
                && (issuedAt == null || user.getAuthStateChangedAt().isAfter(issuedAt));
        if (user == null || Boolean.TRUE.equals(user.getIsDeleted())
                || !IS_ACTIVE.equals(user.getStatus()) || changedAfterToken) {
            throw new DisabledException("Account is unavailable");
        }

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // Role trong JWT được đổi thành ROLE_* để Spring Security dùng cho các
        // kiểm tra role ở tầng method/security expression.
        Set<String> roles = getRolesFromJwt(jwt);
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role)));

        // Permission được tra từ cache theo role và gắn vào cùng Authentication;
        // các kiểm tra quyền sau đó không cần query lại database trong request này.
        permissionResolver.permissionsOf(roles)
                .forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));

        return new JwtAuthenticationToken(jwt, authorities);
    }
}
