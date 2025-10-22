# Servlet 전환 구현 계획서

**작성일**: 2025-10-22
**대상**: 개발팀
**목적**: HttpServer 기반 → Servlet 기반 전환 상세 구현 계획

---

## 1. 개요

### 1.1 전환 결정 사항

개발팀 검토 결과, **시나리오 B: Servlet으로 전환**을 결정하였습니다.

**전환 목표**:
- 표준 Jakarta EE Servlet 기술 스택 적용
- Tomcat 컨테이너 기반 배포
- 기존 비즈니스 로직 100% 재사용
- Frontend 코드 변경 없이 API 호환성 유지

### 1.2 전환 범위

#### ✅ 변경 대상

| 구성 요소 | 현재 | 변경 후 |
|---------|------|--------|
| **서버 메인 클래스** | `MadangServer.java` (HttpServer 생성) | 제거 (Tomcat이 대체) |
| **핸들러 기본 클래스** | `ApiHandler extends HttpHandler` | `ApiServlet extends HttpServlet` |
| **요청/응답 객체** | `HttpExchange` | `HttpServletRequest/Response` |
| **라우팅** | `server.createContext("/api/books", ...)` | `@WebServlet("/api/books")` |
| **정적 파일 서빙** | `StaticFileHandler` 클래스 | Tomcat 기본 서블릿 |
| **배포 방식** | `java -cp` 직접 실행 | WAR 파일 → Tomcat 배포 |
| **프로젝트 구조** | flat 구조 | Maven 표준 구조 |

#### ✅ 유지 대상 (변경 없음)

- `dao/` - BookDAO, CustomerDAO, OrderDAO
- `model/` - Book, Customer, Order, PageRequest, PageResponse
- `util/` - DBConnection, SessionManager, ConfigManager, SqlLogger
- `frontend/` - 모든 HTML, CSS, JavaScript
- `sql/` - 모든 SQL 쿼리
- **비즈니스 로직** - Handler 내부의 모든 처리 로직

---

## 2. 프로젝트 구조 변경

### 2.1 현재 구조

```
madang/
├── src/com/madang/
│   ├── server/
│   │   ├── MadangServer.java
│   │   └── ApiHandler.java
│   ├── handler/
│   │   ├── BookHandler.java
│   │   ├── CustomerHandler.java
│   │   ├── OrderHandler.java
│   │   └── StatsHandler.java
│   ├── dao/
│   ├── model/
│   └── util/
├── frontend/
│   ├── index.html
│   ├── css/
│   └── js/
├── lib/
│   ├── mysql-connector-java-*.jar
│   └── HikariCP-*.jar
└── bin/
```

### 2.2 변경 후 구조 (Maven 표준)

```
madang/
├── pom.xml                          # Maven 설정
├── src/
│   └── main/
│       ├── java/
│       │   └── com/madang/
│       │       ├── servlet/
│       │       │   ├── ApiServlet.java       # 새로 작성
│       │       │   ├── BookServlet.java
│       │       │   ├── CustomerServlet.java
│       │       │   ├── OrderServlet.java
│       │       │   └── StatsServlet.java
│       │       ├── dao/                      # 변경 없음
│       │       ├── model/                    # 변경 없음
│       │       └── util/                     # 변경 없음
│       ├── webapp/
│       │   ├── WEB-INF/
│       │   │   └── web.xml                   # 서블릿 설정
│       │   ├── index.html
│       │   ├── books.html
│       │   ├── css/
│       │   └── js/
│       └── resources/
│           └── application.properties        # 설정 파일
└── target/
    └── madang.war                            # 빌드 결과
```

### 2.3 디렉토리 마이그레이션

| 현재 위치 | 변경 후 위치 |
|---------|------------|
| `src/com/madang/dao/` | `src/main/java/com/madang/dao/` |
| `src/com/madang/model/` | `src/main/java/com/madang/model/` |
| `src/com/madang/util/` | `src/main/java/com/madang/util/` |
| `frontend/*` | `src/main/webapp/*` |
| `config.properties` | `src/main/resources/application.properties` |

---

## 3. 코드 변환 상세

### 3.1 ApiHandler → ApiServlet 변환

#### 현재 (ApiHandler.java)

```java
public abstract class ApiHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        String method = exchange.getRequestMethod();
        Map<String, String> params = parseQueryParams(...);
        String requestBody = readRequestBody(exchange);

        String response;
        switch (method) {
            case "GET": response = handleGet(params); break;
            case "POST": response = handlePost(params, requestBody); break;
            case "PUT": response = handlePut(params, requestBody); break;
            case "DELETE": response = handleDelete(params); break;
        }

        sendJsonResponse(exchange, 200, response);
    }

    protected abstract String handleGet(Map<String, String> params);
    protected abstract String handlePost(Map<String, String> params, String body);
}
```

#### 변경 후 (ApiServlet.java)

```java
public abstract class ApiServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // CORS 헤더 설정
        setCorsHeaders(resp);

        // OPTIONS 요청 처리
        if ("OPTIONS".equals(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        try {
            String method = req.getMethod();
            Map<String, String> params = parseQueryParams(req);
            String requestBody = readRequestBody(req);

            String response;
            switch (method) {
                case "GET": response = handleGet(params); break;
                case "POST": response = handlePost(params, requestBody); break;
                case "PUT": response = handlePut(params, requestBody); break;
                case "DELETE": response = handleDelete(params); break;
                default: response = errorResponse("지원하지 않는 HTTP 메서드");
            }

            sendJsonResponse(resp, HttpServletResponse.SC_OK, response);

        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                errorResponse(e.getMessage()));
        }
    }

    protected abstract String handleGet(Map<String, String> params) throws Exception;
    protected abstract String handlePost(Map<String, String> params, String body) throws Exception;
    protected abstract String handlePut(Map<String, String> params, String body) throws Exception;
    protected abstract String handleDelete(Map<String, String> params) throws Exception;

    // 유틸리티 메서드들 (대부분 동일, 시그니처만 변경)
    protected Map<String, String> parseQueryParams(HttpServletRequest req) { ... }
    protected String readRequestBody(HttpServletRequest req) { ... }
    protected void sendJsonResponse(HttpServletResponse resp, int statusCode, String json) { ... }
    protected void setCorsHeaders(HttpServletResponse resp) { ... }
    protected String successResponse(String data) { ... }
    protected String errorResponse(String message) { ... }
}
```

#### 주요 변경 사항

| 항목 | 변경 전 | 변경 후 |
|------|---------|--------|
| **상속 클래스** | `HttpHandler` | `HttpServlet` |
| **핸들러 메서드** | `handle(HttpExchange)` | `service(HttpServletRequest, HttpServletResponse)` |
| **요청 객체** | `HttpExchange` | `HttpServletRequest` |
| **응답 객체** | `HttpExchange` | `HttpServletResponse` |
| **헤더 설정** | `exchange.getResponseHeaders().set()` | `resp.setHeader()` |
| **상태 코드** | `exchange.sendResponseHeaders(200, ...)` | `resp.setStatus(HttpServletResponse.SC_OK)` |
| **응답 쓰기** | `exchange.getResponseBody().write()` | `resp.getWriter().write()` |

### 3.2 Handler → Servlet 변환 예시

#### BookHandler → BookServlet

**변경 전 (BookHandler.java)**:
```java
public class BookHandler extends ApiHandler {
    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected String handleGet(Map<String, String> params) throws Exception {
        String action = params.getOrDefault("action", "list");

        switch (action) {
            case "detail":
                int bookId = Integer.parseInt(params.get("id"));
                Book book = bookDAO.getBookById(bookId);
                return successResponse(book.toJson());
            // ... 기타 로직
        }
    }
}
```

**변경 후 (BookServlet.java)**:
```java
@WebServlet("/api/books")
public class BookServlet extends ApiServlet {
    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected String handleGet(Map<String, String> params) throws Exception {
        String action = params.getOrDefault("action", "list");

        switch (action) {
            case "detail":
                int bookId = Integer.parseInt(params.get("id"));
                Book book = bookDAO.getBookById(bookId);
                return successResponse(book.toJson());
            // ... 기타 로직 (100% 동일)
        }
    }
}
```

**핵심**: `@WebServlet` 어노테이션 추가, 클래스명 변경, **나머지 로직은 100% 동일**

### 3.3 세션 관리 변환

#### 현재 (ApiHandler.java)

```java
protected String getSessionId() {
    Headers headers = currentExchange.getRequestHeaders();
    List<String> sessionHeaders = headers.get("X-Session-Id");
    if (sessionHeaders != null && !sessionHeaders.isEmpty()) {
        return sessionHeaders.get(0);
    }
    return null;
}

protected Session getSession() {
    String sessionId = getSessionId();
    return SessionManager.getSession(sessionId);
}
```

#### 변경 후 (ApiServlet.java)

```java
protected String getSessionId(HttpServletRequest req) {
    return req.getHeader("X-Session-Id");
}

protected Session getSession(HttpServletRequest req) {
    String sessionId = getSessionId(req);
    return SessionManager.getSession(sessionId);
}
```

**SessionManager 유틸리티는 그대로 유지**

---

## 4. 설정 파일

### 4.1 pom.xml (Maven 설정)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.madang</groupId>
    <artifactId>madang-bookstore</artifactId>
    <version>2.0.0-servlet</version>
    <packaging>war</packaging>

    <name>Madang Bookstore - Servlet Edition</name>
    <description>Educational online bookstore with Servlet</description>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Jakarta Servlet API -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
            <scope>provided</scope>
        </dependency>

        <!-- MySQL Connector -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.0.33</version>
        </dependency>

        <!-- HikariCP -->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>5.0.1</version>
        </dependency>

        <!-- Gson (JSON 처리) -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>
    </dependencies>

    <build>
        <finalName>madang</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.2</version>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 4.2 web.xml (배포 설명자)

**Option 1: 어노테이션 방식 (권장)**

`web.xml` 없이 `@WebServlet` 어노테이션만 사용:

```java
@WebServlet("/api/books")
public class BookServlet extends ApiServlet { ... }
```

**Option 2: web.xml 방식**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
         https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <display-name>Madang Bookstore</display-name>

    <!-- 기본 페이지 설정 -->
    <welcome-file-list>
        <welcome-file>index.html</welcome-file>
    </welcome-file-list>

    <!-- 세션 타임아웃 (120분) -->
    <session-config>
        <session-timeout>120</session-timeout>
    </session-config>

    <!-- 서블릿 선언 (어노테이션 사용 시 불필요) -->
    <!--
    <servlet>
        <servlet-name>BookServlet</servlet-name>
        <servlet-class>com.madang.servlet.BookServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>BookServlet</servlet-name>
        <url-pattern>/api/books</url-pattern>
    </servlet-mapping>
    -->
</web-app>
```

**권장**: 어노테이션 방식 사용, web.xml은 최소 설정만 유지

---

## 5. 빌드 및 배포

### 5.1 Maven 빌드

```bash
# 1. 프로젝트 루트에서 빌드
mvn clean package

# 결과: target/madang.war 생성

# 2. Tomcat에 배포 (방법 1: 수동 복사)
cp target/madang.war $TOMCAT_HOME/webapps/

# 3. Tomcat에 배포 (방법 2: Maven 플러그인)
mvn tomcat7:deploy

# 4. Tomcat 시작
$TOMCAT_HOME/bin/startup.sh

# 5. 접속 테스트
curl http://localhost:8080/madang/api/health
```

### 5.2 Tomcat 설치 (Ubuntu/Debian)

```bash
# 1. Tomcat 10.1 다운로드
wget https://dlcdn.apache.org/tomcat/tomcat-10/v10.1.28/bin/apache-tomcat-10.1.28.tar.gz

# 2. 압축 해제
tar xzf apache-tomcat-10.1.28.tar.gz
sudo mv apache-tomcat-10.1.28 /opt/tomcat

# 3. 환경 변수 설정
export CATALINA_HOME=/opt/tomcat
echo 'export CATALINA_HOME=/opt/tomcat' >> ~/.bashrc

# 4. 실행 권한 부여
chmod +x $CATALINA_HOME/bin/*.sh

# 5. Tomcat 시작
$CATALINA_HOME/bin/startup.sh

# 6. 로그 확인
tail -f $CATALINA_HOME/logs/catalina.out
```

### 5.3 개발 환경 실행 (Hot Reload)

```bash
# Maven Tomcat 플러그인 사용
mvn tomcat7:run

# 또는 cargo 플러그인
mvn cargo:run
```

---

## 6. 전환 체크리스트

### Phase 1: 프로젝트 구조 변경 (4시간)

- [ ] `pom.xml` 생성
- [ ] Maven 디렉토리 구조 생성 (`src/main/java`, `src/main/webapp`)
- [ ] 기존 DAO, Model, Util 코드 이동
- [ ] Frontend 파일들 `src/main/webapp/` 이동
- [ ] `web.xml` 작성 (최소 설정)
- [ ] Maven 빌드 테스트 (`mvn clean compile`)

### Phase 2: 코드 마이그레이션 (8시간)

- [ ] `ApiServlet.java` 작성 (ApiHandler 변환)
  - [ ] `service()` 메서드 구현
  - [ ] `parseQueryParams()` 변환
  - [ ] `readRequestBody()` 변환
  - [ ] `sendJsonResponse()` 변환
  - [ ] `setCorsHeaders()` 변환
  - [ ] 세션 관리 메서드 변환
  - [ ] 권한 검증 메서드 변환
- [ ] `BookServlet.java` 작성 (BookHandler 변환)
  - [ ] `@WebServlet("/api/books")` 추가
  - [ ] 비즈니스 로직 복사 (100% 동일)
- [ ] `CustomerServlet.java` 작성 (CustomerHandler 변환)
- [ ] `OrderServlet.java` 작성 (OrderHandler 변환)
- [ ] `StatsServlet.java` 작성 (StatsHandler 변환)

### Phase 3: 테스트 및 검증 (4시간)

- [ ] WAR 파일 빌드 (`mvn clean package`)
- [ ] Tomcat 배포
- [ ] API 엔드포인트 테스트
  - [ ] `/api/health` - Health Check (서버 및 DB 상태 확인)
  - [ ] `/api/books?action=list` - 도서 목록
  - [ ] `/api/customers?action=list` - 고객 목록
  - [ ] `/api/orders?action=list` - 주문 목록
  - [ ] `/api/stats` - 통계
- [ ] Frontend 통합 테스트
  - [ ] `index.html` 접속
  - [ ] `books.html` 도서 목록 표시
  - [ ] `customer-login.html` 로그인
  - [ ] `my-orders.html` 주문 내역
- [ ] 세션 관리 테스트
- [ ] 권한 검증 테스트
- [ ] 성능 테스트 (부하 테스트)

---

## 7. 리스크 관리

### 7.1 예상 리스크

| 리스크 | 확률 | 영향 | 대응 방안 |
|-------|------|------|----------|
| Maven 빌드 실패 | 중 | 중 | 단계별 빌드 확인, 의존성 검증 |
| Tomcat 설정 오류 | 중 | 중 | 공식 문서 참조, 로그 분석 |
| API 호환성 문제 | 낮 | 높 | 단위 테스트 작성, API 계약 검증 |
| 세션 관리 버그 | 중 | 중 | SessionManager 유틸 재사용 |
| Frontend 연동 실패 | 낮 | 높 | API Base URL 확인, CORS 검증 |

### 7.2 롤백 전략

- 기존 HttpServer 코드는 별도 브랜치에 보존
- Git 커밋 단계별로 진행
- 문제 발생 시 이전 커밋으로 롤백

```bash
# 백업 브랜치 생성
git checkout -b httpserver-backup

# 전환 작업용 브랜치
git checkout -b servlet-migration

# 롤백 (필요 시)
git checkout httpserver-backup
```

---

## 8. 예상 공수

| 단계 | 예상 시간 | 담당자 |
|------|----------|--------|
| **Phase 1: 프로젝트 구조 변경** | 4시간 | 백엔드 개발자 |
| **Phase 2: 코드 마이그레이션** | 8시간 | 백엔드 개발자 |
| **Phase 3: 테스트 및 검증** | 4시간 | 전체 팀 |
| **문서화** | 2시간 | 백엔드 개발자 |
| **예비 시간 (버퍼)** | 2시간 | - |
| **총 합계** | **20시간 (2.5일)** | - |

---

## 9. 참고 자료

### 9.1 공식 문서

- [Jakarta Servlet Specification](https://jakarta.ee/specifications/servlet/)
- [Apache Tomcat 10.1 Documentation](https://tomcat.apache.org/tomcat-10.1-doc/)
- [Maven WAR Plugin](https://maven.apache.org/plugins/maven-war-plugin/)

### 9.2 내부 문서

- `docs/backend-architecture-review.md` - 아키텍처 검토 문서
- `docs/architecture.md` - 현재 아키텍처 상세
- `CLAUDE.md` - 프로젝트 개요

---

## 10. 승인 및 일정

**검토자**: 개발팀 전체
**승인일**: 2025-10-22
**시작일**: 2025-10-22
**완료 예정일**: 2025-10-24

---

**작성자**: 개발팀
**최종 수정일**: 2025-10-22
