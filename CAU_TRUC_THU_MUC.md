# CẤU TRÚC CÂY THỬ MỤC DỰ ÁN E-COMMERCE BACKEND

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
│   │   │               │   ├── AuthController.java            # Authentication endpoints
│   │   │               │   ├── CartController.java            # Cart management endpoints
│   │   │               │   ├── CartItemController.java      # Cart item operations
│   │   │               │   ├── HomeController.java           # Public home endpoints
│   │   │               │   ├── OrderController.java          # Order management endpoints
│   │   │               │   ├── ProductController.java        # Product endpoints
│   │   │               │   ├── RatingController.java         # Rating endpoints
│   │   │               │   ├── ReviewController.java         # Review endpoints
│   │   │               │   └── UserController.java          # User profile endpoints
│   │   │               │
│   │   │               ├── dto/                               # Data Transfer Objects
│   │   │               │   ├── ApiResponse.java              # Generic API response
│   │   │               │   │
│   │   │               │   ├── Auth/                          # Authentication DTOs
│   │   │               │   │   ├── AuthResponse.java         # Auth response DTO
│   │   │               │   │   └── LoginRequest.java         # Login request DTO
│   │   │               │   │
│   │   │               │   ├── Cart/                          # Cart DTOs
│   │   │               │   │   ├── AddItemRequest.java       # Add item to cart request
│   │   │               │   │   ├── CartDTO.java              # Cart data transfer object
│   │   │               │   │   ├── CartItemDTO.java          # Cart item DTO
│   │   │               │   │   └── UpdateCartItem.java       # Update cart item request
│   │   │               │   │
│   │   │               │   ├── Product/                        # Product DTOs
│   │   │               │   │   ├── CreateProductRequest.java # Create product request
│   │   │               │   │   ├── ProductDTO.java           # Product data transfer object
│   │   │               │   │   └── ReviewDTO.java           # Review DTO
│   │   │               │   │
│   │   │               │   ├── Rating/                       # Rating DTOs
│   │   │               │   │   └── RatingRequest.java        # Rating request DTO
│   │   │               │   │
│   │   │               │   └── Review/                       # Review DTOs
│   │   │               │       └── ReviewRequest.java         # Review request DTO
│   │   │               │
│   │   │               ├── exception/                         # Custom exceptions
│   │   │               │   ├── CartItemException.java       # Cart item exceptions
│   │   │               │   ├── OrderException.java          # Order exceptions
│   │   │               │   ├── ProductException.java        # Product exceptions
│   │   │               │   └── UserException.java           # User exceptions
│   │   │               │
│   │   │               ├── model/                             # Entity models (JPA)
│   │   │               │   ├── Address.java                  # Address entity
│   │   │               │   ├── Cart.java                     # Cart entity
│   │   │               │   ├── CartItem.java                 # Cart item entity
│   │   │               │   ├── Category.java                # Category entity
│   │   │               │   ├── Order.java                    # Order entity
│   │   │               │   ├── OrderItem.java                # Order item entity
│   │   │               │   ├── Product.java                  # Product entity
│   │   │               │   ├── ProductColor.java            # Product color entity
│   │   │               │   ├── ProductImage.java            # Product image entity
│   │   │               │   ├── Rating.java                   # Rating entity
│   │   │               │   ├── Review.java                   # Review entity
│   │   │               │   └── User.java                    # User entity
│   │   │               │
│   │   │               ├── repositories/                      # JPA Repositories
│   │   │               │   ├── IAddressRepository.java       # Address repository interface
│   │   │               │   ├── ICartItemRepository.java      # Cart item repository interface
│   │   │               │   ├── ICartRepository.java          # Cart repository interface
│   │   │               │   ├── ICategoryRepository.java     # Category repository interface
│   │   │               │   ├── IOrderItemRepository.java     # Order item repository interface
│   │   │               │   ├── IOrderRepository.java         # Order repository interface
│   │   │               │   ├── IProductImageRepository.java  # Product image repository interface
│   │   │               │   ├── IProductRepository.java       # Product repository interface
│   │   │               │   ├── IRatingRepository.java        # Rating repository interface
│   │   │               │   ├── IReviewRepository.java        # Review repository interface
│   │   │               │   └── UserRepository.java           # User repository interface
│   │   │               │
│   │   │               └── service/                           # Service layer
│   │   │                   ├── ICartItemService.java         # Cart item service interface
│   │   │                   ├── ICartService.java             # Cart service interface
│   │   │                   ├── IOrderItemService.java       # Order item service interface
│   │   │                   ├── IOrderService.java           # Order service interface
│   │   │                   ├── IProductService.java         # Product service interface
│   │   │                   ├── IRatingService.java           # Rating service interface
│   │   │                   ├── IReviewService.java          # Review service interface
│   │   │                   ├── IUserService.java            # User service interface
│   │   │                   │
│   │   │                   └── serviceImpl/                   # Service implementations
│   │   │                       ├── CartItemServiceImpl.java # Cart item service implementation
│   │   │                       ├── CartServiceImpl.java      # Cart service implementation
│   │   │                       ├── CustomUserServiceImpl.java # Custom user details service
│   │   │                       ├── OrderItemService.java    # Order item service implementation
│   │   │                       ├── OrderService.java        # Order service implementation
│   │   │                       ├── ProductServiceImpl.java  # Product service implementation
│   │   │                       ├── RatingService.java       # Rating service implementation
│   │   │                       ├── ReviewService.java       # Review service implementation
│   │   │                       └── UserService.java         # User service implementation
│   │   │
│   │   └── resources/                                         # Resource files
│   │       ├── application.properties                       # Application configuration
│   │       └── logback.xml                                   # Logback logging configuration
│   │
│   └── test/                                                 # Test source code
│       └── java/
│           └── phamiz/
│               └── ecommerce/
│                   └── backend/
│                       └── BackendApplicationTests.java     # Main application tests
│
└── target/                                                   # Build output directory (generated)
    ├── classes/                                             # Compiled classes
    ├── generated-sources/                                   # Generated source files
    ├── generated-test-sources/                              # Generated test sources
    └── test-classes/                                        # Compiled test classes
```

---

## MÔ TẢ CÁC THỦ MỤC CHÍNH

### 📁 **Root Directory**
- **Dockerfile**: File cấu hình Docker để containerize ứng dụng
- **pom.xml**: File cấu hình Maven chứa dependencies và build settings
- **mvnw, mvnw.cmd**: Maven wrapper scripts để chạy Maven mà không cần cài đặt

### 📁 **src/main/java/phamiz/ecommerce/backend/**
Thư mục gốc chứa toàn bộ source code Java:

#### 🔧 **config/**
Các class cấu hình hệ thống:
- **AppConfig**: Cấu hình Security, CORS, PasswordEncoder
- **JwtConstant**: Hằng số cho JWT
- **JwtProvider**: Tạo và xử lý JWT token
- **JwtValidator**: Filter kiểm tra JWT token trong mỗi request

#### 🎮 **controller/**
REST API Controllers xử lý HTTP requests:
- **AuthController**: Đăng ký/đăng nhập (`/auth/signup`, `/auth/signin`)
- **ProductController**: Quản lý sản phẩm (`/api/product/**`)
- **CartController**: Quản lý giỏ hàng (`/api/cart/**`)
- **CartItemController**: Thao tác trên CartItem (`/api/cartItem/**`)
- **OrderController**: Quản lý đơn hàng (`/api/orders/**`)
- **UserController**: Quản lý người dùng (`/api/users/**`)
- **RatingController**: Quản lý đánh giá (`/api/ratings/**`)
- **ReviewController**: Quản lý review (`/api/reviews/**`)
- **HomeController**: Public endpoints (`/home/**`)

#### 📦 **dto/**
Data Transfer Objects - các class truyền dữ liệu giữa các layers:
- **Auth/**: LoginRequest, AuthResponse
- **Cart/**: CartDTO, CartItemDTO, AddItemRequest, UpdateCartItem
- **Product/**: ProductDTO, CreateProductRequest, ReviewDTO
- **Rating/**: RatingRequest
- **Review/**: ReviewRequest
- **ApiResponse**: Generic response wrapper

#### ⚠️ **exception/**
Custom exception classes:
- **CartItemException**: Exception cho CartItem operations
- **OrderException**: Exception cho Order operations
- **ProductException**: Exception cho Product operations
- **UserException**: Exception cho User operations

#### 📊 **model/**
JPA Entity classes - mapping với database tables:
- **User**: Người dùng
- **Product**: Sản phẩm
- **Category**: Danh mục sản phẩm
- **Cart**: Giỏ hàng
- **CartItem**: Mục trong giỏ hàng
- **Order**: Đơn hàng
- **OrderItem**: Mục trong đơn hàng
- **Address**: Địa chỉ giao hàng
- **Rating**: Đánh giá sản phẩm
- **Review**: Review sản phẩm
- **ProductImage**: Hình ảnh sản phẩm
- **ProductColor**: Màu sắc sản phẩm

#### 💾 **repositories/**
JPA Repository interfaces - truy cập database:
- Các interface extends `JpaRepository` cho từng entity
- Cung cấp các method CRUD và custom queries

#### 🔨 **service/**
Service layer - business logic:
- **Interface files** (`I*.java`): Định nghĩa contracts cho services
- **serviceImpl/**: Implementations của các service interfaces:
  - **CartServiceImpl**: Logic xử lý giỏ hàng
  - **CartItemServiceImpl**: Logic xử lý CartItem
  - **OrderService**: Logic xử lý đơn hàng
  - **ProductServiceImpl**: Logic xử lý sản phẩm
  - **UserService**: Logic xử lý người dùng
  - **RatingService**: Logic xử lý đánh giá
  - **ReviewService**: Logic xử lý review
  - **CustomUserServiceImpl**: Custom UserDetailsService cho Spring Security

### 📁 **src/main/resources/**
Resource files:
- **application.properties**: Cấu hình database, JPA, logging
- **logback.xml**: Cấu hình Logback cho logging

### 📁 **src/test/**
Test source code:
- **BackendApplicationTests.java**: Unit tests cho application

### 📁 **target/**
Thư mục build output (generated, không commit vào git):
- Compiled `.class` files
- Generated sources
- Test classes

---

## TỔNG QUAN KIẾN TRÚC

Dự án tuân theo kiến trúc **Layered Architecture** của Spring Boot:

```
┌─────────────────────────────────┐
│      Controller Layer           │  ← REST API Endpoints
├─────────────────────────────────┤
│      Service Layer              │  ← Business Logic
├─────────────────────────────────┤
│      Repository Layer           │  ← Data Access
├─────────────────────────────────┤
│      Model Layer                │  ← Entity/Domain Models
└─────────────────────────────────┘
```

**Luồng xử lý:**
1. **Request** → Controller nhận HTTP request
2. **Controller** → Gọi Service layer để xử lý business logic
3. **Service** → Gọi Repository để truy vấn database
4. **Repository** → Tương tác với database qua JPA
5. **Response** ← Controller trả về kết quả cho client

---

## SỐ LƯỢNG FILE THEO LOẠI

- **Controllers**: 9 files
- **DTOs**: 15 files (bao gồm các file trong subfolders)
- **Models/Entities**: 12 files
- **Repositories**: 11 interfaces
- **Services**: 9 interfaces + 9 implementations = 18 files
- **Config**: 4 files
- **Exceptions**: 4 files
- **Total Java Source Files**: ~75 files

---

**Ghi chú:** Thư mục `target/` được tạo tự động khi build project và không nên commit vào git repository.




