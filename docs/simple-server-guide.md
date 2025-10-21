# Java 21 내장 HTTP 서버 가이드

## 개요

Tomcat 없이 **Java 21의 내장 HTTP 서버**만으로 마당 서점을 실행할 수 있습니다!

## 장점

### ✅ Tomcat 대비 장점

1. **간단함**: Tomcat 설치/설정 불필요
2. **빠른 시작**: 한 줄 명령어로 즉시 실행
3. **학습 용이**: 복잡한 서버 설정 없이 Java 코드에만 집중
4. **이식성**: JDK 21만 있으면 어디서든 실행 가능

### ⚠️ 제약사항

- **프로덕션 용도 부적합**: 교육/개발용으로만 사용
- **성능 제한**: 동시 접속 처리 능력이 Tomcat보다 낮음
- **기능 제한**: WAR 배포, JSP 등 Servlet 고급 기능 미지원

## 아키텍처

```
┌─────────────────────────────────────────┐
│        Browser (프론트엔드)              │
│  index.html, books.html, css, js        │
└────────────────┬────────────────────────┘
                 │ HTTP (localhost:8080)
                 ▼
┌─────────────────────────────────────────┐
│     MadangServer (Java 21 HttpServer)   │
│  ┌───────────────────────────────────┐  │
│  │  StaticFileHandler                │  │
│  │  (HTML, CSS, JS 서빙)             │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │  API Handlers                     │  │
│  │  - BookHandler                    │  │
│  │  - CustomerHandler                │  │
│  │  - OrderHandler                   │  │
│  │  - StatsHandler                   │  │
│  └───────────────────────────────────┘  │
└────────────────┬────────────────────────┘
                 │ JDBC
                 ▼
┌─────────────────────────────────────────┐
│          MySQL Database                 │
│            madangdb                     │
└─────────────────────────────────────────┘
```

## 핵심 클래스

### 1. MadangServer.java

메인 서버 클래스 - HTTP 서버 생성 및 라우팅 설정

```java
package com.madang.server;

public class MadangServer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        // HTTP 서버 생성
        HttpServer server = HttpServer.create(
            new InetSocketAddress(PORT), 0
        );

        // API 라우트 등록
        server.createContext("/api/books", new BookHandler());
        server.createContext("/api/customers", new CustomerHandler());

        // 정적 파일 서빙
        server.createContext("/", new StaticFileHandler("frontend"));

        // 서버 시작
        server.start();
    }
}
```

**주요 기능**:
- HTTP 서버 생성 (포트 8080)
- API 엔드포인트 등록
- 정적 파일 서빙 (HTML, CSS, JS)
- 멀티스레드 처리 (스레드 풀 10개)

### 2. StaticFileHandler.java (내장)

정적 파일 서빙 핸들러

```java
class StaticFileHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        // 1. 요청 경로 파싱
        String path = exchange.getRequestURI().getPath();

        // 2. 파일 읽기
        File file = new File("frontend" + path);

        // 3. Content-Type 설정
        String contentType = getContentType(file);

        // 4. 파일 전송
        exchange.sendResponseHeaders(200, file.length());
        // ... 파일 전송 로직
    }
}
```

**주요 기능**:
- HTML, CSS, JS 파일 자동 서빙
- Content-Type 자동 설정
- 404 에러 처리
- 보안: 디렉토리 밖 파일 접근 차단

### 3. ApiHandler.java (추상 클래스)

API 핸들러 기본 클래스 - 모든 API는 이를 상속

```java
public abstract class ApiHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        // 1. CORS 헤더 설정
        setCorsHeaders(exchange);

        // 2. HTTP 메서드별 처리
        switch (method) {
            case "GET": response = handleGet(params); break;
            case "POST": response = handlePost(params, body); break;
            case "DELETE": response = handleDelete(params); break;
        }

        // 3. JSON 응답 전송
        sendJsonResponse(exchange, 200, response);
    }

    // 하위 클래스에서 구현
    protected abstract String handleGet(Map<String, String> params);
    protected abstract String handlePost(Map<String, String> params, String body);
    protected abstract String handleDelete(Map<String, String> params);
}
```

**주요 기능**:
- HTTP 메서드 자동 라우팅 (GET, POST, PUT, DELETE)
- 쿼리 파라미터 파싱
- 요청 본문 읽기
- JSON 응답 생성
- CORS 처리
- 에러 처리

## 학생이 구현할 부분

### 1. BookHandler.java 예시

```java
package com.madang.handler;

import com.madang.server.ApiHandler;
import com.madang.dao.BookDAO;
import java.util.*;

public class BookHandler extends ApiHandler {

    private BookDAO bookDAO = new BookDAO();

    @Override
    protected String handleGet(Map<String, String> params) throws Exception {
        String action = params.get("action");

        if ("list".equals(action)) {
            // 전체 도서 조회
            List<Book> books = bookDAO.getAllBooks();
            return successResponse(toJson(books));

        } else if ("detail".equals(action)) {
            // 도서 상세 조회
            int bookId = Integer.parseInt(params.get("id"));
            Book book = bookDAO.getBookById(bookId);
            return successResponse(toJson(book));

        } else if ("search".equals(action)) {
            // 도서 검색
            String keyword = params.get("keyword");
            List<Book> books = bookDAO.searchBooks(keyword);
            return successResponse(toJson(books));
        }

        return errorResponse("잘못된 action 파라미터입니다.");
    }
}
```

### 2. MadangServer.java에 핸들러 등록

```java
// registerApiRoutes() 메서드 내부
server.createContext("/api/books", new BookHandler());
server.createContext("/api/customers", new CustomerHandler());
server.createContext("/api/orders", new OrderHandler());
server.createContext("/api/stats", new StatsHandler());
```

## 실행 방법

### Linux/Mac

```bash
# 1단계: 권한 부여 (최초 1회만)
chmod +x run-server.sh

# 2단계: 서버 실행
./run-server.sh
```

### Windows

```cmd
run-server.bat
```

### 출력 예시

```
╔════════════════════════════════════════════╗
║   마당 온라인 서점 서버 컴파일 및 실행     ║
╚════════════════════════════════════════════╝

📦 Java 파일 컴파일 중...
✅ 컴파일 성공!

🚀 서버 시작 중...

╔════════════════════════════════════════════╗
║   마당 온라인 서점 서버 시작됨! 🚀         ║
╚════════════════════════════════════════════╝

📍 서버 주소: http://localhost:8080
📁 프론트엔드: /path/to/madang_claude/frontend

🌐 브라우저에서 접속하세요:
   http://localhost:8080/index.html

⚠️  종료하려면 Ctrl+C를 누르세요
```

## 디렉토리 구조

```
madang_claude/
├── src/
│   └── com/
│       └── madang/
│           ├── server/
│           │   ├── MadangServer.java       # 메인 서버
│           │   └── ApiHandler.java         # API 기본 클래스
│           ├── handler/                    # API 핸들러 (학생 구현)
│           │   ├── BookHandler.java
│           │   ├── CustomerHandler.java
│           │   ├── OrderHandler.java
│           │   └── StatsHandler.java
│           ├── dao/                        # DAO (학생 구현)
│           │   ├── BookDAO.java
│           │   ├── CustomerDAO.java
│           │   └── OrderDAO.java
│           ├── model/                      # 모델 (학생 구현)
│           │   ├── Book.java
│           │   ├── Customer.java
│           │   └── Order.java
│           └── util/
│               └── DBConnection.java       # JDBC 연결
├── frontend/
│   ├── index.html
│   ├── books.html
│   ├── css/
│   │   └── styles.css
│   └── js/
│       ├── api.js
│       └── ...
├── bin/                                    # 컴파일된 .class 파일
├── run-server.sh                           # 실행 스크립트 (Linux/Mac)
└── run-server.bat                          # 실행 스크립트 (Windows)
```

## API 엔드포인트 예시

### 1. 도서 목록 조회

**요청**:
```
GET http://localhost:8080/api/books?action=list
```

**응답**:
```json
{
  "success": true,
  "data": [
    {
      "bookid": 1,
      "bookname": "축구의 역사",
      "publisher": "굿스포츠",
      "price": 7000
    },
    ...
  ]
}
```

### 2. 주문 생성

**요청**:
```
POST http://localhost:8080/api/orders?action=create
Content-Type: application/json

{
  "custid": 1,
  "bookid": 1,
  "saleprice": 6000
}
```

**응답**:
```json
{
  "success": true,
  "data": {
    "orderid": 11
  }
}
```

## 문제 해결

### 포트 8080이 이미 사용 중인 경우

**MadangServer.java 수정**:
```java
private static final int PORT = 8081; // 다른 포트로 변경
```

**frontend/js/api.js 수정**:
```javascript
const API_BASE_URL = 'http://localhost:8081'; // 동일한 포트로 변경
```

### 컴파일 오류

```bash
# Java 버전 확인
java -version
# 21 이상이어야 함

# 소스 파일 경로 확인
ls -R src/
```

### 파일을 찾을 수 없음 (404)

```bash
# frontend 폴더가 있는지 확인
ls frontend/

# 실행 경로 확인 (프로젝트 루트에서 실행해야 함)
pwd
# /path/to/madang_claude 여야 함
```

## Tomcat과의 비교

| 항목 | Java HttpServer | Tomcat |
|------|----------------|--------|
| 설치 | 불필요 (JDK 내장) | 별도 설치 필요 |
| 설정 | Java 코드로만 | web.xml 등 설정 파일 |
| 시작 시간 | 매우 빠름 (1초 이내) | 느림 (5~10초) |
| 학습 곡선 | 낮음 | 높음 |
| 성능 | 낮음 | 높음 |
| 프로덕션 | 부적합 | 적합 |
| JSP 지원 | 없음 | 있음 |
| WAR 배포 | 없음 | 있음 |

## 결론

**교육용 프로젝트에는 Java 21 내장 HTTP 서버가 최적!**

- ✅ 간단한 설정
- ✅ 빠른 시작
- ✅ CRUD SQL 학습에 집중
- ✅ 어디서든 실행 가능

**프로덕션에는 Tomcat 사용 권장**

---

*마당 온라인 서점 - Java 21 Simple HTTP Server*
