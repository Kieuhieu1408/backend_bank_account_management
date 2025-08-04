# Chức năng servive

1. **Account Service:**
    - Quản lý tài khoản người dùng, bao gồm mở tài khoản, đóng tài khoản và cập nhật thông tin tài khoản.
    - Hỗ trợ các loại tài khoản khác nhau như tài khoản thanh toán, tiết kiệm, tín dụng.
    - Xác	thực thông tin khách hàng khi mở tài khoản.
2. **Transaction Service:**
    - Quản lý giao dịch của khách hàng như gửi tiền, rút tiền, chuyển khoản, và thanh toán hóa đơn.
    - Hỗ trợ giao dịch liên ngân hàng và quản lý lịch sử giao dịch.
    - Tích	hợp với hệ thống thông báo và cảnh báo về các giao dịch đáng ngờ.
3. **Notification Service:**
    - Gửi	thông báo qua email hoặc SMS cho khách hàng về các sự	kiện quan trọng như xác nhận giao dịch, thay đổi thông tin tài khoản, hoặc nhắc nhở thanh toán.
    - Quản lý mẫu thông báo và tùy chỉnh nội dung thông báo.

4. **Card service**

- Lưu thông tin thẻ
- Lưu biến động số dư thẻ
- Lưu thông tin đóng mở thẻ


  **Api Gateway**
- Xác thực token và chuyển tiếp yêu cầu đến các service tương ứng.

# Cách xác thực phân quyền giữa các service 
- Sử dụng Keycloak để quản lý người dùng và phân quyền truy cập.
- Mỗi service sẽ xác thực người dùng thông qua token JWT được cấp bởi Keycloak.
- Đầu tiên apiGateway sẽ xác thực token sau đó gửi request đến các service tương ứng. Các service đó không dùng keycloak để xác thực token nữa mà chỉ dùng keycloak để phân quyền

Note:

Sau khi card service được lưu sẽ gửi kafka để cập nhập thông tin số dư mới account service