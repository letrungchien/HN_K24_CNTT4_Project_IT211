# Luồng hoạt động các chức năng — Project_WebService_IT211

> Stack: Spring Boot 4.0.6 · Java 17 · Spring Security + JWT · MySQL · Cloudinary

---

## Mục lục

1. [Xác thực (Auth)](#1-xác-thực-auth)
   - 1.1 Đăng nhập
   - 1.2 Đăng ký
   - 1.3 Làm mới Token
   - 1.4 Đăng xuất
   - 1.5 Đổi mật khẩu
   - 1.6 Quên mật khẩu (FR-10)
   - 1.7 Đặt lại mật khẩu (FR-10)
2. [Quản lý người dùng (Admin)](#2-quản-lý-người-dùng-admin)
3. [Quản lý cụm sân & đặt sân (Manager)](#3-quản-lý-cụm-sân--đặt-sân-manager)
4. [Hình ảnh sân cầu (FR-09 — Cloudinary)](#4-hình-ảnh-sân-cầu-fr-09--cloudinary)
5. [Đặt sân (Customer)](#5-đặt-sân-customer)
6. [Cơ chế bảo mật JWT](#6-cơ-chế-bảo-mật-jwt)

---

## 1. Xác thực (Auth)

### 1.1 Đăng nhập

```
POST /api/v1/auth/login
Body: { username, password }
```

```
Client
  │
  ▼
AuthController.login()
  │
  ▼
AuthService.login(username, password)
  ├─ UserRepository.findByUsername()
  │     └─ Không tìm thấy → RuntimeException("Tài khoản không tồn tại") → 400
  ├─ passwordEncoder.matches(rawPwd, encodedPwd)
  │     └─ Sai → RuntimeException("Sai mật khẩu") → 400
  ├─ Kiểm tra isEnabled == false → RuntimeException("Tài khoản đã bị vô hiệu hoá") → 400
  ├─ JwtUtil.generateToken(username, role) → accessToken (JWT)
  ├─ RefreshTokenRepository.findByUsername()
  │     ├─ Đã có → lấy token cũ
  │     └─ Chưa có → tạo mới, save vào DB
  └─ Trả JWTResponse { username, fullName, email, authorities, token, refreshToken }
  │
  ▼
Response 200: ApiResponse<JWTResponse>
```

---

### 1.2 Đăng ký

```
POST /api/v1/auth/register
Body: { username, password, fullName, email, phoneNumber }
```

```
Client
  │
  ▼
AuthController.register()
  │  @Valid → validate DTO (NotBlank, Size, Email, Pattern)
  │     └─ Lỗi → MethodArgumentNotValidException → 400
  ▼
AuthService.register(UserDTO)
  ├─ UserRepository.findByUsername() → đã tồn tại → RuntimeException → 400
  ├─ Tạo User { role="CUSTOMER", isEnabled=true, password=BCrypt(raw) }
  └─ UserRepository.save(user)
  │
  ▼
Response 201: ApiResponse<UserResponse>
```

---

### 1.3 Làm mới Token

```
POST /api/v1/auth/refresh?username=&refreshToken=
```

```
Client
  │
  ▼
AuthController.refresh()
  │
  ▼
AuthService.refresh(username, refreshToken)
  ├─ RefreshTokenRepository.findByUsername() → không có → RuntimeException → 400
  ├─ So sánh refreshToken value → không khớp → RuntimeException → 400
  ├─ UserRepository.findByUsername() → lấy thông tin user
  ├─ JwtUtil.generateToken() → accessToken mới
  └─ Trả JWTResponse (giữ nguyên refreshToken cũ)
  │
  ▼
Response 200: ApiResponse<JWTResponse>
```

---

### 1.4 Đăng xuất

```
POST /api/v1/auth/logout
Header: Authorization: Bearer <token>
```

```
Client
  │
  ▼
AuthController.logout()
  │
  ▼
BlacklistService.add(token)
  ├─ Lưu token vào bảng token_blacklist (DB)
  └─ JwtFilter sẽ từ chối mọi request tiếp theo dùng token này
  │
  ▼
Response 200: "Đăng xuất thành công"
```

---

### 1.5 Đổi mật khẩu

```
PATCH /api/v1/auth/change-password
Header: Authorization: Bearer <token>
Body: { oldPassword, newPassword, confirmPassword }
```

```
Client (đã đăng nhập)
  │
  ▼
AuthController.changePassword()
  │  Lấy username từ Authentication (JWT đã được filter xác thực)
  │
  ▼
AuthService.changePassword(username, ChangePasswordRequest)
  ├─ UserRepository.findByUsername() → NotFoundException → 404
  ├─ passwordEncoder.matches(oldPwd, storedHash) → sai → RuntimeException → 400
  ├─ newPassword != confirmPassword → RuntimeException → 400
  └─ BCrypt(newPassword) → UserRepository.save()
  │
  ▼
Response 200: "Đổi mật khẩu thành công"
```

---

### 1.6 Quên mật khẩu (FR-10)

```
POST /api/v1/auth/forgot-password   [PUBLIC — không cần JWT]
Body: { email }
```

```
Client
  │
  ▼
AuthController.forgotPassword()
  │  @Valid → validate email format
  │
  ▼
AuthService.forgotPassword(ForgotPasswordRequest)
  │
  ├─ UserRepository.findByEmail(email)
  │     └─ Không tìm thấy → KHÔNG báo lỗi (tránh lộ thông tin)
  │
  └─ [Nếu tìm thấy user]
       ├─ PasswordResetTokenRepository.deleteAllByUserId() → xóa token cũ
       ├─ Tạo token = UUID.randomUUID()
       ├─ Tạo PasswordResetToken { token, user, expiresAt=now+15min, usedAt=null }
       ├─ PasswordResetTokenRepository.save()
       └─ EmailService.sendResetPasswordEmail(email, token, fullName)
             └─ Gửi email qua Gmail SMTP chứa link:
                http://localhost:8080/api/v1/auth/reset-password?token=<UUID>
  │
  ▼
Response 200: "Nếu email tồn tại trong hệ thống, bạn sẽ nhận được hướng dẫn..."
              (luôn trả 200 dù email có hay không)
```

---

### 1.7 Đặt lại mật khẩu (FR-10)

```
POST /api/v1/auth/reset-password    [PUBLIC — không cần JWT]
Body: { token, newPassword, confirmPassword }
```

```
Client (nhấn link trong email)
  │
  ▼
AuthController.resetPassword()
  │  @Valid → validate fields
  │
  ▼
AuthService.resetPassword(ResetPasswordRequest)
  ├─ newPassword != confirmPassword → RuntimeException → 400
  ├─ PasswordResetTokenRepository.findByToken(token)
  │     └─ Không tìm thấy → RuntimeException("Token không hợp lệ") → 400
  ├─ token.usedAt != null → RuntimeException("Token đã được sử dụng") → 400
  ├─ now > token.expiresAt → RuntimeException("Token đã hết hạn") → 400
  ├─ BCrypt(newPassword) → user.setPassword() → UserRepository.save()
  └─ token.setUsedAt(now) → PasswordResetTokenRepository.save()
        (đánh dấu token đã dùng — chỉ dùng 1 lần)
  │
  ▼
Response 200: "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại"
```

---

## 2. Quản lý người dùng (Admin)

> Tất cả endpoint yêu cầu role **ADMIN**

### Tạo tài khoản

```
POST /api/v1/admin/users
Body: { username, password, fullName, email, phoneNumber, role }
```

```
UserController.createUser()
  │  @Valid → validate CreateUserRequest (role phải là ADMIN|MANAGER|CUSTOMER)
  ▼
UserService.createUser(request)
  ├─ Kiểm tra username trùng → RuntimeException → 400
  ├─ BCrypt(password)
  └─ UserRepository.save()
  │
  ▼
Response 201: ApiResponse<UserResponse>
```

### Xem / Tìm kiếm / Cập nhật / Xóa

| Endpoint | Mô tả |
|----------|-------|
| `GET /api/v1/admin/users` | Lấy tất cả user |
| `GET /api/v1/admin/users/search?keyword=` | Tìm theo tên (JPQL LIKE, không phân biệt hoa thường) |
| `PUT /api/v1/admin/users/{id}` | Cập nhật thông tin user |
| `DELETE /api/v1/admin/users/{id}` | Xóa user |

---

## 3. Quản lý cụm sân & đặt sân (Manager)

> Tất cả endpoint yêu cầu role **MANAGER**

```
GET  /api/v1/manager/clusters               → Lấy danh sách cụm sân của manager hiện tại
GET  /api/v1/manager/courts/bookings        → Xem tất cả booking
PATCH /api/v1/manager/bookings/{id}/confirm → Xác nhận booking
PATCH /api/v1/manager/bookings/{id}/cancel  → Huỷ booking
```

**Luồng xác nhận / huỷ booking:**

```
ManagerController.confirmBooking(bookingId, auth)
  │
  ▼
ManagerService.confirmBooking(bookingId, username)
  ├─ BookingRepository.findById() → NotFoundException → 404
  ├─ Kiểm tra booking thuộc cluster của manager → RuntimeException → 400
  ├─ booking.setStatus("CONFIRMED")
  └─ BookingRepository.save()
  │
  ▼
Response 200: ApiResponse<BookingResponse>
```

---

## 4. Hình ảnh sân cầu (FR-09 — Cloudinary)

> Yêu cầu role **MANAGER**, sân phải thuộc cluster của manager đó

```
POST /api/v1/manager/courts/{courtId}/images
Content-Type: multipart/form-data
Form field: files (List<MultipartFile>)
```

```
Client (Manager)
  │
  ▼
CourtImageController.uploadImages(courtId, files, auth)
  │
  ▼
CourtImageService.uploadImages(courtId, files, username)
  │
  ├─ CourtRepository.findById(courtId) → NotFoundException → 404
  ├─ Kiểm tra court.cluster.manager.username == username
  │     └─ Không khớp → RuntimeException("Không có quyền") → 400
  │
  ├─ Validate từng file:
  │     ├─ file.isEmpty() → RuntimeException → 400
  │     ├─ contentType không bắt đầu "image/" → RuntimeException → 400
  │     └─ size > 5MB → RuntimeException → 400
  │
  ├─ Tính nextOrder = MAX(displayOrder) + 1
  │
  └─ Với mỗi file:
       ├─ CloudinaryService.uploadImage(file)
       │     ├─ cloudinary.uploader().upload(bytes, { folder:"courts" })
       │     │     └─ IOException / lỗi mạng → CloudStorageException
       │     │           └─ GlobalExceptionHandler → 503 Service Unavailable
       │     └─ Trả secure_url (HTTPS, CDN Cloudinary)
       ├─ Tạo CourtImage { fileName, imageUrl=secureUrl, displayOrder, uploadedAt }
       └─ CourtImageRepository.save()
  │
  ├─ syncCourtThumbnail(court):
  │     ├─ Lấy ảnh displayOrder nhỏ nhất
  │     └─ court.setImageUrl(firstImage.imageUrl) → CourtRepository.save()
  │
  ▼
Response 201: ApiResponse<List<CourtImageResponse>>
              (mỗi item: id, imageUrl, fileName, displayOrder, uploadedAt)
```

**Sơ đồ lưu trữ — Cloudinary (Stateless):**

```
Trước (local disk):          Sau (Cloudinary):
┌─────────────┐              ┌─────────────┐
│   Server    │              │   Server    │
│  /uploads/  │              │  (stateless)│
│  courts/    │   ──────►    │             │
│  abc.jpg    │              └──────┬──────┘
└─────────────┘                     │ upload bytes
                                    ▼
                             ┌─────────────┐
                             │  Cloudinary │
                             │  CDN Cloud  │
                             │  secure_url │
                             └─────────────┘
                                    │ URL lưu vào DB
                                    ▼
                             ┌─────────────┐
                             │   MySQL DB  │
                             │ court_image │
                             │ image_url   │
                             └─────────────┘
```

---

## 5. Đặt sân (Customer)

> Yêu cầu đăng nhập (role CUSTOMER / MANAGER / ADMIN)

### Đặt sân mới

```
POST /api/v1/customer/bookings
Body: { courtId, bookingDate, timeSlot }
```

```
BookingController.createBooking(BookingDTO, auth)
  │
  ▼
BookingService.createBooking(request, username)
  ├─ UserRepository.findByUsername() → NotFoundException → 404
  ├─ CourtRepository.findById(courtId) → NotFoundException → 404
  ├─ Kiểm tra court.isAvailable == false → RuntimeException → 400
  ├─ BookingRepository.findByCourtIdAndBookingDateAndTimeSlot()
  │     └─ Đã có booking → RuntimeException("Khung giờ đã được đặt") → 400
  ├─ Tính totalPrice
  ├─ Tạo Booking { status="PENDING", createdAt=now }
  └─ BookingRepository.save()
  │
  ▼
Response 201: ApiResponse<BookingResponse>
```

### Xem lịch sử đặt sân

```
GET /api/v1/customer/bookings/my-history

BookingService.getMyBookings(username)
  ├─ UserRepository.findByUsername()
  └─ BookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
  │
  ▼
Response 200: ApiResponse<List<BookingResponse>>
```

---

## 6. Cơ chế bảo mật JWT

```
Client gửi request
  │
  ▼
JwtFilter (OncePerRequestFilter)
  ├─ Lấy token từ header: "Authorization: Bearer <token>"
  ├─ JwtUtil.validateToken(token) → hết hạn / sai chữ ký → 401
  ├─ BlacklistService / TokenBlacklistRepository.existsByToken(token)
  │     └─ Token đã blacklist (logout) → 401
  ├─ JwtUtil.extractUsername(token)
  ├─ UserDetailsService.loadUserByUsername() → load từ DB
  └─ SecurityContextHolder.setAuthentication()
  │
  ▼
SecurityFilterChain kiểm tra role:
  ├─ /api/v1/admin/**    → hasRole("ADMIN")
  ├─ /api/v1/manager/**  → hasRole("MANAGER")
  ├─ /api/v1/customer/** → hasAnyRole("CUSTOMER","MANAGER","ADMIN")
  ├─ /api/v1/auth/login, /register, /refresh,
  │   /forgot-password, /reset-password → permitAll()
  └─ Còn lại → authenticated()
  │
  ▼
Controller xử lý request
```

**Cấu trúc JWT Payload:**
```json
{
  "sub": "username",
  "role": "MANAGER",
  "iat": 1718000000,
  "exp": 1718000600
}
```

---

## Xử lý lỗi tập trung (GlobalExceptionHandler)

| Exception | HTTP Status | Mô tả |
|-----------|-------------|-------|
| `MethodArgumentNotValidException` | 400 Bad Request | Lỗi validate DTO |
| `NotFoundException` | 404 Not Found | Không tìm thấy resource |
| `CloudStorageException` | 503 Service Unavailable | Cloudinary lỗi (FR-09) |
| `RuntimeException` | 400 Bad Request | Lỗi nghiệp vụ chung |

Tất cả response đều theo chuẩn `ApiResponse<T>`:
```json
{
  "status": 200,
  "success": true,
  "message": "Đăng nhập thành công",
  "data": { ... },
  "timestamp": "2026-06-12T15:30:00"
}
```
