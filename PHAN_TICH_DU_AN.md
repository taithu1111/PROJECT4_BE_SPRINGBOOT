# PHÂN TÍCH DỰ ÁN E-COMMERCE BACKEND
(Cập nhật mới nhất)

## 1. TỔNG QUAN DỰ ÁN

**Tên dự án:** E-commerce Backend  
**Công nghệ:** Spring Boot 3.2.4, Java 17, MySQL, JWT Authentication, Spring Security  
**Kiến trúc:** RESTful API với Spring MVC, JPA/Hibernate  

---

## 2. CÁC ACTOR THAM GIA HỆ THỐNG

### 2.1. **User/Customer (Khách hàng)**
- **Vai trò:** Người dùng cuối sử dụng hệ thống
- **Quyền hạn:**
  - Đăng ký tài khoản (`/auth/signup`)
  - Đăng nhập (`/auth/signin`)
  - Xem sản phẩm (danh sách, chi tiết, lọc/tìm kiếm)
  - Quản lý giỏ hàng (thêm, cập nhật, xóa sản phẩm)
  - Đặt hàng và xem lịch sử đơn hàng
  - Đánh giá và review sản phẩm
  - Quản lý thông tin cá nhân và địa chỉ

- **Đặc điểm:**
  - Mỗi user có một giỏ hàng (Cart) riêng (One-to-One relationship)
  - User có thể có nhiều địa chỉ (Address)
  - User có thể đánh giá (Rating) và review nhiều sản phẩm

### 2.2. **Admin (Quản trị viên)**
- **Vai trò:** Quản lý hệ thống toàn diện
- **Quyền hạn:**
  - Quản lý sản phẩm (CRUD): Thêm mới, cập nhật, xóa (soft/hard delete)
  - Quản lý danh mục (Category): Tạo cấu trúc danh mục cha-con
  - Quản lý đơn hàng:
    - Xem danh sách đơn hàng (Filter theo trạng thái)
    - Cập nhật trạng thái đơn hàng (`SHIPPED`, `DELIVERED`, `CANCELLED`, `CONFIRMED`)
    - Xác nhận thanh toán (`PAID`)
  - Quản lý người dùng: Xem danh sách, khóa/mở khóa tài khoản
  - Xem báo cáo và thống kê (Revenue, Orders count)
  - Quản lý nội dung (Rating/Review): Xóa các đánh giá vi phạm

- **Đặc điểm:**
  - Phân biệt với User thông qua field `role` (`ROLE_ADMIN`)
  - Cần xác thực JWT để truy cập các endpoint `/api/admin/**`

### 2.3. **Hệ thống (System)**
- **Vai trò:** Xử lý tự động các tác vụ nền
- **Chức năng:**
  - Xác thực JWT token mỗi request
  - Tự động tính toán tổng tiền giỏ hàng/đơn hàng
  - Kiểm tra tồn kho khóa (Pessimistic Locking) khi đặt hàng 
  - Logging hoạt động hệ thống

---

## 3. CÁC LUỒNG DATA FLOW CHÍNH

### 3.1. LUỒNG QUẢN LÝ ĐƠN HÀNG CỦA ADMIN (Admin Order Flow - MỚI)
Quy trình xử lý đơn hàng từ lúc khách đặt đến khi hoàn tất:

1. **Khách đặt hàng:** Order có status `PENDING`.
2. **Admin xác nhận:** 
   - Gọi API `/confirmed`: Chuyển status sang `CONFIRMED`.
3. **Giao hàng:**
   - Gọi API `/ship`: Chuyển status sang `SHIPPED`.
   - Gọi API `/deliver`: Chuyển status sang `DELIVERED`.
4. **Hoàn tất thanh toán:**
   - Gọi API `/confirmed-payment`: Chuyển status sang `PAID` và `PaymentStatus` sang `PAID` (đối với đơn COD sau khi giao thành công).

### 3.2. LUỒNG XÁC THỰC (Authentication Flow)
*(Giữ nguyên logic cũ nhưng bổ sung chi tiết)*
- **Stateless:** Server không lưu session, client phải gửi token mỗi request.
- **Filter Chain:** `JwtValidator` đứng trước tất cả Controller để chặn request không hợp lệ.

### 3.3. LUỒNG QUẢN LÝ SẢN PHẨM & KHO (Product & Inventory)
- **Kiểm tra tồn kho:** Khi user đặt hàng (`createOrder`), hệ thống sẽ trừ tồn kho (`quantity`).
- **Khóa bi quan (Pessimistic Lock):** Sử dụng khi trừ tồn kho để tránh Race Condition (nhiều người mua cùng lúc 1 sản phẩm cuối cùng).
- **Hoàn trả kho:** Khi đơn hàng bị hủy (`CANCELLED`), số lượng sản phẩm được cộng lại vào kho.

---

## 4. CẤU TRÚC DỮ LIỆU & STATUS

### 4.1. Order Status (Trạng thái đơn hàng)
Hệ thống sử dụng String/Enum để quản lý trạng thái:
- `PENDING`: Mới đặt, chờ xử lý
- `PLACED`: Đã đặt (tương đương Pending trong một số ngữ cảnh)
- `CONFIRMED`: Admin đã xác nhận
- `SHIPPED`: Đang giao hàng
- `DELIVERED`: Đã giao hàng thành công
- `PAID`: Đã thanh toán (Trạng thái cuối cùng của luồng thành công)
- `CANCELLED`: Đã hủy

### 4.2. Payment Status
- `PENDING`: Chưa thanh toán / Chờ thanh toán COD
- `PAID`: Đã thanh toán
- `FAILED`: Thanh toán thất bại

---

## 5. CÁC ĐIỂM LƯU Ý KỸ THUẬT QUAN TRỌNG

1. **Transactional:** Các service quan trọng (`OrderService`, `ProductService`) đều có `@Transactional` để đảm bảo tính toàn vẹn dữ liệu.
2. **Exception Handling:** Sử dụng `GlobalExceptionHandler` (nếu có) hoặc try-catch tại Controller để trả về `ApiResponse` chuẩn.
3. **Phân trang (Pagination):** Hầu hết các API danh sách (`getAllOrders`, `getAllProducts`) đều hỗ trợ phân trang (`page`, `size`) để tối ưu hiệu năng.
4. **Security:** Role-based access control (RBAC) được áp dụng chặt chẽ trên Controller (`@PreAuthorize("hasRole('ADMIN')")`).

---

## 6. TÓM TẮT THAY ĐỔI GẦN ĐÂY
- **Bổ sung API Admin:** Đã hoàn thiện bộ API cho Admin quản lý full vòng đời đơn hàng.
- **Fix lỗi Logic:** Cập nhật giỏ hàng sau khi đặt hàng (Clear cart).
- **Tối ưu cấu trúc:** Tách biệt rõ ràng các Controller Admin vào package riêng hoặc file riêng dể dễ bảo trì.
