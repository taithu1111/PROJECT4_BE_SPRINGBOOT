# CẤU TRÚC CÂY THỬ MỤC DỰ ÁN E-COMMERCE BACKEND
(Cập nhật mới nhất)

```
backend/
│
├── .gitignore                          # Git ignore rules
├── Dockerfile                          # Docker configuration file
├── mvnw                                # Maven wrapper (Unix)
├── mvnw.cmd                            # Maven wrapper (Windows)
├── pom.xml                             # Maven project configuration
├── PHAN_TICH_DU_AN.md                  # Tài liệu phân tích dự án
├── CAU_TRUC_THU_MUC.md                 # File này - cấu trúc thư mục
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── phamiz/
│   │   │       └── ecommerce/
│   │   │           └── backend/
│   │   │               ├── BackendApplication.java           # Main application class
│   │   │               │
│   │   │               ├── config/                            # Configuration classes
│   │   │               │   ├── AppConfig.java                 # Security & CORS config
│   │   │               │   ├── JwtConstant.java              # JWT constants
│   │   │               │   ├── JwtProvider.java              # JWT token generator
│   │   │               │   └── JwtValidator.java              # JWT token validator filter
│   │   │               │
│   │   │               ├── controller/                        # REST Controllers
│   │   │               │   ├── admin/                         # Admin specific controllers
│   │   │               │   │   ├── AdminCategoryController.java
│   │   │               │   │   ├── AdminRatingController.java
│   │   │               │   │   └── AdminReviewController.java
│   │   │               │   │
│   │   │               │   ├── AdminOrderController.java      # Admin Order APIs
│   │   │               │   ├── AdminProductController.java    # Admin Product APIs
│   │   │               │   ├── AdminStatisticsController.java # Admin Dashboard Stats
│   │   │               │   ├── AdminUserController.java       # Admin User APIs
│   │   │               │   │
│   │   │               │   ├── AuthController.java            # Auth endpoints
│   │   │               │   ├── CartController.java            # Cart endpoints
│   │   │               │   ├── CartItemController.java        # CartItem endpoints
│   │   │               │   ├── HomeController.java            # Public/Home endpoints
│   │   │               │   ├── OrderController.java           # User Order endpoints
│   │   │               │   ├── PaymentController.java         # Payment endpoints
│   │   │               │   ├── ProductController.java         # Public Product endpoints
│   │   │               │   ├── RatingController.java          # User Rating endpoints
│   │   │               │   ├── ReviewController.java          # User Review endpoints
│   │   │               │   └── UserController.java            # User Profile endpoints
│   │   │               │
│   │   │               ├── dto/                               # Data Transfer Objects
│   │   │               │   ├── ApiResponse.java              # Generic API response
│   │   │               │   ├── Auth/                          # Auth DTOs
│   │   │               │   ├── Cart/                          # Cart DTOs
│   │   │               │   ├── Category/                      # Category DTOs
│   │   │               │   ├── Order/                         # Order DTOs
│   │   │               │   ├── Product/                       # Product DTOs
│   │   │               │   ├── Rating/                        # Rating DTOs
│   │   │               │   ├── Review/                        # Review DTOs
│   │   │               │   └── User/                          # User DTOs
│   │   │               │
│   │   │               ├── exception/                         # Custom exceptions
│   │   │               │   ├── CartItemException.java
│   │   │               │   ├── OrderException.java
│   │   │               │   ├── ProductException.java
│   │   │               │   └── UserException.java
│   │   │               │
│   │   │               ├── model/                             # Entity models (JPA)
│   │   │               │   ├── Address.java
│   │   │               │   ├── Cart.java
│   │   │               │   ├── CartItem.java
│   │   │               │   ├── Category.java
│   │   │               │   ├── Order.java
│   │   │               │   ├── OrderItem.java
│   │   │               │   ├── PaymentMethod.java (Enum)
│   │   │               │   ├── PaymentStatus.java (Enum)
│   │   │               │   ├── Product.java
│   │   │               │   ├── ProductColor.java
│   │   │               │   ├── ProductImage.java
│   │   │               │   ├── Rating.java
│   │   │               │   ├── Review.java
│   │   │               │   └── User.java
│   │   │               │
│   │   │               ├── repositories/                      # JPA Repositories
│   │   │               │   ├── IAddressRepository.java
│   │   │               │   ├── ICartItemRepository.java
│   │   │               │   ├── ICartRepository.java
│   │   │               │   ├── ICategoryRepository.java
│   │   │               │   ├── IOrderItemRepository.java
│   │   │               │   ├── IOrderRepository.java
│   │   │               │   ├── IProductImageRepository.java
│   │   │               │   ├── IProductRepository.java
│   │   │               │   ├── IRatingRepository.java
│   │   │               │   ├── IReviewRepository.java
│   │   │               │   └── UserRepository.java
│   │   │               │
│   │   │               └── service/                           # Service Interfaces
│   │   │                   ├── IAdminCategoryService.java
│   │   │                   ├── ICartItemService.java
│   │   │                   ├── ICartService.java
│   │   │                   ├── IOrderItemService.java
│   │   │                   ├── IOrderService.java
│   │   │                   ├── IProductService.java
│   │   │                   ├── IRatingService.java
│   │   │                   ├── IReviewService.java
│   │   │                   ├── IStatisticsService.java
│   │   │                   ├── IUserService.java
│   │   │                   ├── PayOSService.java
│   │   │                   │
│   │   │                   └── serviceImpl/                   # Service Implementations
│   │   │                       ├── AdminCategoryServiceImpl.java
│   │   │                       ├── CartItemServiceImpl.java
│   │   │                       ├── OrderService.java
│   │   │                       ├── ProductServiceImpl.java
│   │   │                       ├── StatisticsService.java
│   │   │                       └── ... (các impl khác)
│   │   │
│   │   └── resources/                                         # Resource files
│   │       ├── application.properties
│   │       └── logback.xml
│   │
│   └── test/                                                 # Test source code
│       └── java/
│           └── phamiz/
│               └── ecommerce/
│                   └── backend/
│                       └── controller/
│                           └── AdminOrderControllerTest.java # Tests
│
└── target/                                                   # Build output
```

---

## CẬP NHẬT CHÍNH TRONG CẤU TRÚC

### 1. Phân chia Controller
Cấu trúc Controller hiện tại được chia thành 2 nhóm chính:
- **Root Controller (`controller/`)**: Chứa các controller chính cho User và một số Admin controller quan trọng (`AdminOrder`, `AdminProduct`, `AdminUser`, `AdminStatistics`).
- **Sub-package Admin (`controller/admin/`)**: Chứa các controller Admin bổ trợ hoặc module hóa (`AdminCategory`, `AdminRating`, `AdminReview`).

### 2. Service Layer mở rộng
Đã thêm các Service mới để phục vụ tính năng Admin và Thanh toán:
- **IStatisticsService**: Phục vụ Dashboard thống kê.
- **PayOSService**: Tích hợp cổng thanh toán (nếu có).
- **IAdminCategoryService**: Tách biệt logic quản lý danh mục cho Admin.

### 3. Repository
Các Repository vẫn giữ nguyên, phục vụ truy xuất dữ liệu cho cả User và Admin flow. Ví dụ `IOrderRepository` có thêm các custom query như `findByOrderStatus` để phục vụ Admin.
