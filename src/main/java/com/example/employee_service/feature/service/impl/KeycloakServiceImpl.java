package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.config.KeycloakProperties;
import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.common.response.DomainCode;
import com.example.employee_service.feature.model.dto.CredentialRepresentation;
import com.example.employee_service.feature.model.dto.KeycloakUserRepresentation;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.repository.UsersRepository;
import com.example.employee_service.feature.service.KeycloakService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KeycloakServiceImpl implements KeycloakService {

    static String CLIENT_REGISTRATION_ID = "internal";
    UsersRepository usersRepository;
    KeycloakProperties keycloakProperties;
    OAuth2AuthorizedClientManager authorizedClientManager;
    RestClient restClient = RestClient.builder().build();

    @Override
    public String createUser(UserCreateRequest request) {
//      Kiểm tra user đã tồn tại chưa
        boolean checkUser = usersRepository.existsAllByEmailAndUserName(request.getEmail(), request.getUsername());
        if(checkUser){
            throw new BusinessException(DomainCode.CONFLICT);
        }
        log.info("Bắt đầu tạo user trên Keycloak: username={}, email={}", request.getUsername(), request.getEmail());

        KeycloakUserRepresentation userRep = KeycloakUserRepresentation.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .emailVerified(true)
                .credentials(Collections.singletonList(
                        CredentialRepresentation.builder()
                                .type("password")
                                .value(request.getPassword())
                                .temporary(false)
                                .build()
                ))
                .build();

        String token = getAdminAccessToken();
        String url = String.format("%s/admin/realms/%s/users",
                keycloakProperties.getBaseUrl(), keycloakProperties.getRealm());

        URI location = restClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(userRep)
                .exchange((clientRequest, clientResponse) -> {
                    HttpStatusCode status = clientResponse.getStatusCode();
                    if (status.value() == 409) {
                        throw new BusinessException(DomainCode.CONFLICT, "Username hoặc Email đã tồn tại trên Keycloak");
                    }
                    if (!status.is2xxSuccessful()) {
                        throw new BusinessException(DomainCode.EXTERNAL_SERVICE_ERROR,
                                "Keycloak trả về lỗi khi tạo user: " + status.value());
                    }
                    return clientResponse.getHeaders().getLocation();
                });

        if (location == null) {
            throw new BusinessException(DomainCode.EXTERNAL_SERVICE_ERROR,
                    "Không tìm thấy Location header trả về từ Keycloak");
        }

        String path = location.getPath();
        String keycloakUserId = path.substring(path.lastIndexOf('/') + 1);
        log.info("Tạo thành công user trên Keycloak với ID: {}", keycloakUserId);

        // Gán Role nếu request có roleCode
        if (StringUtils.isNotBlank(request.getRoleCode())) {
            assignRealmRole(keycloakUserId, request.getRoleCode());
        }

        return keycloakUserId;
    }

    @Override
    public KeycloakUserRepresentation getUserById(String keycloakUserId) {
        String token = getAdminAccessToken();
        String url = String.format("%s/admin/realms/%s/users/%s",
                keycloakProperties.getBaseUrl(), keycloakProperties.getRealm(), keycloakUserId);

        return restClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, resp) -> {
                    throw new BusinessException(DomainCode.NOT_FOUND, "Không tìm thấy user trên Keycloak: " + keycloakUserId);
                })
                .body(KeycloakUserRepresentation.class);
    }

    @Override
    public void assignRealmRole(String keycloakUserId, String roleName) {
        log.info("Gán role '{}' cho user Keycloak '{}'", roleName, keycloakUserId);
        String token = getAdminAccessToken();

        // 1. Lấy thông tin Role từ Keycloak
        String roleUrl = String.format("%s/admin/realms/%s/roles/%s",
                keycloakProperties.getBaseUrl(), keycloakProperties.getRealm(), roleName);

        Map<String, Object> roleRepresentation = restClient.get()
                .uri(roleUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, resp) -> {
                    throw new BusinessException(DomainCode.NOT_FOUND, "Không tìm thấy role trên Keycloak: " + roleName);
                })
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        // 2. Map role vào user
        String mappingUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm",
                keycloakProperties.getBaseUrl(), keycloakProperties.getRealm(), keycloakUserId);

        restClient.post()
                .uri(mappingUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(roleRepresentation))
                .retrieve()
                .toBodilessEntity();

        log.info("Gán role '{}' cho user '{}' thành công", roleName, keycloakUserId);
    }

    @Override
    public void deleteUser(String keycloakUserId) {
        log.info("Xoá user trên Keycloak: {}", keycloakUserId);
        String token = getAdminAccessToken();
        String url = String.format("%s/admin/realms/%s/users/%s",
                keycloakProperties.getBaseUrl(), keycloakProperties.getRealm(), keycloakUserId);

        restClient.delete()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void updateUserEnabled(String keycloakUserId, boolean enabled) {
        log.info("Cập nhật trạng thái enabled={} cho user Keycloak: {}", enabled, keycloakUserId);
        String token = getAdminAccessToken();
        String url = String.format("%s/admin/realms/%s/users/%s",
                keycloakProperties.getBaseUrl(), keycloakProperties.getRealm(), keycloakUserId);

        restClient.put()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("enabled", enabled))
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, resp) -> {
                    throw new BusinessException(DomainCode.NOT_FOUND, "Không tìm thấy user trên Keycloak: " + keycloakUserId);
                })
                .onStatus(status -> !status.is2xxSuccessful(), (req, resp) -> {
                    throw new BusinessException(DomainCode.EXTERNAL_SERVICE_ERROR,
                            "Keycloak trả về lỗi khi cập nhật trạng thái user: " + resp.getStatusCode().value());
                })
                .toBodilessEntity();

        log.info("Cập nhật trạng thái enabled={} cho user Keycloak '{}' thành công", enabled, keycloakUserId);
    }

    private String getAdminAccessToken() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(CLIENT_REGISTRATION_ID)
                .principal(CLIENT_REGISTRATION_ID)
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new BusinessException(DomainCode.EXTERNAL_SERVICE_ERROR,
                    "Không thể lấy token OAuth2 cho client: " + CLIENT_REGISTRATION_ID);
        }
        return authorizedClient.getAccessToken().getTokenValue();
    }
}
