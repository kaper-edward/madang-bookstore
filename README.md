# 마당 온라인 서점 (Madang Bookstore)

교육용 온라인 서점 시스템 - CRUD SQL 학습을 위한 실전 프로젝트

## 📚 프로젝트 소개

학생들이 **CRUD SQL 연산**을 배우면서 실제로 동작하는 온라인 서점 웹 애플리케이션을 구축하는 교육용 프로젝트입니다.

### 주요 특징

- ✅ Java 21 내장 HTTP 서버 사용 (Tomcat 불필요)
- ✅ JDBC를 통한 직접적인 데이터베이스 연동
- ✅ RESTful API 구조
- ✅ Vanilla JavaScript 프론트엔드
- ✅ MySQL 데이터베이스 (3개 테이블)

## 🛠️ 기술 스택

### Backend
- **Java 21+** - JDK 21 이상 필수
- **HTTP Server** - `com.sun.net.httpserver.HttpServer` (내장)
- **JDBC** - MySQL Connector/J 9.1.0
- **JSON** - Gson 2.11.0

### Frontend
- **HTML5** - 시맨틱 마크업
- **CSS3** - Utility-first CSS (Tailwind 스타일)
- **JavaScript** - Vanilla JS (ES6+)

### Database
- **MySQL 5.7+**
- 데이터베이스: `madangdb`
- 테이블: Book, Customer, Orders

## 🚀 빠른 시작

### 사전 요구사항

1. **Java 21 이상** 설치
   ```bash
   java -version  # 21 이상 확인
   ```

2. **MySQL 설치 및 실행**
   ```bash
   # Ubuntu/Debian
   sudo systemctl start mysql

   # macOS (Homebrew)
   brew services start mysql
   ```

3. **데이터베이스 설정**
   ```bash
   mysql -u root -p

   # MySQL 콘솔에서:
   CREATE DATABASE madangdb;
   CREATE USER 'madang'@'localhost' IDENTIFIED BY 'madang';
   GRANT ALL PRIVILEGES ON madangdb.* TO 'madang'@'localhost';
   FLUSH PRIVILEGES;
   ```

### 설치 및 실행

1. **저장소 클론**
   ```bash
   git clone https://github.com/YOUR_USERNAME/madang-bookstore.git
   cd madang-bookstore
   ```

2. **데이터베이스 스키마 및 데이터 생성**
   ```bash
   mysql -u madang -p madangdb < sql/schema.sql    # 스키마 생성
   mysql -u madang -p madangdb < sql/sample_data.sql  # 샘플 데이터
   ```

3. **컴파일**
   ```bash
   javac -d bin -cp "lib/*" $(find src -name "*.java")
   ```

4. **서버 실행**
   ```bash
   java -cp "bin:lib/*" com.madang.server.MadangServer
   ```

5. **브라우저에서 접속**
   ```
   http://localhost:8080/index.html
   ```

## 📂 프로젝트 구조

```
madang-bookstore/
├── docs/                       # 문서
│   ├── architecture.md         # 아키텍처 설명
│   ├── page-design.md          # 페이지별 디자인
│   ├── simple-server-guide.md  # 서버 가이드
│   └── mcp-mysql-setup.md      # MCP MySQL 설정
│
├── sql/
│   └── queries.sql             # SQL 학습용 쿼리 모음
│
├── gen/                        # 테스트 데이터 생성기 (Python)
│   ├── gen_book_mysql.py
│   ├── gen_cust_mysql.py
│   └── gen_orders_mysql.py
│
├── frontend/                   # 프론트엔드
│   ├── *.html                  # 7개 HTML 페이지
│   ├── css/styles.css
│   └── js/
│       ├── api.js              # API 호출 유틸리티
│       ├── books.js
│       ├── orders.js
│       ├── customers.js
│       ├── login.js
│       └── dashboard.js
│
├── src/com/madang/             # 백엔드 Java 소스
│   ├── server/
│   │   ├── MadangServer.java  # 메인 서버
│   │   └── ApiHandler.java    # Handler 추상 클래스
│   ├── handler/               # API Handlers
│   │   ├── BookHandler.java
│   │   ├── CustomerHandler.java
│   │   ├── OrderHandler.java
│   │   └── StatsHandler.java
│   ├── dao/                   # Data Access Objects
│   │   ├── BookDAO.java
│   │   ├── CustomerDAO.java
│   │   └── OrderDAO.java
│   ├── model/                 # Models (POJOs)
│   │   ├── Book.java
│   │   ├── Customer.java
│   │   └── Order.java
│   └── util/
│       ├── DBConnection.java  # JDBC 연결
│       └── SqlLogger.java     # SQL 로깅
│
├── lib/                       # 외부 라이브러리
│   ├── mysql-connector-j-9.1.0.jar
│   └── gson-2.11.0.jar
│
├── bin/                       # 컴파일된 .class 파일
├── CLAUDE.md                  # Claude Code 가이드
└── README.md                  # 이 파일
```

## 🎯 주요 기능

### 고객 기능
- 📖 도서 목록 조회 및 검색
- 🔍 출판사별 필터링
- 🛒 도서 구매 (주문 생성)
- 📋 내 주문 내역 조회
- ❌ 주문 취소

### 관리자 기능 (대시보드)
- 📊 전체 매출 통계
- 🏆 베스트셀러 분석
- 👥 고객별 구매 통계
- 📚 출판사별 매출 통계

## 🗄️ 데이터베이스 스키마

### Book 테이블
```sql
CREATE TABLE Book (
    bookid INT PRIMARY KEY AUTO_INCREMENT,
    bookname VARCHAR(40) NOT NULL,
    publisher VARCHAR(40) NOT NULL,
    price INT NOT NULL DEFAULT 0
);
```

### Customer 테이블
```sql
CREATE TABLE Customer (
    custid INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(40),
    address VARCHAR(50),
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'customer'
);
```

### Orders 테이블
```sql
CREATE TABLE Orders (
    orderid INT PRIMARY KEY AUTO_INCREMENT,
    custid INT NOT NULL,
    bookid INT NOT NULL,
    saleprice INT NOT NULL DEFAULT 0,
    orderdate DATE NOT NULL DEFAULT (CURDATE()),
    FOREIGN KEY (custid) REFERENCES Customer(custid),
    FOREIGN KEY (bookid) REFERENCES Book(bookid)
);
```

## 🔌 API 엔드포인트

### Books API
```
GET /api/books?action=list              # 전체 도서 목록
GET /api/books?action=detail&id=1       # 도서 상세
GET /api/books?action=search&keyword=축구 # 도서 검색
GET /api/books?action=publishers        # 출판사 목록
```

### Customers API
```
GET /api/customers?action=list          # 전체 고객 목록
GET /api/customers?action=detail&id=1   # 고객 상세
```

### Orders API
```
GET /api/orders?action=list&custid=1    # 고객 주문 내역
POST /api/orders?action=create          # 주문 생성
DELETE /api/orders?action=delete&id=1&custid=1  # 주문 취소
```

### Stats API
```
GET /api/stats?action=overview          # 전체 통계
GET /api/stats?action=bestsellers       # 베스트셀러
GET /api/stats?action=customers         # 고객별 통계
GET /api/stats?action=publishers        # 출판사별 통계
```

## 📖 학습 포인트

이 프로젝트를 통해 다음을 학습할 수 있습니다:

### SQL
- ✅ **CREATE (INSERT)**: 주문 생성
- ✅ **READ (SELECT)**: 도서/고객/주문 조회
- ✅ **DELETE**: 주문 취소
- ✅ **JOIN**: 다중 테이블 조인
- ✅ **GROUP BY**: 집계 및 통계
- ✅ **Aggregate Functions**: COUNT, SUM, AVG, MAX, MIN
- ✅ **Subquery**: 서브쿼리 활용

### Java/JDBC
- ✅ JDBC 연결 관리
- ✅ PreparedStatement (SQL Injection 방지)
- ✅ ResultSet 처리
- ✅ DAO 패턴
- ✅ Java HTTP Server

### Web Development
- ✅ RESTful API 설계
- ✅ JSON 응답 처리
- ✅ CORS 처리
- ✅ Fetch API
- ✅ DOM 조작

## 🔧 개발 도구

### MCP MySQL Server (선택사항)

개발 시 Claude Code와 함께 사용할 수 있는 MCP MySQL 서버:

```bash
# 저장소 클론 (NPM 버그 우회)
git clone https://github.com/benborla/mcp-server-mysql.git
cd mcp-server-mysql
npm install && npm run build && npm link

# Claude Code에 추가
claude mcp add mcp_server_mysql \
  -e MYSQL_HOST="localhost" \
  -e MYSQL_USER="madang" \
  -e MYSQL_PASS="madang" \
  -e MYSQL_DB="madangdb" \
  -e ALLOW_INSERT_OPERATION="true" \
  -e ALLOW_DELETE_OPERATION="true" \
  -- node $(which mcp-server-mysql)
```

자세한 내용은 `docs/mcp-mysql-setup.md` 참조

## 🐛 문제 해결

### 포트 8080이 이미 사용 중인 경우

1. `src/com/madang/server/MadangServer.java`에서 포트 변경:
   ```java
   private static final int PORT = 8081;
   ```

2. `frontend/js/api.js`에서 API URL 변경:
   ```javascript
   const API_BASE_URL = 'http://localhost:8081/api';
   ```

### 데이터베이스 연결 실패

1. MySQL 서비스 확인:
   ```bash
   sudo systemctl status mysql
   ```

2. `src/com/madang/util/DBConnection.java`의 연결 정보 확인:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/madangdb";
   private static final String USER = "madang";
   private static final String PASSWORD = "madang";
   ```

### 컴파일 오류

Java 21 이상 사용 여부 확인:
```bash
java -version
javac -version
```

## 📝 라이선스

교육 목적으로 자유롭게 사용 가능합니다.

## 👥 기여

교육용 프로젝트이므로 Pull Request 환영합니다!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📧 문의

프로젝트 관련 문의: [GitHub Issues](https://github.com/YOUR_USERNAME/madang-bookstore/issues)

---

**최종 업데이트**: 2025-10-21
