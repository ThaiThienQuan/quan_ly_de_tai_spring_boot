# Hệ thống quản lý đề tài sinh viên — Spring Boot

## Phạm vi hiện tại
Bản này có đăng nhập và phân quyền theo vai trò; các trang đã đăng nhập dùng thanh điều hướng bên trái và giao diện Arial. Người dùng có thể đăng ký tài khoản sinh viên và cập nhật hồ sơ cá nhân; quản trị viên có thể chỉnh sửa, khóa/mở, đặt lại mật khẩu hoặc xóa tài khoản. Các chức năng dùng các bảng đã có trong `database/HT_QLDT.sql`; ứng dụng không tự thay đổi cấu trúc database.

## 1. Công cụ
- Java Development Kit 25
- Apache Maven 3.9.16 (dự án kiểm tra đúng phiên bản Java và Maven khi build)
- MySQL 8
- IntelliJ IDEA hoặc IDE Java tương đương

## 2. Tạo cơ sở dữ liệu
1. Mở MySQL Workbench.
2. Mở file `HT_QLDT.sql` bạn đã cung cấp.
3. Chạy toàn bộ script một lần để tạo cơ sở dữ liệu `student_topic_management` và dữ liệu thử nghiệm.
4. Lưu ý dữ liệu demo hiện dùng mật khẩu `{noop}Demo1234` theo đúng ghi chú trong SQL; chỉ được dùng ở môi trường học tập cục bộ.

## 3. Cấu hình kết nối
Mở `src/main/resources/application.properties`, thay `CHANGE_ME` bằng mật khẩu MySQL cục bộ của bạn. Nếu tài khoản MySQL không phải `root`, sửa `spring.datasource.username`.

`spring.jpa.hibernate.ddl-auto=validate` khiến Hibernate kiểm tra mapping với bảng hiện có thay vì tự sửa cấu trúc cơ sở dữ liệu.

## 4. Chạy
Kiểm tra phiên bản trước khi chạy:
```bash
java -version
mvn -version
```
Kết quả cần thể hiện Java 25 và Apache Maven 3.9.16. Nếu chưa đúng, hãy chỉnh `JAVA_HOME` và `PATH` để trỏ tới JDK 25 và Maven 3.9.16.

Mở terminal ở thư mục dự án:
```bash
mvn clean spring-boot:run
```

Dự án dùng Spring Boot 3.5.16 vì dòng phiên bản này hỗ trợ Java 25. Maven Enforcer được cấu hình để báo lỗi rõ ràng nếu phiên bản Java hoặc Maven không khớp yêu cầu.
Mở `http://localhost:8080`.

## 5. Tài khoản dữ liệu mẫu
Tất cả tài khoản trong SQL mẫu có mật khẩu `Demo1234`:
- `admin`: quản trị viên
- `truongkhoa`: trưởng khoa
- `chutich`: tài khoản chủ tịch hội đồng trong dữ liệu mẫu
- `gv01`, `gv02`, `gv03`: giảng viên
- `sv01` đến `sv09`: sinh viên

Chú ý: trong dữ liệu mẫu hiện tại, hồ sơ giảng viên được liên kết với các tài khoản có `user_id` 3, 4, 5, 6. Do đó `chutich` cũng đang có hồ sơ giảng viên; tài khoản `gv01` mới chỉ có liên kết vai trò trong `user_roles`, không có hồ sơ giảng viên tương ứng. Để thử tạo đề tài, hãy đăng nhập tài khoản có hồ sơ giảng viên, ví dụ `chutich`, hoặc bổ sung hồ sơ phù hợp trong dữ liệu.

## 6. Luồng dữ liệu chính
1. Trình duyệt gửi yêu cầu đến Controller.
2. Spring Security xác thực người dùng trước khi cho phép vào đường dẫn.
3. Controller nhận dữ liệu biểu mẫu và gọi Service.
4. Service kiểm tra quy tắc nghiệp vụ và gọi Repository.
5. Repository dùng Java Persistence API để truy vấn hoặc ghi dữ liệu MySQL.
6. Controller trả tên trang Thymeleaf; Thymeleaf kết hợp dữ liệu để tạo HTML trả về trình duyệt.

## 7. Bảng dữ liệu chính
- `users`, `roles`, `user_roles`: tài khoản và nhóm quyền (Spring Security hiện dùng trường `users.role`).
- `registration_periods`: các mốc đăng ký GV/SV và thời hạn phản biện.
- `topics`, `topic_supervisors`: thông tin đề tài, phê duyệt/công bố và 1-2 GVHD.
- `students`, `student_groups`, `group_members`, `topic_registrations`: nhóm, thành viên, nhóm trưởng và yêu cầu chọn đề tài.
- `reports`: phiên bản báo cáo của nhóm.
- `councils`, `council_members`, `review_assignments`, `evaluations`, `review_results`: hội đồng, phân công, điểm và kết quả.
- `notifications`, `notification_target_roles`: thông báo và nhóm vai trò được xem.

## 8. Quy tắc nghiệp vụ hiện thực
- Thời gian bắt đầu phải trước thời gian kết thúc ở từng giai đoạn.
- Giai đoạn sinh viên phải bắt đầu sau khi giai đoạn đăng ký của giảng viên kết thúc.
- Đợt `TLCN`/`KLTN` phải có hạn phản biện sau ngày kết thúc đăng ký sinh viên; `KLTN` phải có ngày báo cáo hội đồng sau giai đoạn đăng ký.
- Đề tài chỉ được tạo trong giai đoạn đăng ký giảng viên của đợt đã công bố, mã đề tài không trùng trong đợt, được gán GV tạo đề tài và tối đa một GVHD thứ hai.
- Chỉ đề tài `APPROVED` có 1-2 GVHD mới được công bố.
- Nhóm có tối đa ba thành viên; người tạo nhóm là nhóm trưởng; sinh viên không thể thuộc nhóm thứ hai. Chỉ nhóm trưởng đăng ký đề tài và nộp báo cáo.
- Đề tài nhóm chọn phải đã công bố và cùng đợt; yêu cầu được Trưởng khoa duyệt/từ chối. Một đề tài chỉ được duyệt cho một nhóm.
- Hội đồng có 3-5 giảng viên, chủ tịch và thư ký khác nhau. Giảng viên phản biện phải thuộc hội đồng và không phải GVHD đề tài.
- Điểm nhận từ 0 đến 10; chỉ người được phân công được nhập điểm. Chủ tịch chỉ công bố khi toàn bộ phản biện cho đề tài đã nộp điểm. Điểm tổng hợp là trung bình cộng, làm tròn 2 chữ số; ngưỡng `PASS` hiện được đặt là 5.00.
- Thông báo nháp chỉ hiển thị sau khi được đăng tải và chỉ hiển thị với các vai trò mục tiêu.
- Người dùng tự đăng ký với vai trò sinh viên; có thể cập nhật họ tên, email, số điện thoại và đổi mật khẩu khi biết mật khẩu hiện tại. Quản trị viên có thể cập nhật thông tin/vai trò/trạng thái, đặt lại mật khẩu và xóa tài khoản không bị dữ liệu khác tham chiếu; không thể tự xóa, tự khóa hoặc tự hạ quyền tài khoản đang đăng nhập.
- Đặt lại mật khẩu bởi quản trị viên không yêu cầu xác minh email; người quên mật khẩu cần liên hệ quản trị viên.
- Mật khẩu được xử lý qua cơ chế `DelegatingPasswordEncoder`, hỗ trợ định dạng `{noop}` của dữ liệu demo; không dùng `{noop}` trong môi trường triển khai thực tế.

## 9. Luồng chức năng
1. **Đợt đăng ký:** Trưởng khoa tạo đợt ở trạng thái `DRAFT`, nhập thời gian đăng ký của GV/SV và các hạn đặc thù; sau đó công bố.
2. **Đề tài:** GV tạo đề tài trong thời gian GV đăng ký; hệ thống ghi nhận người tạo là GVHD thứ nhất và tùy chọn GVHD thứ hai. Trưởng khoa duyệt rồi công bố.
3. **Nhóm sinh viên:** SV tạo nhóm trong thời gian SV đăng ký; tài khoản tạo nhóm là nhóm trưởng và nhập mã tối đa hai thành viên bổ sung.
4. **Chọn đề tài:** Nhóm trưởng chọn một đề tài đã công bố cùng đợt. Trưởng khoa duyệt hoặc từ chối kèm lý do; đề tài được duyệt sẽ gắn duy nhất với nhóm.
5. **Thực hiện:** Nhóm trưởng nộp báo cáo; mỗi lần nộp tạo phiên bản mới. Các thành viên có thể xem nhóm và báo cáo qua giao diện nhóm.
6. **Phản biện:** Trưởng khoa lập hội đồng và vai trò chủ tịch/thư ký, sau đó phân công thành viên hội đồng chấm đề tài. Hệ thống từ chối phân công GVHD chấm đề tài mình hướng dẫn.
7. **Tổng hợp:** Giảng viên được phân công nhập điểm/nhận xét. Chủ tịch hội đồng tổng hợp sau khi đủ điểm, công bố điểm trung bình và kết quả; SV xem kết quả đã công bố của nhóm mình.
8. **Tài khoản/thông báo:** Người dùng tự đăng ký và cập nhật tài khoản cá nhân; quản trị viên quản lý thông tin, quyền, mật khẩu và xóa tài khoản. Trưởng khoa/quản trị viên soạn thông báo bản nháp, chọn vai trò người nhận rồi đăng tải.

Các thao tác nghiệp vụ được kiểm tra ở tầng service và bảo vệ theo vai trò Spring Security. Hiện còn các giới hạn sau: đăng ký công khai hiện chỉ tạo hồ sơ sinh viên; khôi phục mật khẩu cần quản trị viên đặt lại; quản lý nhóm chỉ tạo nhóm (chưa thêm/xóa thành viên sau đó); đường dẫn báo cáo được nhập thủ công (chưa upload file); kiểm thử hiện bao phủ các quy tắc thời gian của đợt đăng ký và đăng ký sinh viên, chưa có kiểm thử tích hợp với MySQL. Để tránh tự đặt quy định không có trong đề bài, ngưỡng đạt hiện dùng 5.00/10 và có thể đổi theo quy định chính thức của khoa.
