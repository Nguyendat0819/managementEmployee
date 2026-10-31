# Tính lương tự động

## Luồng dữ liệu

1. Payroll lấy các hợp đồng còn hiệu lực giao với kỳ lương từ `employee-service`.
2. Payroll gửi danh sách nhân viên, khoảng hiệu lực hợp đồng và ngày lễ đã cấu hình sang `attendance-service`.
3. Attendance tổng hợp ca được phân, ngày công, vắng, ngày lễ hưởng lương và phút làm thêm.
4. Payroll áp dụng cấu trúc lương, tính phiếu theo từng nhân viên và lưu dữ liệu đầu vào, tỷ lệ cùng kết quả trong `snapshot_data`.

Mọi ngày trong danh mục ngày nghỉ lễ được xử lý như ngày lễ hưởng lương. Thời gian chấm công trùng ngày đó được tính vào nhóm làm thêm ngày lễ.

Chỉ kỳ nháp chưa có phiếu mới chạy được tự động. Phiếu nhập tay không bị ghi đè.

## Công thức hiện tại

- Lương cơ bản và phụ cấp tháng được phân bổ theo ngày hợp đồng hiệu lực trong tháng.
- Tỷ lệ hưởng lương = `(ngày công + ngày lễ hưởng lương) / (ngày làm theo ca + ngày lễ hưởng lương)`, tối đa 100%. Khi chưa có lịch phân ca, hệ thống không suy ra vắng và giữ nguyên lương tháng.
- Tăng ca ngày thường chỉ lấy thời gian sau giờ kết thúc ca đã phân, không vượt quá tổng thời lượng check-in đến check-out, và hưởng mức 150%. Ngày không có ca không tự phát sinh tăng ca; thời gian làm ngày lễ vẫn được tính theo mức 300%. Đơn giá mỗi phút lấy từ lương cơ bản tháng chia tổng phút làm việc chuẩn đã phân ca trong kỳ.
- Gross = lương cơ bản đã hưởng + phụ cấp đã hưởng + tiền làm thêm. Thưởng và khấu trừ khác mặc định bằng 0 vì chưa có nguồn dữ liệu tương ứng.
- Thu nhập tính thuế = thu nhập chịu thuế từ lương, phụ cấp và làm thêm trừ phần làm thêm được miễn thuế và các khoản bảo hiểm bắt buộc của người lao động.
- Kỳ tính thuế từ năm 2026 dùng giảm trừ cá nhân 15,5 triệu đồng/tháng, giảm trừ người phụ thuộc 6,2 triệu đồng/tháng và biểu thuế lũy tiến 5 bậc. Kỳ trước 2026 dùng biểu và mức giảm trừ cũ.
- Người lao động đóng BHXH 8%, BHYT 1,5%, BHTN 1%; doanh nghiệp đóng lần lượt 17,5%, 3%, 1%. BHXH/BHYT áp trần 20 lần mức tham chiếu. Trần BHTN lấy 20 lần lương tối thiểu vùng; cấu hình mặc định là Vùng I.
- Net = Gross trừ BHXH, BHYT, BHTN, thuế TNCN và khấu trừ khác. Chi phí doanh nghiệp = Gross cộng các khoản bảo hiểm do doanh nghiệp đóng.

Từ kỳ tính thuế 2026, toàn bộ tiền làm thêm hợp lệ được miễn thuế; kỳ trước đó chỉ phần trả cao hơn tiền lương giờ bình thường được miễn. Hệ thống giả định phút làm thêm đã được duyệt và trong giới hạn pháp luật. [Hướng dẫn của Chính phủ về thu nhập làm thêm được miễn thuế](https://baochinhphu.vn/cac-truong-hop-tien-luong-tien-cong-duoc-mien-thue-tncn-102260715163431228.htm)

## Cấu hình và giới hạn dữ liệu

- `PAYROLL_BHTN_REGION_MINIMUM_2026` và `PAYROLL_BHTN_REGION_MINIMUM_2024_2025` đặt mức lương tối thiểu vùng để tính trần BHTN; mặc định lần lượt là 5.310.000 và 4.960.000 đồng (Vùng I). [Mức vùng năm 2026](https://baochinhphu.vn/tang-luong-toi-thieu-vung-tu-1-1-2026-102251110175033018.htm)
- Mặc định thuế coi nhân viên là cá nhân cư trú, không có người phụ thuộc. Cấu trúc lương hiện chưa phân loại phụ cấp chịu thuế hoặc tham gia bảo hiểm nên các phụ cấp được tính vào cả hai căn cứ.
- Chưa có dữ liệu phép năm/nghỉ hưởng lương để đối chiếu ngày vắng; ngày vắng được suy ra từ ca có phân nhưng không có chấm công hoàn chỉnh. HR cần rà soát cảnh báo trước khi gửi duyệt.
- Dữ liệu nhân viên hiện chưa có địa bàn làm việc hoặc hồ sơ bảo hiểm/thuế theo từng người. Nếu công ty có nhiều vùng hoặc nhân viên thuộc diện đóng khác nhau, cần bổ sung hồ sơ này trước khi dùng kết quả làm căn cứ quyết toán.
