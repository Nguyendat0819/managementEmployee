package com.example.employee_service.common.httpclient;

import com.example.employee_service.common.util.SecurityUtil;
import io.micrometer.observation.ObservationRegistry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * Tự đăng ký các interface {@code @HttpExchange} trong package
 * {@code com.example.employee_service.feature.client} thành proxy bean, GỘP theo base URL.
 *
 * <ul>
 *   <li>1 group = 1 base URL. Client chung URL -> để chung sub-package -> cùng group.</li>
 *   <li>Mở rộng thêm service khác: thêm {@code @ImportHttpServices(group="external", basePackages="...")}
 *       + 1 nhánh {@code case "external"} trong {@code switch} dưới.</li>
 * </ul>
 *
 * <p>Cấu hình chung (token + traceId) khai 1 lần; base URL set RIÊNG theo tên group.</p>
 */
@Configuration
@ImportHttpServices(group = "internal", basePackages = "com.example.employee_service.feature.client")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HttpClientConfig {

    InternalProperties internalProperties;

    @Bean
    public RestClientHttpServiceGroupConfigurer httpServiceGroupConfigurer(
            ObservationRegistry observationRegistry) {
        return groups -> groups.forEachClient((group, builder) -> {
            builder.observationRegistry(observationRegistry);                       // propagate traceId
            builder.requestInterceptor(authInterceptor());
            String baseUrl = switch (group.name()) {
                case "internal" -> internalProperties.getBaseUrl();
                // case "external" -> externalProperties.getBaseUrl();
                default -> null;
            };
            if (StringUtils.isNotBlank(baseUrl)) {
                builder.baseUrl(baseUrl);
            }
        });
    }

    private ClientHttpRequestInterceptor authInterceptor() {
        return (request, body, execution) -> {
            String token = SecurityUtil.getTokenValue();
            if (StringUtils.isNotBlank(token)) {
                request.getHeaders().setBearerAuth(token);
            }
            return execution.execute(request, body);
        };
    }

}
