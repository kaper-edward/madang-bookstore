# 마당 온라인 서점 시스템 아키텍처

## 1. 시스템 개요

### 목적
학생들이 CRUD SQL을 학습하면서 실제 동작하는 온라인 서점 시스템을 구축하는 교육용 프로젝트

### 주요 기능
- 도서 조회 및 검색
- 고객 로그인 (세션 기반)
- 도서 주문 및 주문 내역 관리
- 통계 대시보드 (관리자/CEO/개발자용)

---

## 2. 기술 스택

### Frontend
- **HTML5**: 시맨틱 마크업
- **Modern CSS**: Tailwind 스타일의 Utility-first CSS
- **Vanilla JavaScript**: DOM 조작 및 API 통신 (ES6+)

### Backend
- **Java**: JDK 21+
- **JDBC**: MySQL Connector/J
- **HTTP Server**: Java built-in `com.sun.net.httpserver.HttpServer` (교육용 - Tomcat 불필요)
- **Handler**: Custom `ApiHandler` abstract class

### Database
- **MySQL 5.7+**: madangdb
- **테이블**: Book, Customer, Orders (스키마 수정 불가)

### 개발 도구
- **MCP MySQL Server**: 개발/테스트 시 SQL 쿼리 실행 및 검증용

---

## 3. 시스템 아키텍처

```
┌─────────────────────────────────────────────────────┐
│                    Client (Browser)                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │  HTML Pages │  │  Modern CSS │  │ JavaScript  │ │
│  └─────────────┘  └─────────────┘  └─────────────┘ │
└────────────────────────┬────────────────────────────┘
                         │ HTTP/HTTPS (REST API)
                         ▼
┌─────────────────────────────────────────────────────┐
│         Java Built-in HTTP Server (Port 8080)        │
│  ┌───────────────────────────────────────────────┐  │
│  │            Handler Layer (API)                 │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐      │  │
│  │  │  Book    │ │Customer  │ │  Order   │      │  │
│  │  │ Handler  │ │ Handler  │ │ Handler  │ ...  │  │
│  │  └──────────┘ └──────────┘ └──────────┘      │  │
│  │           (extends ApiHandler)                │  │
│  └───────────────────┬───────────────────────────┘  │
│                      │                               │
│  ┌───────────────────▼───────────────────────────┐  │
│  │            DAO Layer (Data Access)             │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐      │  │
│  │  │ BookDAO  │ │Customer  │ │ OrderDAO │      │  │
│  │  │          │ │   DAO    │ │          │      │  │
│  │  └──────────┘ └──────────┘ └──────────┘      │  │
│  └───────────────────┬───────────────────────────┘  │
│                      │ JDBC                          │
│  ┌───────────────────▼───────────────────────────┐  │
│  │         DBConnection (JDBC Connection)         │  │
│  └────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────┘
                         │ JDBC Driver
                         ▼
┌─────────────────────────────────────────────────────┐
│                   MySQL Database                     │
│              ┌─────────────────────┐                 │
│              │     madangdb        │                 │
│              │  ┌────────────────┐ │                 │
│              │  │  Book          │ │                 │
│              │  │  Customer      │ │                 │
│              │  │  Orders        │ │                 │
│              │  └────────────────┘ │                 │
│              └─────────────────────┘                 │
└─────────────────────────────────────────────────────┘
```

---

## 4. 계층별 역할

### 4.1 Presentation Layer (Frontend)

#### HTML Pages (7개)
| 페이지 | 파일명 | 역할 | 주요 기능 |
|--------|--------|------|-----------|
| 메인 | index.html | 서점 소개 | 최신 도서 3권 표시 |
| 로그인 | customer-login.html | 고객 선택 | Customer 목록에서 선택 |
| 도서 목록 | books.html | 도서 조회 | 검색, 필터링 |
| 도서 상세 | book-detail.html | 도서 정보 | 구매하기 |
| 주문 | order.html | 주문 확인 | 주문 생성 |
| 주문 내역 | my-orders.html | 내 주문 | 주문 취소 |
| 대시보드 | dashboard.html | 통계 조회 | 매출, 베스트셀러 |

#### CSS (Utility-first)
- `styles.css`: Tailwind 스타일의 클래스 기반 CSS
- 반응형 디자인 (Mobile First)
- 모던한 UI/UX (카드, 그리드, Flexbox)

#### JavaScript Modules
```javascript
api.js:
  - fetchAPI(endpoint, options) // 공통 API 호출
  - handleError(error)          // 에러 처리

books.js:
  - loadBooks()                 // 도서 목록 로드
  - searchBooks(keyword)        // 도서 검색
  - filterByPublisher(pub)      // 필터링

orders.js:
  - loadMyOrders(custid)        // 주문 내역
  - createOrder(orderData)      // 주문 생성
  - cancelOrder(orderid)        // 주문 취소

dashboard.js:
  - loadStats()                 // 통계 로드
  - renderCharts()              // 차트 렌더링
```

---

### 4.2 Application Layer (Backend - Java)

#### Model Classes (DTO/VO)
```java
Book.java:
  - int bookid
  - String bookname
  - String publisher
  - int price

Customer.java:
  - int custid
  - String name
  - String address
  - String phone
  - String role

Order.java:
  - int orderid
  - int custid
  - int bookid
  - int saleprice
  - Date orderdate
```

#### DAO Classes (Data Access Object)

**BookDAO.java**
```java
public class BookDAO {
    // SELECT 쿼리
    public List<Book> getAllBooks()
    public Book getBookById(int bookId)
    public List<Book> searchBooks(String keyword)
    public List<Book> getBooksByPublisher(String publisher)
    public List<String> getDistinctPublishers()

    // 통계 쿼리 (JOIN)
    public Map<String, Object> getBookStats(int bookId)
}
```

**CustomerDAO.java**
```java
public class CustomerDAO {
    // SELECT 쿼리
    public List<Customer> getAllCustomers()
    public Customer getCustomerById(int custId)
}
```

**OrderDAO.java**
```java
public class OrderDAO {
    // SELECT 쿼리
    public List<Order> getOrdersByCustomer(int custId)
    public Order getOrderById(int orderId)

    // INSERT 쿼리
    public int createOrder(Order order)
    public int getNextOrderId()

    // DELETE 쿼리
    public boolean deleteOrder(int orderId, int custId)

    // 통계 쿼리 (JOIN, GROUP BY, Aggregate)
    public Map<String, Object> getCustomerOrderStats(int custId)
    public List<Map<String, Object>> getBestsellers(int limit)
    public Map<String, Object> getOverallStats()
    public List<Map<String, Object>> getStatsByCustomer()
    public List<Map<String, Object>> getStatsByPublisher()
}
```

#### Handler Classes (API Controller)

모든 Handler는 `ApiHandler` 추상 클래스를 상속하며, `handleGet()`, `handlePost()`, `handleDelete()` 메서드를 구현합니다.

**BookHandler.java** - `/api/books`
```java
GET ?action=list              → getAllBooks()
GET ?action=detail&id=1       → getBookById(1)
GET ?action=search&keyword=축구 → searchBooks("축구")
GET ?action=publisher&name=굿스포츠 → getBooksByPublisher("굿스포츠")
GET ?action=publishers        → getDistinctPublishers()
```

**CustomerHandler.java** - `/api/customers`
```java
GET ?action=list              → getAllCustomers()
GET ?action=detail&id=1       → getCustomerById(1)
```

**OrderHandler.java** - `/api/orders`
```java
GET ?action=list&custid=1     → getOrdersByCustomer(1)
POST ?action=create           → createOrder(Order)
DELETE ?action=delete&id=1&custid=1 → deleteOrder(1, 1)
GET ?action=stats&custid=1    → getCustomerOrderStats(1)
```

**StatsHandler.java** - `/api/stats`
```java
GET ?action=overview          → getOverallStats()
GET ?action=bestsellers       → getBestsellers(5)
GET ?action=customers         → getStatsByCustomer()
GET ?action=publishers        → getStatsByPublisher()
```

---

### 4.3 Data Layer (Database)

#### 데이터베이스 연결 관리

**DBConnection.java**
```java
public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/madangdb";
    private static final String USER = "madang";
    private static final String PASSWORD = "madang";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }
    }

    // try-with-resources 사용 권장
}
```

#### 데이터베이스 스키마 (수정 없음)

**Book 테이블**
```sql
CREATE TABLE Book (
    bookid INT PRIMARY KEY AUTO_INCREMENT,
    bookname VARCHAR(40) NOT NULL,
    publisher VARCHAR(40) NOT NULL,
    price INT NOT NULL DEFAULT 0,
    INDEX idx_bookname (bookname),
    INDEX idx_publisher (publisher)
);
```

**Customer 테이블**
```sql
CREATE TABLE Customer (
    custid INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(40),
    address VARCHAR(50),
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'customer',
    INDEX idx_name (name),
    INDEX idx_role (role)
);
```

**Orders 테이블**
```sql
CREATE TABLE Orders (
    orderid INT PRIMARY KEY AUTO_INCREMENT,
    custid INT NOT NULL,
    bookid INT NOT NULL,
    saleprice INT NOT NULL DEFAULT 0,
    orderdate DATE NOT NULL DEFAULT (CURDATE()),
    FOREIGN KEY (custid) REFERENCES Customer(custid),
    FOREIGN KEY (bookid) REFERENCES Book(bookid),
    INDEX idx_custid (custid),
    INDEX idx_bookid (bookid),
    INDEX idx_orderdate (orderdate)
);
```

---

## 5. 사용자 역할 및 기능

### 5.1 일반 고객 (Customer)

**인증 방식**:
- Customer 테이블에서 고객 선택 (간단한 로그인)
- 세션에 custid 저장 (localStorage)

**기능**:
- 도서 조회 및 검색
- 도서 구매 (주문 생성)
- 내 주문 내역 조회
- 주문 취소

**사용 페이지**: 전체 페이지 (대시보드 제외)

### 5.2 관리자/CEO/개발자

**인증 방식**:
- 별도 인증 없음 (읽기 전용)
- 또는 간단한 비밀번호 보호

**기능**:
- 전체 통계 조회
- 고객별/출판사별/도서별 분석
- 베스트셀러 조회
- 매출 현황 조회

**사용 페이지**: dashboard.html

### 5.3 출판사 (Publisher)

**제한사항**:
- 별도 테이블이 없음
- Book.publisher 컬럼으로만 존재

**기능**:
- 출판사별 통계 조회 (대시보드에서)
- 출판사별 도서 매출 확인

---

## 6. API 설계 원칙

### RESTful API 가이드

#### HTTP 메서드
- **GET**: 조회 (SELECT)
- **POST**: 생성 (INSERT)
- **DELETE**: 삭제 (DELETE)
- ~~PUT/PATCH~~: 사용 안 함 (UPDATE 미구현)

#### 응답 형식 (JSON)

**성공 응답**:
```json
{
  "success": true,
  "data": [...],
  "message": "성공"
}
```

**실패 응답**:
```json
{
  "success": false,
  "error": "에러 메시지",
  "code": 400
}
```

#### 에러 처리
- 400: Bad Request (잘못된 파라미터)
- 404: Not Found (리소스 없음)
- 500: Internal Server Error (DB 연결 실패 등)

---

## 7. CRUD SQL 학습 매핑

### CREATE (INSERT)
- **주문 생성**: `OrderHandler.handlePost()` → `OrderDAO.createOrder()`
```sql
INSERT INTO Orders (custid, bookid, saleprice, orderdate)
VALUES (?, ?, ?, CURDATE());
```

### READ (SELECT)
- **도서 목록**: `BookHandler.handleGet()` → `BookDAO.getAllBooks()`
```sql
SELECT bookid, bookname, publisher, price FROM Book ORDER BY bookid;
```

- **도서 검색**: `BookDAO.searchBooks(keyword)`
```sql
SELECT * FROM Book WHERE bookname LIKE CONCAT('%', ?, '%');
```

- **주문 내역 (JOIN)**: `OrderDAO.getOrdersByCustomer(custid)`
```sql
SELECT o.orderid, o.saleprice, o.orderdate, b.bookname, b.publisher
FROM Orders o
JOIN Book b ON o.bookid = b.bookid
WHERE o.custid = ?
ORDER BY o.orderdate DESC;
```

- **통계 (GROUP BY)**: `OrderDAO.getBestsellers()`
```sql
SELECT b.bookname, COUNT(*) as 판매수
FROM Orders o
JOIN Book b ON o.bookid = b.bookid
GROUP BY b.bookid, b.bookname
ORDER BY 판매수 DESC
LIMIT ?;
```

### UPDATE
- **미구현** (학생들이 아직 배우지 않음)

### DELETE
- **주문 취소**: `OrderHandler.handleDelete()` → `OrderDAO.deleteOrder()`
```sql
DELETE FROM Orders WHERE orderid = ? AND custid = ?;
```

---

## 8. 보안 고려사항

### 교육용 프로젝트 수준

1. **SQL Injection 방지**: PreparedStatement 사용
2. **XSS 방지**: JavaScript에서 innerHTML 대신 textContent 사용
3. **세션 관리**: localStorage에 custid 저장 (간단한 방식)
4. **CORS**: 동일 출처 또는 허용된 출처만

### 실무 수준으로 개선 시

- 비밀번호 해싱 (BCrypt)
- JWT 토큰 인증
- HTTPS 적용
- CSRF 토큰
- 입력 유효성 검증 강화

---

## 9. 개발 및 배포 환경

### 개발 환경
- **IDE**: VS Code / IntelliJ IDEA / Eclipse
- **JDK**: 21+
- **Server**: Java built-in HttpServer (포트 8080)
- **MySQL**: 5.7+ (localhost:3306)
- **브라우저**: Chrome, Firefox (최신 버전)

### 실행 방법
```bash
# 컴파일
javac -d bin -cp "lib/*" $(find src -name "*.java")

# 실행
java -cp "bin:lib/*" com.madang.server.MadangServer
```

### 배포 환경
- **서버**: Linux/Windows Server
- **Java Runtime**: JDK 21+
- **MySQL**: 외부 접속 허용 설정
- **포트**: 8080 (방화벽 오픈 필요)

### MCP MySQL 사용 (개발 시)
- SQL 쿼리 테스트
- 데이터 검증
- 스키마 확인
- **프로덕션에는 포함 안 됨**

---

## 10. 프로젝트 구조

```
madang/
├── docs/
│   ├── architecture.md              # 이 문서
│   ├── page-design.md               # 페이지별 상세 디자인
│   ├── simple-server-guide.md       # 간단한 서버 가이드
│   └── mcp-mysql-setup.md           # MCP MySQL 설정 가이드
│
├── sql/
│   └── queries.sql                  # CRUD SQL 학습용 쿼리 모음
│
├── gen/
│   ├── gen_book_mysql.py            # 도서 테스트 데이터 생성
│   ├── gen_cust_mysql.py            # 고객 테스트 데이터 생성
│   └── gen_orders_mysql.py          # 주문 테스트 데이터 생성
│
├── frontend/
│   ├── index.html
│   ├── customer-login.html
│   ├── books.html
│   ├── book-detail.html
│   ├── order.html
│   ├── my-orders.html
│   ├── dashboard.html
│   ├── css/
│   │   └── styles.css               # Modern CSS
│   └── js/
│       ├── api.js
│       ├── books.js
│       ├── orders.js
│       ├── customers.js
│       ├── login.js
│       └── dashboard.js
│
├── src/
│   └── com/
│       └── madang/
│           ├── server/
│           │   ├── MadangServer.java   # HTTP 서버 메인
│           │   └── ApiHandler.java     # Handler 추상 클래스
│           ├── handler/
│           │   ├── BookHandler.java
│           │   ├── CustomerHandler.java
│           │   ├── OrderHandler.java
│           │   └── StatsHandler.java
│           ├── dao/
│           │   ├── BookDAO.java
│           │   ├── CustomerDAO.java
│           │   └── OrderDAO.java
│           ├── model/
│           │   ├── Book.java
│           │   ├── Customer.java
│           │   └── Order.java
│           └── util/
│               ├── DBConnection.java
│               └── SqlLogger.java      # SQL 로깅 (선택)
│
├── lib/
│   ├── mysql-connector-j-9.1.0.jar
│   └── gson-2.11.0.jar              # JSON 처리
│
├── bin/                             # 컴파일된 .class 파일
│
├── CLAUDE.md                        # Claude Code 가이드
└── README.md                        # 프로젝트 실행 가이드
```

---

## 11. 향후 확장 가능성

### Phase 1 (현재)
- ✅ CRUD SQL 학습
- ✅ Java JDBC 기초
- ✅ Java HTTP Server 이해
- ✅ REST API 구현

### Phase 2 (UPDATE 추가)
- 고객 정보 수정
- 도서 정보 수정 (관리자)
- 주문 정보 수정

### Phase 3 (고급 기능)
- 회원가입/로그인 (인증)
- 장바구니 기능
- 리뷰 시스템
- 페이징 처리
- 파일 업로드 (도서 이미지)

### Phase 4 (기술 스택 업그레이드)
- Spring Boot 전환
- MyBatis/JPA 도입
- React/Vue.js 프론트엔드
- REST API 개선

---

---

## 12. Java Built-in HTTP Server vs Tomcat

### 교육용 선택 이유

본 프로젝트는 **Java 21의 내장 HTTP Server**를 사용합니다:

**장점**:
- 별도 서버 설치 불필요 (Tomcat, Jetty 등)
- 단일 명령으로 컴파일 및 실행
- 가벼운 학습 곡선
- JDBC와 SQL 학습에 집중 가능

**단점**:
- 프로덕션 환경에 부적합
- Servlet 표준 미지원
- 성능 및 확장성 제한

**Tomcat과의 차이**:
| 항목 | Java HttpServer | Apache Tomcat |
|------|----------------|---------------|
| 설치 | 불필요 | 필요 |
| 설정 | 코드 기반 | web.xml |
| 배포 | JAR/직접 실행 | WAR 파일 |
| 성능 | 교육용 | 프로덕션급 |
| 표준 | Custom | Servlet API |

---

*최종 업데이트: 2025-10-21*
