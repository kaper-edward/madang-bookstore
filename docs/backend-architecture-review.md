# 마당 온라인 서점 백엔드 아키텍처 검토 문서

**작성일**: 2025-10-22
**대상**: 개발팀 전체 (개발자, 디자이너, 기획자, 업무 담당자)
**목적**: HttpServer, Servlet, JSP 기술 비교 및 아키텍처 전환 가능성 검토

---

## 📋 목차

1. [문서 개요](#1-문서-개요)
2. [현재 시스템 구조](#2-현재-시스템-구조)
3. [핵심 질문 답변](#3-핵심-질문-답변)
4. [기술 비교 분석](#4-기술-비교-분석)
5. [전환 시나리오](#5-전환-시나리오)
6. [권장사항](#6-권장사항)

---

## 1. 문서 개요

### 1.1 배경 및 목적

마당 온라인 서점은 현재 **Java 21의 내장 HttpServer**를 사용하여 구현되어 있습니다. 이는 Tomcat과 같은 별도의 서블릿 컨테이너 없이도 웹 서버를 실행할 수 있는 경량 방식입니다.

이 문서는 다음 질문들에 대한 답을 제공하여 개발팀의 의사결정을 돕기 위해 작성되었습니다:

1. **Servlet 전환 가능성**: 현재 구조를 Tomcat 기반 Servlet으로 전환할 수 있는가?
2. **HttpServer vs Servlet**: 두 구현 방식의 차이점은 무엇인가?
3. **JSP와의 차이**: JSP는 무엇이며, 현재 구조와 어떻게 다른가?
4. **Frontend 영향도**: 백엔드 변경 시 Frontend 코드도 수정이 필요한가?

### 1.2 대상 독자

- **개발자**: 기술적 세부사항 및 구현 방법
- **디자이너**: Frontend에 미치는 영향
- **기획자**: 비즈니스 영향 및 의사결정 기준
- **업무 담당자**: 각 옵션의 장단점 및 비용

---

## 2. 현재 시스템 구조

### 2.1 전체 아키텍처

마당 서점은 **3계층 아키텍처(Three-Tier Architecture)**를 따릅니다:

```
┌─────────────────────────────────────┐
│   Frontend (프레젠테이션 계층)       │
│   - HTML, CSS, JavaScript           │
│   - 사용자 인터페이스               │
└──────────────┬──────────────────────┘
               │ HTTP REST API
               │ (JSON 통신)
┌──────────────▼──────────────────────┐
│   Backend (비즈니스 로직 계층)       │
│   - Java 21 HttpServer              │
│   - Handler (Controller 역할)        │
│   - DAO (Data Access Object)        │
└──────────────┬──────────────────────┘
               │ JDBC
               │
┌──────────────▼──────────────────────┐
│   Database (데이터 계층)             │
│   - MySQL 5.7+                      │
│   - Book, Customer, Orders 테이블   │
└─────────────────────────────────────┘
```

### 2.2 현재 Backend 패키지 구조

```
src/com/madang/
├── server/
│   ├── MadangServer.java      # HTTP 서버 메인 클래스
│   └── ApiHandler.java        # 추상 Handler 기본 클래스
├── handler/
│   ├── BookHandler.java       # 도서 API 핸들러
│   ├── CustomerHandler.java   # 고객 API 핸들러
│   ├── OrderHandler.java      # 주문 API 핸들러
│   └── StatsHandler.java      # 통계 API 핸들러
├── dao/
│   ├── BookDAO.java           # Book 테이블 접근
│   ├── CustomerDAO.java       # Customer 테이블 접근
│   └── OrderDAO.java          # Orders 테이블 접근
├── model/
│   ├── Book.java              # 도서 모델
│   ├── Customer.java          # 고객 모델
│   ├── Order.java             # 주문 모델
│   ├── PageRequest.java       # 페이지네이션 요청
│   └── PageResponse.java      # 페이지네이션 응답
└── util/
    ├── DBConnection.java      # DB 연결 관리 (HikariCP)
    ├── SessionManager.java    # 세션 관리
    ├── ConfigManager.java     # 설정 파일 관리
    └── SqlLogger.java         # SQL 로깅
```

### 2.3 현재 구현의 핵심 특징

#### ✅ 장점

1. **간단한 설정**: Tomcat 설치 불필요, Java만 있으면 실행 가능
2. **교육 목적 적합**: 복잡한 설정 없이 웹 서버 동작 원리 학습
3. **빠른 시작**: 컴파일 후 즉시 실행 가능 (`java -cp` 명령어 하나)
4. **경량**: 메모리 사용량 적음, 빠른 재시작
5. **명확한 구조**: Handler 패턴으로 코드 이해 쉬움

#### ⚠️ 제약사항

1. **프로덕션 환경 부적합**: 엔터프라이즈 기능 부족
2. **표준 기술 아님**: Servlet/JSP 표준에서 벗어남
3. **생태계 제한**: Spring Boot 등 프레임워크 사용 불가
4. **고급 기능 제한**: 클러스터링, 로드 밸런싱 등 어려움

---

## 3. 핵심 질문 답변

### 3.1 Q1: Servlet으로 전환 가능한가?

**답변: 네, 가능합니다. ✅**

현재 구조는 Servlet으로 전환이 충분히 가능합니다. 실제로 **대부분의 코드는 그대로 유지**되며, 일부 계층만 변경하면 됩니다.

#### 변경이 필요한 부분

| 구분 | 현재 (HttpServer) | 변경 후 (Servlet) |
|------|------------------|-------------------|
| **서버 클래스** | `MadangServer.java` | `web.xml` 또는 `@WebServlet` 어노테이션 |
| **Handler 기본 클래스** | `ApiHandler extends HttpHandler` | `ApiServlet extends HttpServlet` |
| **요청/응답 처리** | `HttpExchange` 사용 | `HttpServletRequest/Response` 사용 |
| **라우팅 방식** | `server.createContext("/api/books", ...)` | `@WebServlet("/api/books")` 또는 web.xml |
| **실행 방법** | `java -cp bin:lib/* MadangServer` | Tomcat에 WAR 배포 |

#### 그대로 유지되는 부분 (변경 없음)

- ✅ **DAO 계층 전체** (BookDAO, CustomerDAO, OrderDAO)
- ✅ **Model 계층 전체** (Book, Customer, Order 등)
- ✅ **Utility 클래스** (DBConnection, SessionManager, ConfigManager)
- ✅ **비즈니스 로직** (Handler 내부의 처리 로직)
- ✅ **SQL 쿼리** (모든 데이터베이스 접근 코드)
- ✅ **Frontend 전체** (HTML, CSS, JavaScript)

#### 전환 작업 난이도

- **난이도**: 중간 (개발자 기준)
- **예상 공수**: 2~3일 (1인 개발자 기준)
- **리스크**: 낮음 (구조가 이미 계층화되어 있음)

---

### 3.2 Q2: HttpServer vs Servlet 차이점

#### 쉽게 이해하기

**비유**: HttpServer는 자동차를 직접 조립하여 운전하는 것이고, Servlet은 완성된 자동차(Tomcat)에 탑승하여 운전하는 것입니다.

#### 상세 비교

| 항목 | Java HttpServer | Servlet (Tomcat) |
|------|-----------------|------------------|
| **개념** | Java 표준 라이브러리의 간단한 HTTP 서버 | Java EE 표준 웹 애플리케이션 기술 |
| **실행 환경** | JDK만 있으면 실행 가능 | Tomcat, Jetty 등 서블릿 컨테이너 필요 |
| **설정 복잡도** | 매우 간단 (코드만 작성) | 중간 (web.xml 또는 어노테이션 설정) |
| **학습 난이도** | 낮음 | 중간 |
| **표준화** | 비표준 (JDK 내장 API) | 표준 (Jakarta EE Servlet Specification) |
| **프로덕션 사용** | ❌ 권장하지 않음 | ✅ 권장함 |
| **성능** | 기본적인 수준 | 최적화 및 튜닝 가능 |
| **확장성** | 제한적 | 우수 (세션 클러스터링, 로드 밸런싱 등) |
| **보안 기능** | 직접 구현 필요 | 다양한 보안 기능 제공 |
| **개발 생산성** | 단순 프로젝트에 적합 | 대규모 프로젝트에 적합 |
| **프레임워크 연동** | 제한적 | Spring Boot 등 연동 용이 |

#### 기술적 차이점 상세

##### 1) 요청 처리 방식

**HttpServer 방식**:
```java
public class BookHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // 요청 처리
        String method = exchange.getRequestMethod();
        String query = exchange.getRequestURI().getQuery();

        // 응답 전송
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
    }
}
```

**Servlet 방식**:
```java
@WebServlet("/api/books")
public class BookServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        // 요청 처리
        String action = request.getParameter("action");

        // 응답 전송
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);
    }
}
```

##### 2) 라이프사이클 관리

- **HttpServer**: 개발자가 직접 서버 시작/종료, 스레드 풀 관리
- **Servlet**: 컨테이너(Tomcat)가 자동으로 생명주기 관리 (init, service, destroy)

##### 3) 배포 방식

- **HttpServer**:
  - 컴파일: `javac -d bin src/**/*.java`
  - 실행: `java -cp bin:lib/* com.madang.server.MadangServer`

- **Servlet**:
  - WAR 파일로 패키징: `jar cvf madang.war *`
  - Tomcat webapps 폴더에 배포
  - 자동으로 서비스 시작

---

### 3.3 Q3: JSP와의 차이점

#### JSP란?

**JSP (JavaServer Pages)**는 HTML 안에 Java 코드를 직접 삽입하여 동적 웹 페이지를 생성하는 기술입니다.

**쉬운 비유**:
- **현재 방식**: 주방(Backend)과 식당 홀(Frontend)이 완전히 분리되어 있고, 메뉴판(API)을 통해서만 소통
- **JSP 방식**: 주방에서 직접 요리를 만들면서 동시에 접시에 담아 서빙까지 함

#### 현재 구조 vs JSP 구조

##### 현재 구조 (REST API + Vanilla JavaScript)

```
Frontend (books.html)
    ↓ AJAX 요청
Backend (BookHandler)
    ↓ JSON 응답
Frontend JavaScript가 HTML 동적 생성
```

**books.html** (정적):
```html
<div id="book-list"></div>
<script>
  fetch('/api/books')
    .then(res => res.json())
    .then(data => {
      // JavaScript로 HTML 동적 생성
      document.getElementById('book-list').innerHTML = ...
    });
</script>
```

##### JSP 방식

```
사용자 요청
    ↓
JSP 파일 (books.jsp)
    ↓ JSTL/EL로 Java 호출
DAO에서 데이터 조회
    ↓
JSP에서 HTML 생성
    ↓
완성된 HTML 응답
```

**books.jsp** (동적):
```jsp
<%@ page import="com.madang.dao.BookDAO" %>
<%@ page import="com.madang.model.Book" %>
<%@ page import="java.util.List" %>

<%
  BookDAO dao = new BookDAO();
  List<Book> books = dao.getBooks(null, null, null, null, null, null);
%>

<div id="book-list">
  <% for (Book book : books) { %>
    <div class="book-card">
      <h3><%= book.getBookname() %></h3>
      <p><%= book.getPublisher() %></p>
      <p><%= book.getPrice() %>원</p>
    </div>
  <% } %>
</div>
```

#### 비교표

| 항목 | 현재 (REST API + JS) | JSP |
|------|---------------------|-----|
| **Frontend/Backend 분리** | ✅ 완전 분리 | ❌ 결합됨 |
| **개발 역할 분담** | Frontend/Backend 독립 개발 가능 | 서버 개발자가 화면까지 개발 |
| **디자이너 협업** | 용이 (HTML/CSS만 수정) | 어려움 (Java 코드 이해 필요) |
| **API 재사용** | ✅ 모바일 앱 등에서 재사용 가능 | ❌ 웹 전용 |
| **페이지 로딩** | SPA 방식 가능 (빠른 UX) | 매 요청마다 전체 페이지 로드 |
| **JavaScript 프레임워크** | React, Vue 사용 가능 | 제한적 |
| **캐싱** | API 캐싱 용이 | 페이지 단위 캐싱 |
| **학습 난이도** | 중간 (JS + REST API) | 낮음 (초기 진입) |
| **유지보수** | 우수 (관심사 분리) | 보통 (코드 혼재) |

#### 언제 JSP를 사용하는가?

JSP는 다음과 같은 경우에 적합합니다:

1. **서버 렌더링이 중요한 경우**: SEO(검색엔진 최적화)가 필수적인 공개 웹사이트
2. **단순한 CRUD 관리 페이지**: 복잡한 사용자 상호작용이 없는 관리자 페이지
3. **Legacy 시스템 유지보수**: 기존 JSP 프로젝트를 유지해야 하는 경우
4. **빠른 프로토타입**: MVP(Minimum Viable Product) 개발

**현재 프로젝트에는 적합하지 않은 이유**:
- ❌ 이미 REST API로 깔끔하게 분리되어 있음
- ❌ 교육 목적상 현대적인 아키텍처(REST API) 학습이 유리
- ❌ JSP 추가 시 프로젝트 복잡도만 증가
- ❌ 디자이너와의 협업 어려워짐

---

### 3.4 Q4: Frontend 코드 변경 필요성

**답변: 아니요, 변경 불필요합니다. ✅**

#### 핵심 원리: API 계약(Contract)

Backend를 HttpServer에서 Servlet으로 변경하더라도, **API 엔드포인트와 응답 형식이 동일하게 유지**되므로 Frontend는 전혀 변경하지 않아도 됩니다.

#### API 계약 예시

**도서 목록 조회 API**

```
요청:
  GET /api/books?action=list&sortBy=price&direction=asc

응답 (JSON):
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

이 API 계약은 Backend 구현 방식(HttpServer, Servlet, 심지어 다른 언어)과 **완전히 독립적**입니다.

#### Frontend에서 보이는 것

Frontend JavaScript는 다음과 같이 API를 호출합니다:

```javascript
// frontend/js/api.js
async function fetchAPI(endpoint, options = {}) {
  const url = API_BASE_URL + endpoint;
  const response = await fetch(url, options);
  return await response.json();
}

// 도서 목록 조회
const data = await fetchAPI('/api/books?action=list');
```

Backend가 HttpServer든 Servlet이든, 이 코드는 **전혀 변경되지 않습니다**. 왜냐하면:

1. **엔드포인트가 동일**: `/api/books`
2. **HTTP 메서드가 동일**: `GET`, `POST`, `PUT`, `DELETE`
3. **응답 형식이 동일**: JSON
4. **응답 구조가 동일**: `{ success, data }` 형태

#### 검증 방법

Backend 변경 후 다음과 같이 테스트하면 됩니다:

```bash
# 1. API 응답 테스트 (Backend만 테스트)
curl http://localhost:8080/api/books?action=list

# 2. Frontend 통합 테스트
# 브라우저에서 http://localhost:8080/books.html 접속
# 도서 목록이 정상적으로 표시되는지 확인
```

#### 변경이 필요한 유일한 경우

**API Base URL**만 변경되는 경우:

```javascript
// frontend/js/api.js
const API_BASE_URL = 'http://localhost:8080';  // HttpServer
// 또는
const API_BASE_URL = 'http://localhost:8080/madang';  // Servlet (Context Path)
```

Servlet으로 전환 시 Context Path가 추가될 수 있지만, 이 역시 설정으로 조정 가능합니다.

---

## 4. 기술 비교 분석

### 4.1 종합 비교표

| 평가 항목 | HttpServer (현재) | Servlet | JSP |
|----------|------------------|---------|-----|
| **학습 난이도** | ⭐⭐ (낮음) | ⭐⭐⭐ (중간) | ⭐⭐ (낮음) |
| **구현 복잡도** | ⭐⭐ (간단) | ⭐⭐⭐ (중간) | ⭐⭐⭐⭐ (복잡) |
| **성능** | ⭐⭐⭐ (보통) | ⭐⭐⭐⭐ (우수) | ⭐⭐⭐ (보통) |
| **확장성** | ⭐⭐ (제한적) | ⭐⭐⭐⭐⭐ (매우 우수) | ⭐⭐⭐ (보통) |
| **프로덕션 적합성** | ⭐ (부적합) | ⭐⭐⭐⭐⭐ (매우 적합) | ⭐⭐⭐ (보통) |
| **교육 목적 적합성** | ⭐⭐⭐⭐⭐ (매우 적합) | ⭐⭐⭐⭐ (적합) | ⭐⭐ (제한적) |
| **현대적 개발 방식** | ⭐⭐⭐⭐ (REST API) | ⭐⭐⭐⭐⭐ (REST API + 표준) | ⭐⭐ (Legacy) |
| **Frontend 독립성** | ⭐⭐⭐⭐⭐ (완전 분리) | ⭐⭐⭐⭐⭐ (완전 분리) | ⭐ (결합됨) |
| **API 재사용성** | ⭐⭐⭐⭐⭐ (우수) | ⭐⭐⭐⭐⭐ (우수) | ⭐ (불가) |
| **유지보수** | ⭐⭐⭐⭐ (양호) | ⭐⭐⭐⭐⭐ (우수) | ⭐⭐⭐ (보통) |
| **초기 설정 시간** | 5분 | 30분 | 1시간 |
| **개발 생산성 (소규모)** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **개발 생산성 (대규모)** | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ |

### 4.2 비용 분석

#### 개발 비용

| 단계 | HttpServer | Servlet | JSP |
|------|-----------|---------|-----|
| **초기 학습** | 1일 | 3일 | 2일 |
| **환경 설정** | 10분 | 1시간 | 2시간 |
| **기능 개발** | 기준 | +20% | +30% |
| **테스트** | 기준 | +10% | +20% |
| **배포** | 1분 | 10분 | 15분 |

#### 운영 비용

| 항목 | HttpServer | Servlet | JSP |
|------|-----------|---------|-----|
| **서버 리소스** | 매우 낮음 | 보통 | 보통 |
| **모니터링 도구** | 직접 구현 | 다양한 도구 지원 | 다양한 도구 지원 |
| **유지보수 공수** | 낮음 (간단한 구조) | 중간 | 높음 (복잡성) |

### 4.3 실제 사용 사례

#### HttpServer 사용이 적합한 경우

1. ✅ **교육용 프로젝트** (현재 프로젝트)
2. ✅ **프로토타입/MVP 개발**
3. ✅ **마이크로서비스 학습**
4. ✅ **IoT 디바이스 경량 서버**

#### Servlet 사용이 적합한 경우

1. ✅ **엔터프라이즈 웹 애플리케이션**
2. ✅ **대규모 트래픽 처리**
3. ✅ **Spring Boot로 확장 계획이 있는 경우**
4. ✅ **표준 기술 스택이 요구되는 경우**

#### JSP 사용이 적합한 경우

1. ✅ **SEO가 중요한 공개 웹사이트**
2. ✅ **단순한 관리자 페이지 (CRUD)**
3. ✅ **Legacy 시스템 유지보수**
4. ⚠️ **현재 프로젝트에는 부적합**

---

## 5. 전환 시나리오

### 5.1 시나리오 A: 현재 HttpServer 유지 (권장 ⭐⭐⭐⭐⭐)

#### 개요
현재 구조를 그대로 유지하며, 필요한 경우 기능만 추가/개선

#### 장점
- ✅ 추가 작업 불필요 (공수 0일)
- ✅ 학습 곡선 없음
- ✅ 교육 목적에 최적화
- ✅ 빠른 개발 속도 유지
- ✅ 리스크 없음

#### 단점
- ⚠️ 프로덕션 환경에 부적합 (하지만 교육용이므로 문제없음)
- ⚠️ Spring Boot로 확장 시 전환 필요

#### 권장 대상
- 교육/학습이 주 목적인 프로젝트
- 빠른 프로토타입 개발
- 소규모 팀 프로젝트

---

### 5.2 시나리오 B: Servlet으로 전환

#### 개요
현재 Handler 구조를 Servlet 기반으로 변경

#### 전환 로드맵

##### Phase 1: 프로젝트 구조 변경 (4시간)

1. **web.xml 생성** 또는 **어노테이션 방식 결정**
2. **디렉토리 구조 변경**:
   ```
   src/
   ├── main/
   │   ├── java/
   │   │   └── com/madang/
   │   │       ├── servlet/       # ApiServlet (새로 작성)
   │   │       ├── handler/ 제거  # Servlet으로 통합
   │   │       ├── dao/           # 변경 없음
   │   │       ├── model/         # 변경 없음
   │   │       └── util/          # 변경 없음
   │   ├── webapp/
   │   │   ├── WEB-INF/
   │   │   │   └── web.xml
   │   │   ├── index.html
   │   │   ├── books.html
   │   │   └── ...
   │   └── resources/
   └── test/
   ```

##### Phase 2: 코드 마이그레이션 (8시간)

1. **ApiHandler → ApiServlet 변환**
   ```java
   // 변경 전 (HttpServer)
   public abstract class ApiHandler implements HttpHandler {
       public void handle(HttpExchange exchange) { ... }
   }

   // 변경 후 (Servlet)
   public abstract class ApiServlet extends HttpServlet {
       protected void service(HttpServletRequest req,
                              HttpServletResponse resp) { ... }
   }
   ```

2. **Handler → Servlet 변환**
   ```java
   // BookHandler.java → BookServlet.java
   @WebServlet("/api/books")
   public class BookServlet extends ApiServlet {
       @Override
       protected void handleGet(Map<String, String> params) {
           // 비즈니스 로직은 동일
       }
   }
   ```

3. **DAO, Model, Util**: 변경 없음

##### Phase 3: 테스트 및 검증 (4시간)

1. **단위 테스트**: 각 Servlet 단위 테스트
2. **통합 테스트**: API 응답 검증
3. **Frontend 테스트**: 기존 HTML 페이지 동작 확인
4. **성능 테스트**: 부하 테스트 및 비교

##### 총 예상 공수

- **개발 시간**: 2~3일 (1인 개발자)
- **테스트 시간**: 1일
- **문서화**: 0.5일
- **총합**: **3.5~4.5일**

#### 장점
- ✅ 표준 기술 스택 (Jakarta EE)
- ✅ Spring Boot로 확장 용이
- ✅ 프로덕션 환경 준비
- ✅ 다양한 도구/라이브러리 지원

#### 단점
- ⚠️ 개발 공수 소요 (3.5~4.5일)
- ⚠️ Tomcat 설치 및 관리 필요
- ⚠️ 학습 곡선 증가
- ⚠️ 초기 설정 복잡도 증가

#### 권장 대상
- 실무 환경 학습이 필요한 경우
- Spring Boot로 확장 계획이 있는 경우
- 표준 기술 스택 경험이 필요한 경우

---

### 5.3 시나리오 C: JSP 추가 (비권장 ⭐)

#### 개요
현재 REST API는 유지하되, 일부 페이지를 JSP로 구현

#### 예시: 도서 목록 페이지

**현재 방식**:
- `books.html` (정적) + `books.js` (동적 로딩)

**JSP 방식**:
- `books.jsp` (서버에서 HTML 생성)

#### 장점
- ✅ 서버 사이드 렌더링 (SSR) 학습 가능
- ✅ SEO 개선 (검색엔진 최적화)

#### 단점
- ❌ **프로젝트 복잡도 증가**: REST API + JSP 혼재
- ❌ **일관성 저하**: 일부는 SPA, 일부는 SSR
- ❌ **디자이너 협업 어려움**: JSP는 Java 코드 포함
- ❌ **현대적 개발 트렌드와 불일치**
- ❌ **교육 효과 감소**: 두 가지 방식을 동시에 학습하면 혼란

#### 권장 대상
- **없음** (현재 프로젝트에는 부적합)

---

## 6. 권장사항

### 6.1 최종 권장 방향

#### 🏆 **시나리오 A: 현재 HttpServer 유지** (강력 권장)

**이유**:

1. **교육 목적에 최적화**
   - 현재 프로젝트는 "SQL과 웹 개발을 학습하기 위한 교육용 프로젝트"
   - HttpServer는 웹 서버의 기본 원리를 이해하기에 가장 적합
   - 설정 복잡도가 낮아 학습자가 본질(SQL, CRUD)에 집중 가능

2. **충분히 현대적인 아키텍처**
   - REST API + JSON 응답 (현대 웹 개발의 표준)
   - Frontend/Backend 완전 분리 (마이크로서비스 아키텍처 기초)
   - DAO 패턴, Session 관리 등 실무 패턴 포함

3. **코드 품질 우수**
   - 이미 잘 구조화된 3계층 아키텍처
   - 명확한 책임 분리 (Handler, DAO, Model, Util)
   - JDBC PreparedStatement로 SQL Injection 방지
   - HikariCP 연결 풀로 성능 최적화

4. **리스크 없음**
   - 추가 작업 불필요 (공수 0일)
   - 기존 코드 안정성 유지
   - 학습 곡선 없음

### 6.2 조건부 권장 (선택 사항)

#### Servlet 전환을 고려할 수 있는 경우

다음 **모든 조건**을 만족하는 경우에만 Servlet 전환을 고려:

1. ✅ 학습자가 이미 HttpServer로 충분히 학습함
2. ✅ 실무 표준 기술(Servlet)을 배우고 싶음
3. ✅ Spring Boot로 확장할 계획이 있음
4. ✅ 3~4일의 추가 개발 시간을 투자할 수 있음

**전환 시기**: 현재 프로젝트를 완성한 **이후**, 별도의 학습 단계로 진행

### 6.3 절대 비권장

#### ❌ JSP 추가

- 현재 프로젝트에 JSP를 추가하는 것은 **강력히 비권장**
- 이유:
  1. 프로젝트 복잡도만 증가
  2. REST API와 혼재되어 일관성 저하
  3. 현대적 개발 트렌드와 불일치
  4. 교육 효과 감소

### 6.4 단계별 학습 로드맵 (권장)

프로젝트를 단계별로 확장하려면 다음 순서를 추천합니다:

```
1단계: HttpServer + REST API (현재)
   ↓ 충분히 학습 후
2단계: Servlet + REST API
   ↓ 충분히 학습 후
3단계: Spring Boot + REST API
   ↓ 고급 학습
4단계: Spring Boot + React/Vue
```

각 단계는 **독립적인 프로젝트**로 진행하여, 비교 학습하는 것이 효과적입니다.

### 6.5 핵심 메시지

> **"현재 프로젝트는 이미 충분히 우수하고 현대적입니다."**

- ✅ REST API 기반 (현대 웹 개발의 표준)
- ✅ Frontend/Backend 완전 분리 (마이크로서비스 기초)
- ✅ 깔끔한 3계층 아키텍처
- ✅ 실무에서 사용하는 패턴 (DAO, Session 관리, 연결 풀 등)
- ✅ 보안 고려 (PreparedStatement, Session 관리)

**변경하지 않고 유지하는 것이 최선의 선택입니다.**

---

## 7. 부록

### 7.1 주요 용어 정리

| 용어 | 설명 |
|------|------|
| **HttpServer** | Java 표준 라이브러리(JDK)에 포함된 간단한 HTTP 웹 서버 |
| **Servlet** | Java로 웹 애플리케이션을 개발하기 위한 표준 기술 (Jakarta EE) |
| **Tomcat** | Servlet을 실행할 수 있는 서블릿 컨테이너 (웹 애플리케이션 서버) |
| **JSP** | HTML 안에 Java 코드를 삽입하여 동적 페이지를 생성하는 기술 |
| **REST API** | HTTP를 통해 자원(Resource)을 주고받는 웹 아키텍처 스타일 |
| **JSON** | JavaScript Object Notation, 데이터 교환 형식 |
| **DAO** | Data Access Object, 데이터베이스 접근 로직을 분리한 패턴 |
| **JDBC** | Java Database Connectivity, Java에서 DB에 접근하는 표준 API |
| **HikariCP** | 고성능 JDBC 연결 풀 라이브러리 |
| **3계층 아키텍처** | 프레젠테이션(Frontend) - 비즈니스 로직(Backend) - 데이터(DB)로 분리한 구조 |

### 7.2 참고 자료

#### 공식 문서
- [Java HttpServer Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpServer.html)
- [Jakarta Servlet Specification](https://jakarta.ee/specifications/servlet/)
- [Apache Tomcat Documentation](https://tomcat.apache.org/tomcat-10.1-doc/)

#### 프로젝트 문서
- `docs/architecture.md` - 현재 아키텍처 상세 설명
- `docs/simple-server-guide.md` - HttpServer 가이드
- `CLAUDE.md` - 프로젝트 개요 및 기술 스택

### 7.3 FAQ

#### Q: HttpServer는 왜 프로덕션에 부적합한가요?

**A**: HttpServer는 다음 기능이 부족합니다:
- 세션 클러스터링 (여러 서버 간 세션 공유)
- 로드 밸런싱 (부하 분산)
- Hot Deployment (서버 재시작 없이 배포)
- JMX 모니터링
- 다양한 보안 기능
- 성능 튜닝 옵션

프로덕션에서는 안정성과 확장성이 중요하므로, Tomcat 등 검증된 서블릿 컨테이너를 사용합니다.

#### Q: Servlet으로 전환하면 성능이 얼마나 좋아지나요?

**A**: 현재 프로젝트 규모(소규모 교육용)에서는 **체감할 만한 성능 차이가 없습니다**. Tomcat의 장점은 대규모 트래픽 처리와 고급 기능이므로, 교육용 프로젝트에서는 오버스펙입니다.

#### Q: Frontend 개발자는 Backend 기술을 알아야 하나요?

**A**: **아니요**. REST API 계약(엔드포인트, 파라미터, 응답 형식)만 알면 됩니다. Backend가 HttpServer든 Servlet이든 Frontend 개발에는 영향이 없습니다. 이것이 바로 **관심사 분리(Separation of Concerns)**의 핵심입니다.

#### Q: 이 프로젝트를 실제 서비스로 배포하려면?

**A**: 교육용 프로젝트를 실제 서비스로 전환하려면:
1. Servlet 또는 Spring Boot로 전환
2. HTTPS 적용
3. 비밀번호 암호화 (BCrypt)
4. 입력 검증 강화
5. 로깅 및 모니터링 추가
6. 백업 및 복구 전략 수립
7. 성능 테스트 및 튜닝

하지만 **교육 목적으로는 현재 구조가 최적**입니다.

---

## 8. 결론

### 핵심 답변 요약

1. **Servlet 전환 가능성**: ✅ 가능하며, DAO/Model/Util/Frontend는 변경 불필요
2. **HttpServer vs Servlet**: HttpServer는 간단하고 교육에 적합, Servlet은 표준이며 프로덕션에 적합
3. **JSP와의 차이**: 현재는 REST API (Frontend/Backend 분리), JSP는 결합형 (비권장)
4. **Frontend 변경 필요성**: ❌ 불필요 (API 계약이 동일하므로)

### 최종 권장

> **🏆 현재 HttpServer 구조를 그대로 유지하는 것을 강력히 권장합니다.**

**이유**:
- 교육 목적에 최적화된 구조
- 이미 충분히 현대적이고 우수한 아키텍처
- 추가 작업 불필요
- 학습에 집중 가능

---

**문서 작성**: 2025-10-22
**검토자**: 개발팀 전체
**승인**: (의사결정 후 기입)
