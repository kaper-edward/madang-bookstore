# 마당 온라인 서점 (Madang Bookstore)

교육용 온라인 서점 시스템 - CRUD SQL 학습을 위한 실전 프로젝트

**버전**: 2.0.0-servlet
**기반**: Jakarta EE 11, Servlet 6.1
**서버**: Apache Tomcat 11.0+
**전환 완료일**: 2025-10-22

---

## 📚 프로젝트 소개

학생들이 **CRUD SQL 연산**을 배우면서 실제로 동작하는 온라인 서점 웹 애플리케이션을 구축하는 교육용 프로젝트입니다.

### 주요 특징

- ✅ Jakarta EE 11 표준 준수 (Servlet 6.1)
- ✅ Apache Tomcat 11.0+ 기반
- ✅ JDBC를 통한 직접적인 데이터베이스 연동
- ✅ RESTful API 구조
- ✅ Vanilla JavaScript 프론트엔드
- ✅ MySQL 데이터베이스 (3개 테이블)
- ✅ Maven 표준 프로젝트 구조

### 버전 히스토리

- **v2.0.0-servlet** (2025-10-22): Jakarta EE Servlet 기반으로 전환
- **v1.0.0** (2025-10-15): Java HttpServer 기반 초기 버전

---

## 🛠️ 기술 스택

### Backend
- **Java 17+** - Jakarta EE 11 요구사항 (Java 21 권장)
- **Jakarta EE 11** - 최신 Enterprise Java 표준
- **Servlet 6.1** - Jakarta Servlet API
- **Apache Tomcat 11.0.13** - Servlet Container
- **JDBC** - MySQL Connector/J 8.0.33
- **JSON** - Gson 2.10.1
- **Maven** - 빌드 및 의존성 관리

### Frontend
- **HTML5** - 시맨틱 마크업
- **CSS3** - Utility-first CSS (Tailwind 스타일)
- **JavaScript** - Vanilla JS (ES6+)

### Database
- **MySQL 5.7+**
- 데이터베이스: `madangdb`
- 테이블: Book, Customer, Orders

---

## 🌟 Jakarta EE 11 & Servlet 6.1

### Jakarta EE 11이란?

**Jakarta EE**(구 Java EE)는 엔터프라이즈급 Java 애플리케이션을 위한 표준 플랫폼입니다. Jakarta EE 11은 2024년에 릴리스된 최신 버전으로, 다음과 같은 특징이 있습니다:

- **Java 17 이상 필수** - 최신 Java 언어 기능 활용
- **현대적인 웹 애플리케이션 개발** - 클라우드 네이티브 환경 지원
- **표준화된 API** - 벤더 독립적인 개발 가능
- **보안 강화** - SecurityManager 제거 등 보안 개선

### Jakarta Servlet 6.1이란?

**Jakarta Servlet**은 Java 기반 웹 애플리케이션의 핵심 API입니다. Servlet 6.1은 Jakarta EE 11의 일부로, 다음과 같은 개선사항을 포함합니다:

**주요 기능** (2024년 6월 릴리스):
- ✨ **리다이렉트 제어 강화** - 상태 코드 및 응답 본문 제어 가능
- ✨ **향상된 에러 디스패칭** - 쿼리 문자열 속성 지원
- ✨ **새로운 HTTP 상태 코드 상수** - 최신 HTTP 표준 반영
- ✨ **Charset 지원 메서드** - 문자 인코딩 처리 개선
- ✨ **ByteBuffer 지원** - 입출력 스트림에서 ByteBuffer 사용
- 🔒 **보안 강화** - SecurityManager 참조 제거

**스펙 문서**: https://jakarta.ee/specifications/servlet/6.1/

### 호환 Servlet Container

- **Apache Tomcat 11.0.x** ✅ (이 프로젝트 사용)
- **Eclipse GlassFish 8.0+**
- **WildFly 33+**

---

## 🚀 빠른 시작

### 사전 요구사항

#### 공통 요구사항

1. **Java 17 이상** 설치 (Java 21 권장)
   ```bash
   # 버전 확인
   java -version  # 17 이상 확인
   ```

2. **MySQL 5.7 이상** 설치 및 실행

3. **Git** 설치

#### Java 설치 가이드

**Linux (Ubuntu/Debian)**
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

**macOS (Homebrew)**
```bash
brew install openjdk@21
```

**Windows**
1. [Adoptium (Eclipse Temurin)](https://adoptium.net/) 또는 [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) 다운로드
2. 설치 후 환경 변수 설정:
   - `JAVA_HOME`: JDK 설치 경로 (예: `C:\Program Files\Java\jdk-21`)
   - `Path`에 `%JAVA_HOME%\bin` 추가
3. 명령 프롬프트에서 확인:
   ```cmd
   java -version
   javac -version
   ```

---

## 📦 설치 및 실행 가이드

### 1단계: 프로젝트 클론

```bash
git clone https://github.com/YOUR_USERNAME/madang-bookstore.git
cd madang-bookstore
```

### 2단계: 데이터베이스 설정

#### MySQL 설정 (공통)

```sql
-- MySQL 콘솔 접속
mysql -u root -p

-- 데이터베이스 및 사용자 생성
CREATE DATABASE madangdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'madang'@'localhost' IDENTIFIED BY 'madang';
GRANT ALL PRIVILEGES ON madangdb.* TO 'madang'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### 스키마 및 샘플 데이터 로드

**Linux/macOS:**
```bash
mysql -u madang -p madangdb < sql/schema.sql
mysql -u madang -p madangdb < sql/sample_data.sql
```

**Windows (PowerShell):**
```powershell
Get-Content sql\schema.sql | mysql -u madang -p madangdb
Get-Content sql\sample_data.sql | mysql -u madang -p madangdb
```

**Windows (명령 프롬프트):**
```cmd
mysql -u madang -p madangdb < sql\schema.sql
mysql -u madang -p madangdb < sql\sample_data.sql
```

### 3단계: Tomcat 설치

#### Option A: Linux/macOS

```bash
# 1. Tomcat 11.0.13 다운로드
cd ~
wget https://dlcdn.apache.org/tomcat/tomcat-11/v11.0.13/bin/apache-tomcat-11.0.13.tar.gz

# 2. 압축 해제
tar xzf apache-tomcat-11.0.13.tar.gz

# 3. 적절한 위치로 이동
sudo mv apache-tomcat-11.0.13 /opt/tomcat
# 또는 홈 디렉토리에 유지
mv apache-tomcat-11.0.13 ~/tomcat11

# 4. 환경 변수 설정 (~/.bashrc 또는 ~/.zshrc에 추가)
export CATALINA_HOME=~/tomcat11  # 또는 /opt/tomcat
export PATH=$CATALINA_HOME/bin:$PATH

# 5. 적용
source ~/.bashrc  # 또는 source ~/.zshrc

# 6. 실행 권한 부여
chmod +x $CATALINA_HOME/bin/*.sh
```

#### Option B: Windows

1. **다운로드**
   - [Tomcat 11.0.13 다운로드 페이지](https://tomcat.apache.org/download-11.cgi)에서 **Windows Service Installer (64-bit)** 또는 **zip** 파일 다운로드

2. **설치 (Installer 사용 시)**
   - 다운로드한 `.exe` 파일 실행
   - 설치 경로 지정 (예: `C:\Program Files\Apache Software Foundation\Tomcat 11.0`)
   - "Tomcat" 서비스 자동 시작 옵션 선택
   - 설치 완료

3. **설치 (ZIP 사용 시)**
   - ZIP 파일을 원하는 위치에 압축 해제 (예: `C:\tomcat11`)
   - 환경 변수 설정 (선택사항):
     - `CATALINA_HOME`: Tomcat 설치 경로
     - `Path`에 `%CATALINA_HOME%\bin` 추가

4. **Tomcat 실행**
   - **서비스 방식**: Windows 서비스 관리자에서 "Apache Tomcat" 시작
   - **수동 방식**: `C:\tomcat11\bin\startup.bat` 실행

5. **Tomcat 확인**
   - 브라우저에서 `http://localhost:8080` 접속
   - Tomcat 기본 페이지가 보이면 성공

### 4단계: WAR 파일 빌드

#### Option A: Maven 사용 (권장)

**Linux/macOS:**
```bash
# Maven 설치
# Ubuntu/Debian:
sudo apt install maven

# macOS (Homebrew):
brew install maven

# 빌드
mvn clean package

# 결과: target/madang.war 생성
```

**Windows:**
```powershell
# Maven 설치 확인
mvn -version

# Maven이 없으면 https://maven.apache.org/download.cgi 에서 다운로드 및 설치

# 빌드 (PowerShell 또는 명령 프롬프트)
mvn clean package

# 결과: target\madang.war 생성
```

#### Option B: 수동 빌드 스크립트

**Linux/macOS:**
```bash
chmod +x build-manual.sh
./build-manual.sh
```

**Windows (수동 빌드):**

Windows에서는 다음 단계를 수동으로 실행:

```cmd
REM 1. 빌드 디렉토리 생성
mkdir target\classes
mkdir target\madang\WEB-INF\classes
mkdir target\madang\WEB-INF\lib

REM 2. Jakarta Servlet API 다운로드 (필요 시)
REM https://repo1.maven.org/maven2/jakarta/servlet/jakarta.servlet-api/6.1.0/jakarta.servlet-api-6.1.0.jar
REM 다운로드 후 target\ 에 저장

REM 3. Gson 다운로드 (필요 시)
REM https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
REM 다운로드 후 target\ 에 저장

REM 4. Java 소스 컴파일
dir /s /b src\main\java\*.java > sources.txt
javac -d target\classes -cp "target\jakarta.servlet-api-6.1.0.jar;target\gson-2.10.1.jar;lib\*" -encoding UTF-8 -source 17 -target 17 @sources.txt

REM 5. 클래스 파일 복사
xcopy target\classes\* target\madang\WEB-INF\classes\ /E /I

REM 6. 라이브러리 복사
copy lib\*.jar target\madang\WEB-INF\lib\
copy target\gson-2.10.1.jar target\madang\WEB-INF\lib\

REM 7. webapp 파일 복사
xcopy src\main\webapp\* target\madang\ /E /I

REM 8. WAR 파일 생성
cd target\madang
jar cvf ..\madang.war *
cd ..\..
```

**권장**: Windows 사용자는 Maven을 사용하는 것이 훨씬 간편합니다.

### 5단계: Tomcat에 배포

#### Linux/macOS

```bash
# 1. 기존 배포 제거 (있다면)
rm -rf $CATALINA_HOME/webapps/madang
rm -f $CATALINA_HOME/webapps/madang.war

# 2. 새 WAR 파일 복사
cp target/madang.war $CATALINA_HOME/webapps/

# 3. Tomcat 시작
$CATALINA_HOME/bin/startup.sh

# 4. 로그 확인
tail -f $CATALINA_HOME/logs/catalina.out
```

#### Windows

**서비스 방식:**
```cmd
REM 1. 기존 배포 제거 (있다면)
del /Q "%CATALINA_HOME%\webapps\madang.war"
rmdir /S /Q "%CATALINA_HOME%\webapps\madang"

REM 2. 새 WAR 파일 복사
copy target\madang.war "%CATALINA_HOME%\webapps\"

REM 3. Windows 서비스 관리자에서 Tomcat 재시작
REM 또는 명령어:
net stop Tomcat11
net start Tomcat11
```

**수동 방식:**
```cmd
REM 1. 기존 배포 제거
del /Q "C:\tomcat11\webapps\madang.war"
rmdir /S /Q "C:\tomcat11\webapps\madang"

REM 2. WAR 파일 복사
copy target\madang.war "C:\tomcat11\webapps\"

REM 3. Tomcat 시작
C:\tomcat11\bin\startup.bat

REM 4. 로그 확인
type "C:\tomcat11\logs\catalina.out"
```

### 6단계: 접속 및 테스트

#### 브라우저 접속

- **메인 페이지**: http://localhost:8080/madang/
- **도서 목록**: http://localhost:8080/madang/books.html
- **고객 로그인**: http://localhost:8080/madang/customer-login.html
- **관리자 페이지**: http://localhost:8080/madang/admin/

#### API 테스트

**Linux/macOS (curl):**
```bash
# 서버 상태 확인
curl http://localhost:8080/madang/

# 도서 목록 조회
curl "http://localhost:8080/madang/api/books?action=list"

# 고객 목록 조회
curl "http://localhost:8080/madang/api/customers?action=list"

# 통계 조회
curl "http://localhost:8080/madang/api/stats?action=overview"
```

**Windows (PowerShell):**
```powershell
# 서버 상태 확인
Invoke-WebRequest -Uri "http://localhost:8080/madang/" | Select-Object -ExpandProperty Content

# 도서 목록 조회
Invoke-RestMethod -Uri "http://localhost:8080/madang/api/books?action=list"

# 고객 목록 조회
Invoke-RestMethod -Uri "http://localhost:8080/madang/api/customers?action=list"

# 통계 조회
Invoke-RestMethod -Uri "http://localhost:8080/madang/api/stats?action=overview"
```

#### ⚠️ 중요: JavaScript API 경로 설정

Tomcat에 배포 시 WAR 파일명이 context path가 되므로, JavaScript에서 API 호출 시 반드시 context path를 포함해야 합니다.

**파일**: `src/main/webapp/js/api.js`
```javascript
// ✅ 올바른 설정 (Tomcat 배포용)
const API_BASE_URL = '/madang';

// ❌ 잘못된 설정 (404 에러 발생)
const API_BASE_URL = '';
```

**주의사항**:
- WAR 파일명이 `madang.war`이면 context path는 `/madang`
- WAR 파일명을 변경하면 `API_BASE_URL`도 함께 변경 필요
- 브라우저에서 "데이터를 불러오지 못했습니다" 에러가 발생하면 개발자 도구(F12)로 API 요청 경로 확인

---

## 📂 프로젝트 구조

```
madang/
├── pom.xml                          # Maven 설정
├── build-manual.sh                  # 수동 빌드 스크립트 (Linux/macOS)
├── src/
│   └── main/
│       ├── java/com/madang/
│       │   ├── servlet/             # Servlet 클래스
│       │   │   ├── ApiServlet.java  # 기본 서블릿
│       │   │   ├── BookServlet.java
│       │   │   ├── CustomerServlet.java
│       │   │   ├── OrderServlet.java
│       │   │   └── StatsServlet.java
│       │   ├── dao/                 # Data Access Objects
│       │   │   ├── BookDAO.java
│       │   │   ├── CustomerDAO.java
│       │   │   └── OrderDAO.java
│       │   ├── model/               # Models (POJOs)
│       │   │   ├── Book.java
│       │   │   ├── Customer.java
│       │   │   ├── Order.java
│       │   │   ├── PageRequest.java
│       │   │   └── PageResponse.java
│       │   └── util/                # 유틸리티
│       │       ├── DBConnection.java
│       │       ├── SessionManager.java
│       │       ├── ConfigManager.java
│       │       └── SqlLogger.java
│       ├── webapp/                  # 웹 리소스
│       │   ├── WEB-INF/
│       │   │   └── web.xml          # 서블릿 설정
│       │   ├── index.html           # 메인 페이지
│       │   ├── books.html
│       │   ├── customer-login.html
│       │   ├── book-detail.html
│       │   ├── order.html
│       │   ├── my-orders.html
│       │   ├── admin/               # 관리자 페이지
│       │   │   ├── index.html
│       │   │   ├── books-admin.html
│       │   │   └── customers-admin.html
│       │   ├── css/styles.css
│       │   └── js/
│       │       ├── api.js
│       │       ├── books.js
│       │       ├── orders.js
│       │       ├── customers.js
│       │       ├── login.js
│       │       └── dashboard.js
│       └── resources/
│           └── application.properties
├── docs/                            # 문서
│   ├── servlet-migration-plan.md
│   ├── tomcat-deployment-guide.md
│   ├── backend-architecture-review.md
│   └── architecture.md
├── sql/
│   ├── queries.sql                  # SQL 학습용 쿼리 모음
│   ├── schema.sql                   # 스키마
│   └── sample_data.sql              # 샘플 데이터
├── gen/                             # 테스트 데이터 생성기
├── lib/                             # 외부 라이브러리
│   └── mysql-connector-j-8.0.33.jar
├── target/                          # 빌드 결과
│   └── madang.war
├── CLAUDE.md                        # Claude Code 가이드
├── README.md                        # 이 파일
└── SERVLET-README.md                # Servlet 버전 상세 가이드
```

---

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
- 📈 월별 매출 추이
- 🎯 고객 세그먼트 분석

---

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
    role VARCHAR(20) NOT NULL DEFAULT 'customer',
    CONSTRAINT chk_customer_role CHECK (role IN ('customer', 'publisher', 'admin', 'manager'))
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

---

## 🔌 API 엔드포인트

모든 API 엔드포인트는 `/api/*` 경로로 제공됩니다.

### Books API
```
GET /api/books?action=list              # 전체 도서 목록
GET /api/books?action=detail&id=1       # 도서 상세
GET /api/books?action=search&keyword=축구 # 도서 검색
GET /api/books?action=publishers        # 출판사 목록
POST /api/books?action=create           # 도서 등록 (관리자)
PUT /api/books?action=update            # 도서 수정 (관리자)
DELETE /api/books?action=delete&id=1    # 도서 삭제 (관리자)
```

### Customers API
```
GET /api/customers?action=list          # 전체 고객 목록
GET /api/customers?action=detail&id=1   # 고객 상세
POST /api/customers?action=login        # 로그인
POST /api/customers?action=create       # 고객 등록 (관리자)
PUT /api/customers?action=update        # 고객 정보 수정
DELETE /api/customers?action=delete&id=1 # 고객 삭제 (관리자)
```

### Orders API
```
GET /api/orders?action=list&custid=1    # 고객 주문 내역
GET /api/orders?action=stats&custid=1   # 고객 주문 통계
POST /api/orders?action=create          # 주문 생성
PUT /api/orders?action=update           # 주문 가격 수정
DELETE /api/orders?action=delete&id=1&custid=1  # 주문 취소
```

### Stats API
```
GET /api/stats?action=overview               # 전체 통계
GET /api/stats?action=bestsellers&limit=5    # 베스트셀러
GET /api/stats?action=weekly-bestsellers     # 주간 베스트셀러
GET /api/stats?action=monthly&months=12      # 월별 매출
GET /api/stats?action=customers              # 고객별 통계
GET /api/stats?action=publishers             # 출판사별 통계
GET /api/stats?action=books                  # 도서별 통계
GET /api/stats?action=customer-segments      # 고객 세그먼트
```

---

## 📖 학습 포인트

이 프로젝트를 통해 다음을 학습할 수 있습니다:

### SQL
- ✅ **CREATE (INSERT)**: 주문 생성, 도서 등록
- ✅ **READ (SELECT)**: 도서/고객/주문 조회
- ✅ **UPDATE**: 도서 정보 수정, 고객 정보 수정
- ✅ **DELETE**: 주문 취소, 도서 삭제
- ✅ **JOIN**: 다중 테이블 조인 (Orders-Customer-Book)
- ✅ **GROUP BY**: 집계 및 통계
- ✅ **Aggregate Functions**: COUNT, SUM, AVG, MAX, MIN
- ✅ **Subquery**: 서브쿼리 활용
- ✅ **Pagination**: LIMIT, OFFSET

### Jakarta EE & Servlet
- ✅ Servlet 6.1 API 사용
- ✅ 어노테이션 기반 라우팅 (`@WebServlet`)
- ✅ HTTP 요청/응답 처리
- ✅ 세션 관리
- ✅ 필터 및 리스너
- ✅ WAR 패키징 및 배포

### Java/JDBC
- ✅ JDBC 연결 관리 (HikariCP Connection Pool)
- ✅ PreparedStatement (SQL Injection 방지)
- ✅ ResultSet 처리
- ✅ DAO 패턴
- ✅ 트랜잭션 관리

### Web Development
- ✅ RESTful API 설계
- ✅ JSON 응답 처리
- ✅ CORS 처리
- ✅ Fetch API
- ✅ DOM 조작
- ✅ 반응형 디자인

---

## 🐛 문제 해결

### 포트 8080이 이미 사용 중인 경우

**Linux/macOS:**
```bash
# 포트 사용 프로세스 확인
lsof -i :8080

# 프로세스 종료
kill -9 <PID>

# 또는 Tomcat 포트 변경
vi $CATALINA_HOME/conf/server.xml
# <Connector port="8080" → port="8081"
```

**Windows:**
```cmd
REM 포트 사용 프로세스 확인
netstat -ano | findstr :8080

REM 프로세스 종료
taskkill /PID <PID> /F

REM 또는 Tomcat 포트 변경
notepad "%CATALINA_HOME%\conf\server.xml"
REM <Connector port="8080" → port="8081"
```

### 데이터베이스 연결 실패

1. **MySQL 서비스 확인**

   **Linux:**
   ```bash
   sudo systemctl status mysql
   sudo systemctl start mysql
   ```

   **macOS:**
   ```bash
   brew services list
   brew services start mysql
   ```

   **Windows:**
   ```cmd
   net start MySQL80
   ```

2. **연결 정보 확인**

   `src/main/resources/application.properties` 또는 `src/main/java/com/madang/util/DBConnection.java`의 연결 정보 확인

### 브라우저에서 데이터가 로드되지 않는 경우

**증상**: 브라우저에서 "도서 데이터를 불러오지 못했습니다" 또는 "데이터를 불러오는 중 오류가 발생했습니다" 메시지 표시

**원인**: JavaScript API 경로 설정이 Tomcat context path와 일치하지 않음

**해결 방법**:

1. **브라우저 개발자 도구로 확인** (F12 또는 Ctrl+Shift+I):
   ```
   Network 탭에서 404 에러 확인:
   ❌ http://localhost:8080/api/books → 404 Not Found
   ✅ http://localhost:8080/madang/api/books → 200 OK
   ```

2. **API_BASE_URL 수정**:

   **파일**: `src/main/webapp/js/api.js`
   ```javascript
   // 수정 전 (잘못된 설정)
   const API_BASE_URL = '';

   // 수정 후 (올바른 설정)
   const API_BASE_URL = '/madang';  // WAR 파일명과 일치
   ```

3. **재빌드 및 재배포**:
   ```bash
   # Maven 빌드
   mvn clean package

   # 기존 배포 제거
   rm -rf $CATALINA_HOME/webapps/madang*

   # 새 WAR 배포
   cp target/madang.war $CATALINA_HOME/webapps/

   # Tomcat 재시작 (또는 자동 배포 대기)
   ```

4. **브라우저 캐시 삭제**: Ctrl+Shift+R 또는 Cmd+Shift+R로 강력 새로고침

### 컴파일 오류

Java 17 이상 사용 여부 확인:
```bash
java -version
javac -version
```

### WAR 배포 실패

**배포 로그 확인:**

**Linux/macOS:**
```bash
tail -f $CATALINA_HOME/logs/catalina.out
```

**Windows:**
```cmd
type "%CATALINA_HOME%\logs\catalina.out"
```

### MySQL Connector JAR 누락

**오류**: `Cannot load JDBC driver`

**해결**:
```bash
# WAR 파일에 MySQL Connector 포함 확인
unzip -l target/madang.war | grep mysql

# 없다면 lib/ 디렉토리에 추가 후 재빌드
```

---

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

---

## 📚 추가 문서

- **Servlet 전환 가이드**: `SERVLET-README.md`
- **Tomcat 배포 가이드**: `docs/tomcat-deployment-guide.md`
- **아키텍처 문서**: `docs/architecture.md`
- **백엔드 아키텍처 리뷰**: `docs/backend-architecture-review.md`
- **SQL 쿼리 예제**: `sql/queries.sql`

---

## 📝 라이선스

MIT License - 교육 목적으로 자유롭게 사용 가능합니다.

---

## 👥 기여

교육용 프로젝트이므로 Pull Request 환영합니다!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📧 문의

프로젝트 관련 문의: [GitHub Issues](https://github.com/YOUR_USERNAME/madang-bookstore/issues)

---

**최종 업데이트**: 2025-10-22
**버전**: 2.0.0-servlet
**Jakarta EE**: 11
**Servlet**: 6.1
**Tomcat**: 11.0.13
