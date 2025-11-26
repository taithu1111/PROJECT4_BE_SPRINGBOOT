# PHÂN TÍCH DỰ ÁN E-COMMERCE BACKEND

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
- **Vai trò:** Quản lý hệ thống
- **Quyền hạn:**
  - Quản lý sản phẩm (CRUD)
  - Quản lý danh mục (Category)
  - Quản lý đơn hàng (xem, cập nhật trạng thái)
  - Quản lý người dùng
  - Xem báo cáo và thống kê

- **Đặc điểm:**
  - Phân biệt với User thông qua field `role` trong bảng User
  - Cần xác thực JWT để truy cập các endpoint quản trị

### 2.3. **Hệ thống (System)**
- **Vai trò:** Xử lý tự động các tác vụ
- **Chức năng:**
  - Xác thực JWT token
  - Bảo mật API endpoints
  - Quản lý session (stateless)
  - Logging và error handling

---

## 3. CÁC LUỒNG DATA FLOW CHÍNH

### 3.1. LUỒNG XÁC THỰC (Authentication Flow)

```
┌─────────┐         ┌─────────────┐         ┌──────────────┐
│ Client  │────────▶│AuthController│────────▶│UserRepository│
└─────────┘         └─────────────┘         └──────────────┘
                           │                         │
                           │                         ▼
                           │                  ┌──────────────┐
                           │                  │   Database    │
                           │                  └──────────────┘
                           │                         │
                           ▼                         │
                    ┌─────────────┐                 │
                    │ JwtProvider │◀────────────────┘
                    └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ JWT Token   │
                    └─────────────┘
```

**Chi tiết luồng:**

1. **Đăng ký (Signup):**
   - Client gửi POST `/auth/signup` với thông tin User
   - `AuthController` kiểm tra email đã tồn tại chưa
   - Tạo User mới với password đã mã hóa (BCrypt)
   - Tự động tạo Cart cho User mới
   - Tạo JWT token và trả về cho Client

2. **Đăng nhập (Signin):**
   - Client gửi POST `/auth/signin` với email/password
   - `AuthController` xác thực thông tin qua `CustomUserServiceImpl`
   - Nếu thành công, tạo JWT token
   - Client lưu token để sử dụng cho các request sau

3. **Xác thực JWT (Mỗi request đến `/api/**`):**
   - `JwtValidator` filter kiểm tra token trong header `Authorization`
   - Giải mã token và lấy thông tin user (email, authorities)
   - Đặt Authentication vào SecurityContext
   - Cho phép request tiếp tục nếu token hợp lệ

---

### 3.2. LUỒNG QUẢN LÝ SẢN PHẨM (Product Flow)

```
┌─────────┐         ┌──────────────┐         ┌──────────────┐
│ Client  │────────▶│ProductController│──────▶│ProductService│
└─────────┘         └──────────────┘         └──────────────┘
                           │                         │
                           │                         ▼
                           │                  ┌──────────────┐
                           │                  │ProductRepo   │
                           │                  └──────────────┘
                           │                         │
                           ▼                         ▼
                    ┌─────────────┐         ┌──────────────┐
                    │  ProductDTO │         │  Database    │
                    └─────────────┘         └──────────────┘
```

**Các endpoint:**

- `GET /api/product` - Lấy tất cả sản phẩm
- `GET /api/product/{id}` - Lấy chi tiết sản phẩm
- `GET /api/product/filter` - Lọc sản phẩm theo:
  - Category
  - Color
  - Price range (minPrice, maxPrice)
  - Sort (sắp xếp)
  - Pagination (pageNumber, pageSize)

- `GET /home/get-new` - Lấy sản phẩm mới (không cần auth)
- `GET /home/get-random` - Lấy sản phẩm ngẫu nhiên (không cần auth)

**Cấu trúc Product:**
- Product có nhiều ProductImage
- Product có nhiều ProductColor
- Product thuộc một Category
- Product có nhiều Rating và Review

---

### 3.3. LUỒNG QUẢN LÝ GIỎ HÀNG (Cart Flow)

```
┌─────────┐         ┌──────────────┐         ┌──────────────┐
│ Client  │────────▶│CartController│────────▶│ CartService  │
└─────────┘         └──────────────┘         └──────────────┘
      │                     │                         │
      │                     │                         ▼
      │                     │                  ┌──────────────┐
      │                     │                  │ CartItem     │
      │                     │                  │ Service      │
      │                     │                  └──────────────┘
      │                     │                         │
      │                     ▼                         ▼
      │              ┌──────────────┐         ┌──────────────┐
      │              │   CartRepo   │         │CartItemRepo   │
      │              └──────────────┘         └──────────────┘
      │                     │                         │
      ▼                     ▼                         ▼
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│ JWT Token   │         │   Database   │         │   Product    │
└─────────────┘         └──────────────┘         └──────────────┘
```

**Chi tiết luồng:**

1. **Thêm sản phẩm vào giỏ (Add to Cart):**
   - Client gửi PUT `/api/cart/add` với JWT token và `AddItemRequest` (productId, quantity, price)
   - `CartController` xác thực user từ JWT
   - `CartService` tìm Cart của user (hoặc tạo mới nếu chưa có)
   - Kiểm tra sản phẩm đã có trong Cart chưa
   - Nếu chưa có: tạo CartItem mới
   - Cập nhật `totalPrice` và `totalItem` của Cart

2. **Xem giỏ hàng:**
   - Client gửi GET `/api/cart` với JWT token
   - `CartService` trả về CartDTO chứa danh sách CartItem

3. **Cập nhật số lượng:**
   - Client gửi PUT `/api/cartItem/{cartItemId}` với quantity mới
   - `CartItemController` cập nhật quantity và tính lại totalPrice

4. **Xóa sản phẩm khỏi giỏ:**
   - Client gửi DELETE `/api/cartItem/{cartItemId}`
   - `CartItemService` xóa CartItem và cập nhật Cart

---

### 3.4. LUỒNG ĐẶT HÀNG (Order Flow)

```
┌─────────┐         ┌──────────────┐         ┌──────────────┐
│ Client  │────────▶│OrderController│───────▶│ OrderService │
└─────────┘         └──────────────┘         └──────────────┘
      │                     │                         │
      │                     │                         ▼
      │                     │                  ┌──────────────┐
      │                     │                  │  CartService │
      │                     │                  └──────────────┘
      │                     │                         │
      │                     │                         ▼
      │                     │                  ┌──────────────┐
      │                     │                  │CartItemService│
      │                     │                  └──────────────┘
      │                     │                         │
      │                     ▼                         ▼
      │              ┌──────────────┐         ┌──────────────┐
      │              │ AddressRepo  │         │OrderItemService│
      │              └──────────────┘         └──────────────┘
      │                     │                         │
      ▼                     ▼                         ▼
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│ JWT Token   │         │   Database   │         │   OrderRepo │
└─────────────┘         └──────────────┘         └──────────────┘
```

**Chi tiết luồng:**

1. **Tạo đơn hàng:**
   - Client gửi POST `/api/orders/` với:
     - JWT token
     - Address (shippingAddress): streetAddress, city, zipCode
   
   - `OrderService.createOrder()` thực hiện:
     - Lưu Address và liên kết với User
     - Lấy Cart của User
     - Chuyển đổi tất cả CartItem thành OrderItem
     - Tạo Order mới với:
       - orderDate = hiện tại
       - orderStatus = "PENDING"
       - totalPrice = cart.totalPrice
       - totalItem = cart.totalItem
     - Lưu Order và OrderItem vào database
     - **Lưu ý:** Giỏ hàng vẫn còn sau khi đặt hàng (có thể thêm xóa Cart sau khi đặt)

2. **Xem lịch sử đơn hàng:**
   - Client gửi GET `/api/orders/user` với JWT token
   - Trả về danh sách Order của user

3. **Xem chi tiết đơn hàng:**
   - Client gửi GET `/api/orders/{id}` với JWT token
   - Trả về Order với đầy đủ OrderItem

---

### 3.5. LUỒNG ĐÁNH GIÁ VÀ REVIEW (Rating & Review Flow)

```
┌─────────┐         ┌──────────────┐         ┌──────────────┐
│ Client  │────────▶│RatingController│──────▶│RatingService│
└─────────┘         └──────────────┘         └──────────────┘
      │                     │                         │
      │                     │                         ▼
      │                     │                  ┌──────────────┐
      │                     │                  │ RatingRepo   │
      │                     │                  └──────────────┘
      │                     │                         │
      │                     ▼                         ▼
      │              ┌──────────────┐         ┌──────────────┐
      │              │  ReviewController │     │  ReviewRepo  │
      │              └──────────────┘         └──────────────┘
      │                     │                         │
      ▼                     ▼                         ▼
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│ JWT Token   │         │   Database   │         │   Product    │
└─────────────┘         └──────────────┘         └──────────────┘
```

**Chi tiết luồng:**

1. **Tạo Rating:**
   - Client gửi POST `/api/ratings/create` với:
     - JWT token
     - RatingRequest (productId, rating value)
   - `RatingService` tạo Rating và liên kết với User và Product

2. **Xem Rating của sản phẩm:**
   - Client gửi GET `/api/ratings/product/{productId}`
   - Trả về danh sách Rating của sản phẩm

3. **Tạo Review:**
   - Client gửi POST `/api/reviews/create` với:
     - JWT token
     - ReviewRequest (productId, review text)
   - `ReviewService` tạo Review và liên kết với User và Product

4. **Xem Review của sản phẩm:**
   - Client gửi GET `/api/reviews/product/{productId}` (không cần auth)
   - Trả về danh sách Review của sản phẩm

---

### 3.6. LUỒNG QUẢN LÝ NGƯỜI DÙNG (User Management Flow)

```
┌─────────┐         ┌──────────────┐         ┌──────────────┐
│ Client  │────────▶│UserController│────────▶│ UserService  │
└─────────┘         └──────────────┘         └──────────────┘
      │                     │                         │
      │                     │                         ▼
      │                     │                  ┌──────────────┐
      │                     │                  │ JwtProvider  │
      │                     │                  └──────────────┘
      │                     │                         │
      │                     │                         ▼
      │                     │                  ┌──────────────┐
      │                     │                  │ UserRepository│
      │                     │                  └──────────────┘
      │                     │                         │
      ▼                     ▼                         ▼
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│ JWT Token   │         │  User Entity │         │   Database   │
└─────────────┘         └──────────────┘         └──────────────┘
```

**Chi tiết:**

1. **Xem thông tin cá nhân:**
   - Client gửi GET `/api/users/profile` với JWT token
   - `UserService.findUserProfileByJwt()` giải mã token, lấy email
   - Tìm User trong database và trả về thông tin

---

## 4. KIẾN TRÚC DỮ LIỆU (Data Model)

### 4.1. Quan hệ giữa các Entity:

```
User (1) ────── (1) Cart ────── (N) CartItem ────── (1) Product
  │                                                      │
  │                                                      │
  ├──── (N) Address                                       │
  │                                                      │
  ├──── (N) Order ────── (N) OrderItem ─────────────────┤
  │                                                      │
  ├──── (N) Rating ─────────────────────────────────────┤
  │                                                      │
  └──── (N) Review ─────────────────────────────────────┤
                                                         │
                                              ┌──────────┴──────────┐
                                              │                     │
                                         (1) Category         (N) ProductImage
                                                              (N) ProductColor
```

### 4.2. Các Entity chính:

- **User:** id, firstName, lastName, email, password, role, mobile, addresses, ratings, reviews
- **Product:** id, product_name, quantity, price, brand, category, colors, images, ratings, reviews
- **Cart:** id, user, cartItems, totalPrice, totalItem
- **CartItem:** id, cart, product, quantity, price
- **Order:** id, orderId, user, orderItems, orderDate, deliveryDate, shippingAddress, totalPrice, orderStatus, totalItem
- **OrderItem:** id, order, product, quantity, price
- **Address:** id, streetAddress, city, zipCode, user
- **Category:** id, category_name, parent_category, level
- **Rating:** id, user, product, rating, createdAt
- **Review:** id, user, product, review, createdAt

---

## 5. BẢO MẬT VÀ XÁC THỰC

### 5.1. Security Configuration:
- **Stateless Authentication:** Sử dụng JWT, không lưu session
- **CORS:** Cho phép từ `http://localhost:4200` và `http://localhost:8080`
- **Protected Endpoints:** Tất cả `/api/**` cần JWT token
- **Public Endpoints:** `/auth/**`, `/home/**`

### 5.2. JWT Flow:
1. User đăng nhập → Nhận JWT token
2. Mỗi request đến `/api/**` → Gửi token trong header `Authorization: Bearer <token>`
3. `JwtValidator` filter kiểm tra và giải mã token
4. Đặt Authentication vào SecurityContext
5. Controller có thể lấy User từ SecurityContext

---

## 6. CÔNG NGHỆ VÀ THÀNH PHẦN

- **Framework:** Spring Boot 3.2.4
- **Database:** MySQL
- **ORM:** JPA/Hibernate
- **Security:** Spring Security + JWT
- **Logging:** SLF4J + Logback
- **Mapping:** ModelMapper
- **API Documentation:** SpringDoc OpenAPI
- **Build Tool:** Maven

---

## 7. TÓM TẮT CÁC LUỒNG CHÍNH

1. **Authentication Flow:** Signup → Login → JWT Token → Protected API calls
2. **Product Browsing Flow:** View Products → Filter/Search → View Details
3. **Shopping Flow:** Add to Cart → Update Cart → View Cart → Create Order → Order History
4. **Review Flow:** Create Rating → Create Review → View Reviews
5. **User Profile Flow:** View Profile → Manage Addresses

---

## 8. ĐIỂM LƯU Ý

1. **Giỏ hàng không tự động xóa sau khi đặt hàng:** Sau khi tạo Order, Cart vẫn còn các CartItem
2. **Authentication:** Tất cả endpoints `/api/**` đều cần JWT token, trừ `/auth/**` và `/home/**`
3. **One-to-One Cart-User:** Mỗi user chỉ có một Cart duy nhất
4. **Cascade Operations:** Xóa CartItem sẽ không ảnh hưởng đến Product
5. **Order Status:** Hiện tại chỉ có status "PENDING" khi tạo, chưa có cập nhật trạng thái











