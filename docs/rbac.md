# Role-Based Access Control (RBAC) Documentation

## 목차
- [개요](#개요)
- [역할 정의](#역할-정의)
- [권한 매트릭스](#권한-매트릭스)
- [데이터베이스 스키마](#데이터베이스-스키마)
- [구현 세부사항](#구현-세부사항)
- [API 엔드포인트 권한](#api-엔드포인트-권한)
- [관리자 계정](#관리자-계정)
- [보안 고려사항](#보안-고려사항)
- [역할 관리](#역할-관리)

---

## 개요

마당서점 시스템은 Role-Based Access Control (RBAC)을 사용하여 사용자별로 차별화된 권한을 부여합니다. 각 사용자는 Customer 테이블의 `role` 필드에 저장된 역할에 따라 시스템의 특정 기능에만 접근할 수 있습니다.

### RBAC의 장점

- **보안 강화**: 최소 권한 원칙(Principle of Least Privilege) 적용
- **관리 용이성**: 역할 기반으로 권한을 그룹화하여 관리
- **확장성**: 새로운 역할 추가 및 권한 조정 용이
- **감사 추적**: 역할별 작업 기록 추적 가능

---

## 역할 정의

마당서점 시스템은 4가지 역할을 정의합니다:

### 1. customer (일반 고객)

**설명**: 기본 역할로 모든 신규 사용자에게 자동 할당됩니다.

**권한**:
- ✅ 도서 목록 조회
- ✅ 도서 상세 정보 조회
- ✅ 도서 검색 및 필터링
- ✅ 도서 구매 (주문 생성)
- ✅ 본인의 주문 내역 조회
- ✅ 본인의 주문 취소
- ❌ 타인의 주문 조회/수정/삭제
- ❌ 도서 등록/수정/삭제
- ❌ 통계 대시보드 접근

**사용 사례**: 온라인 서점에서 책을 구매하는 일반 고객

---

### 2. manager (매니저)

**설명**: 서점 운영을 담당하는 관리자입니다.

**권한**:
- ✅ customer의 모든 권한
- ✅ **도서 관리**: 도서 등록, 수정, 삭제
- ✅ **주문 관리**: 모든 주문 조회, 주문 상태 변경
- ✅ **통계 조회**: 판매 통계, 베스트셀러, 고객별/출판사별 통계
- ✅ 고객 정보 조회
- ❌ 사용자 역할 변경
- ❌ 시스템 설정 변경
- ❌ 관리자 계정 관리

**사용 사례**:
- 재고 관리자: 도서 등록 및 가격 조정
- 주문 처리 담당자: 주문 확인 및 처리
- 영업 매니저: 판매 통계 분석

---

### 3. publisher (출판사)

**설명**: 출판사 담당자로 자사 도서를 관리합니다.

**권한**:
- ✅ customer의 모든 권한
- ✅ **자사 도서 관리**: 본인 출판사의 도서만 등록, 수정, 삭제
- ✅ **자사 도서 통계**: 본인 출판사 도서의 판매 통계 조회
- ❌ 타 출판사 도서 수정/삭제
- ❌ 전체 주문 관리
- ❌ 사용자 관리

**사용 사례**:
- 굿스포츠, 나무수, 대한미디어 등의 출판사 담당자
- 자사 도서의 정보 업데이트 및 판매 현황 모니터링

**참고**: 현재 버전에서는 publisher 역할이 정의되어 있으나 실제 구현은 미완성 상태입니다. 향후 출판사별 도서 관리 기능 추가 시 활성화될 예정입니다.

---

### 4. admin (시스템 관리자)

**설명**: 시스템의 모든 권한을 가진 최고 관리자입니다.

**권한**:
- ✅ **모든 권한**: manager와 publisher의 모든 권한
- ✅ **사용자 관리**: 사용자 역할 변경, 계정 활성화/비활성화
- ✅ **시스템 설정**: 서버 설정, 데이터베이스 백업/복원
- ✅ **감사 로그**: 모든 사용자의 활동 로그 조회
- ✅ **권한 관리**: 새로운 역할 추가 및 권한 조정

**사용 사례**:
- 시스템 관리자: 전체 시스템 관리
- IT 관리자: 기술적 문제 해결 및 시스템 유지보수

---

## 권한 매트릭스

| 기능 | customer | manager | publisher | admin |
|------|----------|---------|-----------|-------|
| **도서 조회** | ✅ | ✅ | ✅ | ✅ |
| **도서 검색** | ✅ | ✅ | ✅ | ✅ |
| **도서 구매** | ✅ | ✅ | ✅ | ✅ |
| **본인 주문 조회** | ✅ | ✅ | ✅ | ✅ |
| **본인 주문 취소** | ✅ | ✅ | ✅ | ✅ |
| **도서 등록** | ❌ | ✅ | ✅ (자사만) | ✅ |
| **도서 수정** | ❌ | ✅ | ✅ (자사만) | ✅ |
| **도서 삭제** | ❌ | ✅ | ✅ (자사만) | ✅ |
| **전체 주문 조회** | ❌ | ✅ | ❌ | ✅ |
| **주문 상태 변경** | ❌ | ✅ | ❌ | ✅ |
| **판매 통계 조회** | ❌ | ✅ | ✅ (자사만) | ✅ |
| **고객 정보 조회** | ❌ | ✅ | ❌ | ✅ |
| **사용자 역할 변경** | ❌ | ❌ | ❌ | ✅ |
| **시스템 설정** | ❌ | ❌ | ❌ | ✅ |
| **감사 로그 조회** | ❌ | ❌ | ❌ | ✅ |

---

## 데이터베이스 스키마

### Customer 테이블

```sql
CREATE TABLE `Customer` (
  `custid` int NOT NULL AUTO_INCREMENT,
  `name` varchar(40) DEFAULT NULL,
  `address` varchar(50) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'customer',
  PRIMARY KEY (`custid`),
  KEY `idx_customer_name` (`name`),
  KEY `idx_customer_role` (`role`),
  CONSTRAINT `chk_customer_role` CHECK (
    `role` IN ('customer', 'publisher', 'admin', 'manager')
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 주요 특징

1. **role 필드**:
   - 타입: `VARCHAR(20)`
   - NOT NULL 제약
   - 기본값: `'customer'`

2. **CHECK 제약 조건** (`chk_customer_role`):
   - 허용된 값: `'customer'`, `'publisher'`, `'admin'`, `'manager'`
   - 유효하지 않은 역할 삽입 방지

3. **인덱스** (`idx_customer_role`):
   - role 필드에 인덱스 생성
   - 역할별 사용자 조회 성능 향상

---

## 구현 세부사항

### 백엔드 (Java)

#### 1. 세션 관리

현재 로그인한 사용자의 정보는 프론트엔드의 `localStorage`에 저장됩니다:

```javascript
// 로그인 시
localStorage.setItem('custid', customer.custid);
localStorage.setItem('name', customer.name);
localStorage.setItem('role', customer.role);  // RBAC 핵심 정보

// 권한 확인
const userRole = localStorage.getItem('role');
if (userRole === 'admin' || userRole === 'manager') {
    // 관리자 기능 표시
}
```

#### 2. 권한 확인 로직 (구현 예정)

서버 측에서 권한을 확인하는 유틸리티 클래스:

```java
// src/com/madang/util/RoleChecker.java (예정)
public class RoleChecker {

    public static boolean isAdmin(String role) {
        return "admin".equals(role);
    }

    public static boolean isManager(String role) {
        return "manager".equals(role);
    }

    public static boolean hasManagementAccess(String role) {
        return isAdmin(role) || isManager(role);
    }

    public static boolean canModifyBook(String role, String publisher) {
        if (isAdmin(role) || isManager(role)) {
            return true;
        }
        if ("publisher".equals(role)) {
            // 출판사는 자사 도서만 수정 가능 (구현 필요)
            return true;
        }
        return false;
    }

    public static boolean canViewAllOrders(String role) {
        return isAdmin(role) || isManager(role);
    }
}
```

#### 3. Handler에서 권한 확인 (예시)

```java
// src/com/madang/handler/BookHandler.java (예시)
@Override
protected String handlePost(Map<String, String> params) throws Exception {
    String action = params.get("action");
    String role = params.get("role");  // 세션에서 가져온 역할

    if ("create".equals(action)) {
        // 도서 생성은 manager 이상 권한 필요
        if (!RoleChecker.hasManagementAccess(role)) {
            return errorResponse("권한이 없습니다.", 403);
        }

        // 도서 생성 로직
        // ...
    }

    return errorResponse("Invalid action");
}
```

### 프론트엔드 (JavaScript)

#### 1. UI 조건부 렌더링

```javascript
// frontend/js/dashboard.js (예시)
function initDashboard() {
    const userRole = localStorage.getItem('role');

    // 관리자 전용 메뉴 표시
    if (userRole === 'admin') {
        document.getElementById('admin-menu').style.display = 'block';
    }

    // 매니저 이상 권한
    if (userRole === 'admin' || userRole === 'manager') {
        document.getElementById('management-menu').style.display = 'block';
    }

    // 일반 고객은 대시보드 접근 불가
    if (userRole === 'customer') {
        alert('접근 권한이 없습니다.');
        window.location.href = 'books.html';
    }
}
```

#### 2. API 호출 시 역할 전달

```javascript
// frontend/js/api.js
async function fetchAPI(endpoint, options = {}) {
    const custid = localStorage.getItem('custid');
    const role = localStorage.getItem('role');

    // URL에 role 파라미터 추가
    const url = new URL(API_BASE_URL + endpoint);
    if (custid) {
        url.searchParams.append('custid', custid);
    }
    if (role) {
        url.searchParams.append('role', role);
    }

    const response = await fetch(url, options);
    return response.json();
}
```

---

## API 엔드포인트 권한

### 도서 관리 API (`/api/books`)

| 액션 | 메서드 | 필요 권한 | 설명 |
|------|--------|-----------|------|
| `list` | GET | 모든 사용자 | 도서 목록 조회 |
| `detail` | GET | 모든 사용자 | 도서 상세 조회 |
| `search` | GET | 모든 사용자 | 도서 검색 |
| `create` | POST | manager, admin | 도서 등록 |
| `update` | POST | manager, admin, publisher* | 도서 수정 |
| `delete` | DELETE | manager, admin, publisher* | 도서 삭제 |

*publisher는 자사 도서만 수정/삭제 가능

### 주문 관리 API (`/api/orders`)

| 액션 | 메서드 | 필요 권한 | 설명 |
|------|--------|-----------|------|
| `create` | POST | 모든 사용자 | 주문 생성 |
| `myorders` | GET | 로그인 사용자 | 본인 주문 조회 |
| `list` | GET | manager, admin | 전체 주문 조회 |
| `detail` | GET | 본인 또는 manager, admin | 주문 상세 조회 |
| `cancel` | DELETE | 본인 또는 manager, admin | 주문 취소 |

### 통계 API (`/api/stats`)

| 액션 | 메서드 | 필요 권한 | 설명 |
|------|--------|-----------|------|
| `overview` | GET | manager, admin | 전체 통계 개요 |
| `bestsellers` | GET | manager, admin | 베스트셀러 조회 |
| `by-customer` | GET | manager, admin | 고객별 통계 |
| `by-publisher` | GET | manager, admin, publisher* | 출판사별 통계 |

*publisher는 자사 통계만 조회 가능

### 고객 관리 API (`/api/customers`)

| 액션 | 메서드 | 필요 권한 | 설명 |
|------|--------|-----------|------|
| `login` | GET | 모든 사용자 | 고객 로그인 |
| `list` | GET | admin | 전체 고객 조회 |
| `update-role` | POST | admin | 역할 변경 |

---

## 관리자 계정

### 현재 관리자 목록

| ID | 이름 | 주소 | 역할 | 비고 |
|----|------|------|------|------|
| 1 | 박지성 | 영국 맨체스타 | **admin** | 시스템 관리자 |
| 2 | 김연아 | 대한민국 서울 | **admin** | 시스템 관리자 |
| 3 | 김연경 | 대한민국 경기도 | **manager** | 도서 관리자 |
| 4 | 추신수 | 미국 클리블랜드 | **manager** | 주문 관리자 |
| 5 | 박세리 | 대한민국 대전 | **manager** | 통계 분석 담당 |

### 테스트용 계정

개발 및 테스트 시 위 계정들을 사용하여 각 역할별 기능을 확인할 수 있습니다:

```javascript
// 테스트 로그인 예시
// Admin 테스트: custid=1 (박지성)
// Manager 테스트: custid=3 (김연경)
// Customer 테스트: custid=6 이상 (일반 고객)
```

---

## 보안 고려사항

### 현재 구현 수준 (교육용)

현재 마당서점 시스템은 **교육용 프로젝트**로 다음과 같은 간소화된 보안을 사용합니다:

✅ **구현됨**:
- SQL Injection 방지: PreparedStatement 사용
- 기본 XSS 방지: textContent 사용
- 역할 기반 권한 구분

⚠️ **미구현** (프로덕션 필요):
- 비밀번호 해싱 (BCrypt 등)
- JWT 기반 인증
- HTTPS 암호화 통신
- CSRF 토큰
- 세션 타임아웃
- 서버 측 세션 관리

### 프로덕션 업그레이드 권장사항

실제 운영 환경에 배포 시 다음 보안 기능을 추가해야 합니다:

#### 1. 인증 강화

```sql
-- Customer 테이블에 비밀번호 필드 추가
ALTER TABLE Customer ADD COLUMN password_hash VARCHAR(255);
ALTER TABLE Customer ADD COLUMN salt VARCHAR(255);
```

```java
// 비밀번호 해싱 (BCrypt 사용 권장)
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean checkPassword(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}
```

#### 2. JWT 기반 인증

```java
// JWT 토큰 발급 및 검증
public class JWTUtil {
    private static final String SECRET_KEY = "your-secret-key";

    public static String generateToken(int custid, String role) {
        // JWT 토큰 생성 로직
    }

    public static Claims validateToken(String token) {
        // JWT 토큰 검증 로직
    }
}
```

#### 3. 서버 측 권한 검증

**중요**: 프론트엔드의 권한 확인은 UI 편의성을 위한 것이며, **반드시 서버 측에서도 권한을 검증**해야 합니다.

```java
// 모든 API Handler에서 권한 확인
@Override
protected String handlePost(Map<String, String> params) throws Exception {
    // 1. 토큰 검증
    String token = params.get("token");
    Claims claims = JWTUtil.validateToken(token);

    // 2. 역할 추출
    String role = claims.get("role", String.class);

    // 3. 권한 확인
    if (!RoleChecker.hasManagementAccess(role)) {
        return errorResponse("Unauthorized", 401);
    }

    // 4. 비즈니스 로직 실행
    // ...
}
```

#### 4. 감사 로그

모든 중요 작업에 대한 로그를 기록합니다:

```sql
-- 감사 로그 테이블
CREATE TABLE AuditLog (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    custid INT NOT NULL,
    action VARCHAR(50) NOT NULL,  -- 'CREATE_BOOK', 'DELETE_ORDER' 등
    resource_type VARCHAR(50),    -- 'Book', 'Order' 등
    resource_id INT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    FOREIGN KEY (custid) REFERENCES Customer(custid)
);
```

#### 5. Rate Limiting

API 남용 방지를 위한 요청 제한:

```java
// 간단한 Rate Limiter 예시
public class RateLimiter {
    private static Map<String, Integer> requestCounts = new HashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 100;

    public static boolean isAllowed(String ipAddress) {
        // Rate limiting 로직
    }
}
```

---

## 역할 관리

### 역할 변경 (Admin만 가능)

#### 1. SQL로 직접 변경

```sql
-- 일반 고객을 매니저로 승격
UPDATE Customer SET role = 'manager' WHERE custid = 10;

-- 매니저를 관리자로 승격
UPDATE Customer SET role = 'admin' WHERE custid = 3;

-- 관리자를 일반 고객으로 강등
UPDATE Customer SET role = 'customer' WHERE custid = 5;
```

#### 2. Python 스크립트 사용

```bash
# gen/update_roles.py 스크립트 수정 후 실행
python3 gen/update_roles.py
```

#### 3. API를 통한 변경 (구현 예정)

```javascript
// 관리자 대시보드에서 역할 변경 (향후 구현)
async function changeUserRole(custid, newRole) {
    const response = await fetchAPI('/api/customers', {
        method: 'POST',
        body: JSON.stringify({
            action: 'update-role',
            custid: custid,
            new_role: newRole,
            admin_custid: getCurrentUserId()
        })
    });

    if (response.success) {
        alert('역할이 변경되었습니다.');
    }
}
```

### 역할별 사용자 통계 조회

```sql
-- 역할별 사용자 수 확인
SELECT role, COUNT(*) as count
FROM Customer
GROUP BY role
ORDER BY CASE role
    WHEN 'admin' THEN 1
    WHEN 'manager' THEN 2
    WHEN 'publisher' THEN 3
    WHEN 'customer' THEN 4
END;
```

### 제약 조건 수정

새로운 역할을 추가하려면 CHECK 제약 조건을 수정해야 합니다:

```sql
-- 기존 제약 조건 삭제
ALTER TABLE Customer DROP CONSTRAINT chk_customer_role;

-- 새로운 역할 추가 (예: 'staff')
ALTER TABLE Customer
ADD CONSTRAINT chk_customer_role
CHECK (role IN ('customer', 'publisher', 'admin', 'manager', 'staff'));
```

또는 `gen/alter_role_constraint.py` 스크립트를 수정하여 실행:

```bash
python3 gen/alter_role_constraint.py
```

---

## 추가 구현 고려사항

### 1. 세밀한 권한 관리 (Fine-Grained Access Control)

현재는 역할별로만 권한을 구분하지만, 향후 다음과 같은 세밀한 권한 관리를 고려할 수 있습니다:

```sql
-- 권한 테이블
CREATE TABLE Permission (
    permission_id INT AUTO_INCREMENT PRIMARY KEY,
    permission_name VARCHAR(50) UNIQUE NOT NULL,  -- 'READ_BOOK', 'WRITE_BOOK' 등
    description TEXT
);

-- 역할-권한 매핑 테이블
CREATE TABLE RolePermission (
    role VARCHAR(20) NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (role, permission_id),
    FOREIGN KEY (permission_id) REFERENCES Permission(permission_id)
);
```

### 2. 계층적 역할 구조

역할 간 상속 관계를 정의하여 권한 관리를 단순화:

```
admin
  ├─ manager
  │   └─ publisher
  │       └─ customer
```

### 3. 동적 권한 확인

데이터베이스에서 권한을 동적으로 조회:

```java
public class DynamicRoleChecker {
    public static boolean hasPermission(String role, String permission) {
        String sql = "SELECT COUNT(*) FROM RolePermission rp " +
                     "JOIN Permission p ON rp.permission_id = p.permission_id " +
                     "WHERE rp.role = ? AND p.permission_name = ?";
        // 실행 로직
    }
}
```

---

## 참고 자료

- [OWASP Access Control Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Access_Control_Cheat_Sheet.html)
- [NIST RBAC Standard](https://csrc.nist.gov/projects/role-based-access-control)
- [Spring Security RBAC](https://docs.spring.io/spring-security/reference/servlet/authorization/architecture.html)

---

## 문서 버전 정보

- **작성일**: 2025-10-21
- **버전**: 1.0
- **작성자**: Claude Code
- **최종 수정일**: 2025-10-21

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 | 작성자 |
|------|------|-----------|--------|
| 2025-10-21 | 1.0 | 초기 문서 작성 | Claude Code |
| 2025-10-21 | 1.0 | Customer 테이블 CHECK 제약 조건에 'manager' 역할 추가 | Claude Code |
| 2025-10-21 | 1.0 | 5명의 관리자 계정 생성 (admin 2명, manager 3명) | Claude Code |
