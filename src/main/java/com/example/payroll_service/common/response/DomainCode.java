package com.example.payroll_service.common.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã phản hồi của service.
 *
 * <p>Mỗi mã gồm: {@code code} (định danh), {@code httpStatus} (HTTP status tương ứng),
 * và {@code message} mặc định. Chuỗi {@code "{}"} trong message là chỗ truyền tham số
 * động (xem {@link ApiResponseFactory}).</p>
 *
 * <p><b>Quy ước code:</b> {@code <PREFIX>-<NHÓM><SỐ>}. PREFIX đổi theo service khi clone
 * (mặc định {@code TEM}). Nhóm: {@code 0xx} = success, {@code 4xx} = lỗi client,
 * {@code 5xx} = lỗi server/ngoài.</p>
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum DomainCode {

    //  Success
    SUCCESS("PAY-000", HttpStatus.OK, "Thành công"),

    //  Lỗi client (4xx)
    BAD_REQUEST("PAY-400", HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ"),
    UNAUTHORIZED("PAY-401", HttpStatus.UNAUTHORIZED, "Chưa xác thực"),
    INVALID_PARAMETER("PAY-402", HttpStatus.BAD_REQUEST, "Tham số không hợp lệ: {}"),
    FORBIDDEN("PAY-403", HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này"),
    NOT_FOUND("PAY-404", HttpStatus.NOT_FOUND, "Không tìm thấy: {}"),
    VALIDATION_ERROR("PAY-405", HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ: {}"),
    TOKEN_EXPIRED("PAY-406", HttpStatus.UNAUTHORIZED, "Phiên đăng nhập đã hết hạn"),
    CONFLICT("PAY-407", HttpStatus.CONFLICT, "Dữ liệu đã tồn tại hoặc đang xung đột: {}"),

    //  Lỗi server / service ngoài (5xx)
    INTERNAL_ERROR("PAY-500", HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau"),
    EXTERNAL_SERVICE_ERROR("PAY-502", HttpStatus.BAD_GATEWAY, "Lỗi khi gọi dịch vụ ngoài: {}"),
    SERVICE_UNAVAILABLE("PAY-503", HttpStatus.SERVICE_UNAVAILABLE, "Dịch vụ tạm thời không khả dụng");

    //  Mã nghiệp vụ riêng (thêm theo service)
    // Ví dụ: PAYROLL_NOT_FOUND("PAY-1001", HttpStatus.NOT_FOUND, "Không tìm thấy kỳ lương {}"),

    String code;
    HttpStatus httpStatus;
    String message;
}
