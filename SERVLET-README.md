# 마당 온라인 서점 - Servlet 버전

**버전**: 2.0.0-servlet
**기반**: Jakarta EE 11, Servlet 6.1
**서버**: Apache Tomcat 11.0+
**전환 완료일**: 2025-10-22

---

## 📌 개요

이 프로젝트는 기존 **Java HttpServer 기반**에서 **Jakarta EE Servlet 기반**으로 전환되었습니다.

### 주요 변경사항

| 항목 | 이전 (v1.0) | 현재 (v2.0-servlet) |
|------|------------|---------------------|
| **서버** | Java 21 HttpServer | Apache Tomcat 11.0+ |
| **Jakarta EE** | - | Jakarta EE 11 |
| **Servlet API** | - | Servlet 6.1 |
| **API 기본 클래스** | `ApiHandler extends HttpHandler` | `ApiServlet extends HttpServlet` |
| **요청/응답** | `HttpExchange` | `HttpServletRequest/Response` |
| **라우팅** | `server.createContext()` | `@WebServlet` 어노테이션 |
| **배포 방식** | `java -cp` 직접 실행 | WAR 파일 → Tomcat 배포 |
| **프로젝트 구조** | Flat 구조 | Maven 표준 구조 |

### 유지된 부분 (100% 재사용)

✅ **DAO 계층** - BookDAO, CustomerDAO, OrderDAO
✅ **Model 계층** - Book, Customer, Order, PageRequest, PageResponse
✅ **Util 계층** - DBConnection, SessionManager, ConfigManager, SqlLogger
✅ **Frontend** - 모든 HTML, CSS, JavaScript (변경 없음)
✅ **비즈니스 로직** - 모든 CRUD 및 통계 로직

---

## 🚀 빠른 시작

### 1. 사전 준비

- Java 17 이상 (Jakarta EE 11 요구사항, Java 21 권장)
- MySQL 5.7+ (madangdb 데이터베이스)
- Apache Tomcat 11.0+ 또는 Maven

### 2. 빌드

#### Option A: Maven 사용 (권장)

```bash
mvn clean package
# 결과: target/madang.war
```

#### Option B: 수동 빌드 스크립트

```bash
./build-manual.sh
# 결과: target/madang.war
```

### 3. Tomcat 배포

```bash
# Tomcat 설치 (예: Ubuntu)
wget https://dlcdn.apache.org/tomcat/tomcat-11/v11.0.13/bin/apache-tomcat-11.0.13.tar.gz
tar xzf apache-tomcat-11.0.13.tar.gz
sudo mv apache-tomcat-11.0.13 /opt/tomcat

# WAR 파일 배포
cp target/madang.war /opt/tomcat/webapps/

# Tomcat 시작
/opt/tomcat/bin/startup.sh
```

### 4. 접속

- **Frontend**: http://localhost:8080/madang/
- **Health Check**: http://localhost:8080/madang/api/health
- **관리자**: http://localhost:8080/madang/admin/

---

## 📁 프로젝트 구조

```
madang/
├── pom.xml                          # Maven 설정
├── build-manual.sh                  # 수동 빌드 스크립트
├── src/
│   └── main/
│       ├── java/com/madang/
│       │   ├── servlet/             # 🆕 Servlet 클래스
│       │   │   ├── ApiServlet.java  # 기본 서블릿 (ApiHandler 대체)
│       │   │   ├── BookServlet.java
│       │   │   ├── CustomerServlet.java
│       │   │   ├── OrderServlet.java
│       │   │   └── StatsServlet.java
│       │   ├── dao/                 # ✅ 변경 없음
│       │   ├── model/               # ✅ 변경 없음
│       │   └── util/                # ✅ 변경 없음
│       ├── webapp/                  # 🆕 웹 리소스
│       │   ├── WEB-INF/
│       │   │   └── web.xml          # 서블릿 설정
│       │   ├── index.html           # ✅ Frontend (변경 없음)
│       │   ├── books.html
│       │   ├── css/
│       │   └── js/
│       └── resources/
│           └── application.properties
├── docs/
│   ├── servlet-migration-plan.md    # 🆕 전환 계획서
│   ├── tomcat-deployment-guide.md   # 🆕 배포 가이드
│   ├── backend-architecture-review.md
│   └── architecture.md
└── target/
    └── madang.war                   # 빌드 결과
```

---

## 🔧 주요 코드 변환

### ApiHandler → ApiServlet

**변경 전 (ApiHandler.java)**:
```java
public abstract class ApiHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS, 메서드 라우팅, JSON 응답
    }

    protected abstract String handleGet(Map<String, String> params);
}
```

**변경 후 (ApiServlet.java)**:
```java
public abstract class ApiServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // CORS, 메서드 라우팅, JSON 응답
    }

    protected abstract String handleGet(Map<String, String> params,
                                       HttpServletRequest req,
                                       HttpServletResponse resp);
}
```

### Handler → Servlet 변환 예시

**변경 전 (BookHandler.java)**:
```java
public class BookHandler extends ApiHandler {
    // 라우팅: MadangServer.java에서 등록
    // server.createContext("/api/books", new BookHandler());
}
```

**변경 후 (BookServlet.java)**:
```java
@WebServlet("/api/books")  // 🆕 어노테이션으로 라우팅
public class BookServlet extends ApiServlet {
    // 비즈니스 로직은 100% 동일
}
```

---

## 📚 문서

| 문서 | 내용 |
|------|------|
| `docs/servlet-migration-plan.md` | Servlet 전환 상세 계획 |
| `docs/tomcat-deployment-guide.md` | Tomcat 설치 및 배포 가이드 |
| `docs/backend-architecture-review.md` | HttpServer vs Servlet 비교 분석 |
| `CLAUDE.md` | 프로젝트 전체 개요 |

---

## 🔍 API 엔드포인트 (변경 없음)

모든 API 엔드포인트는 기존과 **100% 호환**됩니다.

### Books

- `GET /api/books?action=list` - 도서 목록
- `GET /api/books?action=detail&id=1` - 도서 상세
- `POST /api/books?action=create` - 도서 등록
- `PUT /api/books?action=update` - 도서 수정
- `DELETE /api/books?action=delete&id=1` - 도서 삭제

### Customers

- `GET /api/customers?action=list` - 고객 목록
- `POST /api/customers?action=login` - 로그인
- `POST /api/customers?action=create` - 고객 등록
- `PUT /api/customers?action=update` - 고객 정보 수정
- `DELETE /api/customers?action=delete&id=1` - 고객 삭제

### Orders

- `GET /api/orders?action=list&custid=1` - 주문 목록
- `POST /api/orders?action=create` - 주문 생성
- `DELETE /api/orders?action=delete&id=1&custid=1` - 주문 취소

### Stats

- `GET /api/stats?action=overview` - 전체 통계
- `GET /api/stats?action=bestsellers&limit=5` - 베스트셀러
- `GET /api/stats?action=monthly&months=12` - 월별 매출

---

## 🧪 테스트

### API 테스트

```bash
# Health Check (서버 및 DB 상태 확인)
curl http://localhost:8080/madang/api/health

# 도서 목록 조회
curl http://localhost:8080/madang/api/books?action=list

# 고객 목록 조회
curl http://localhost:8080/madang/api/customers?action=list
```

### 로그 확인

```bash
# Tomcat 로그
tail -f /opt/tomcat/logs/catalina.out

# 애플리케이션 로그
tail -f /opt/tomcat/logs/madang.log
```

---

## 💡 주요 기능

### Backend (Servlet)

✅ **Jakarta EE 11 & Servlet 6.1 표준 준수**
- Servlet 6.1 API (2024년 6월 릴리스)
- Java 17 이상 필수
- 어노테이션 기반 라우팅 (`@WebServlet`)
- 표준 요청/응답 처리
- SecurityManager 제거로 보안 강화
- ByteBuffer 지원 및 Charset 메서드 개선

✅ **변경 없는 비즈니스 로직**
- CRUD 연산 (Create, Read, Delete)
- JOIN 쿼리 (고객-주문-도서)
- 통계 및 분석 (GROUP BY, 집계 함수)
- 페이지네이션

✅ **세션 관리**
- 서버 측 세션 (SessionManager)
- X-Session-Id 헤더 기반
- 2시간 타임아웃

✅ **권한 검증**
- Role 기반 접근 제어 (admin, manager, customer)
- `requireAdmin()`, `requireRole()`, `requireSelfOrAdmin()`

### Frontend (변경 없음)

✅ **Vanilla JavaScript**
- 프레임워크 없이 순수 JavaScript
- REST API 호출 (Fetch API)
- 동적 HTML 생성

✅ **반응형 디자인**
- Utility-first CSS (Tailwind 스타일)
- 모바일 친화적 UI

---

## 📊 성능 비교

| 항목 | HttpServer (v1.0) | Servlet (Tomcat 11, v2.0) |
|------|-------------------|---------------------------|
| **Jakarta EE** | - | Jakarta EE 11 |
| **Servlet API** | - | Servlet 6.1 |
| **Java 요구사항** | Java 21 | Java 17+ (21 권장) |
| **시작 시간** | ~1초 | ~3초 |
| **메모리 사용** | ~100MB | ~150MB |
| **동시 접속** | 제한적 | 수백~수천 |
| **확장성** | 낮음 | 높음 |
| **안정성** | 보통 | 우수 |
| **모니터링** | 제한적 | 다양한 도구 |
| **표준 준수** | 없음 | Jakarta EE 11 표준 |

---

## 🛠️ 개발 환경

### 필수 도구

- **Java 17+** (Jakarta EE 11 요구사항, Java 21 권장)
- **Maven 3.8+** (또는 수동 빌드 스크립트)
- **MySQL 5.7+**
- **Apache Tomcat 11.0+** (Jakarta EE 11, Servlet 6.1 지원)

### 권장 IDE

- IntelliJ IDEA (Ultimate 또는 Community)
- Eclipse IDE for Enterprise Java
- VS Code + Extension Pack for Java

### 데이터베이스

```sql
-- 데이터베이스 생성
CREATE DATABASE madangdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 및 권한 부여
CREATE USER 'madang'@'localhost' IDENTIFIED BY 'madang';
GRANT ALL PRIVILEGES ON madangdb.* TO 'madang'@'localhost';
FLUSH PRIVILEGES;
```

---

## 🔐 보안

### 구현된 보안 기능

✅ SQL Injection 방지 (PreparedStatement)
✅ XSS 방지 (textContent 사용)
✅ 서버 측 세션 관리
✅ Role 기반 접근 제어 (RBAC)
✅ CORS 헤더 설정

### 프로덕션 권장 사항

⚠️ 비밀번호 해싱 (BCrypt)
⚠️ HTTPS 적용
⚠️ CSRF 토큰
⚠️ Rate Limiting
⚠️ 입력 검증 강화

---

## 🚧 문제 해결

### 일반적인 오류

**오류**: "ClassNotFoundException: jakarta.servlet.http.HttpServlet"
**해결**: Tomcat 11.0 이상 사용 (Jakarta EE 11, Servlet 6.1)
- Tomcat 10.1은 Servlet 6.0 지원 (일부 기능 제한 가능)

**오류**: "Port 8080 already in use"
**해결**: `lsof -i :8080` → `kill -9 <PID>`

**오류**: "Cannot load JDBC driver"
**해결**: `WEB-INF/lib/`에 mysql-connector-j.jar 추가

**오류**: 브라우저에서 "도서 데이터를 불러오지 못했습니다" (데이터 로드 실패)
**원인**: JavaScript API 경로가 Tomcat context path와 불일치
**해결**:
1. 브라우저 개발자 도구(F12) → Network 탭에서 404 에러 확인
2. `src/main/webapp/js/api.js` 수정:
   ```javascript
   const API_BASE_URL = '/madang';  // WAR 파일명과 일치시킴
   ```
3. 재빌드 및 배포: `mvn clean package && cp target/madang.war $CATALINA_HOME/webapps/`
4. 브라우저 캐시 삭제: Ctrl+Shift+R

자세한 내용은 `docs/tomcat-deployment-guide.md` 참조

---

## 📝 변경 로그

### v2.0.0-servlet (2025-10-22)

**Added**:
- ✨ Jakarta EE 11 지원
- ✨ Jakarta Servlet 6.1 지원 (2024년 6월 릴리스)
- ✨ Apache Tomcat 11.0+ 지원
- ✨ Maven 프로젝트 구조
- ✨ `@WebServlet` 어노테이션 라우팅
- ✨ WAR 패키징 및 배포
- ✨ `/api/health` 헬스체크 엔드포인트 (서버 상태, DB 연결, 시스템 정보)
- 📚 Servlet 전환 계획서
- 📚 Tomcat 배포 가이드

**Changed**:
- 🔄 `ApiHandler` → `ApiServlet` 변환
- 🔄 `BookHandler` → `BookServlet` 변환
- 🔄 `CustomerHandler` → `CustomerServlet` 변환
- 🔄 `OrderHandler` → `OrderServlet` 변환
- 🔄 `StatsHandler` → `StatsServlet` 변환
- 🔄 프로젝트 구조: flat → Maven 표준
- 🎨 대시보드 고객 세그먼트 차트 색상 개선 (VIP: 황금색, 우수: 핑크, 일반: 회색, 신규: 파란색)

**Preserved**:
- ✅ DAO, Model, Util 계층 (100% 재사용)
- ✅ Frontend (HTML, CSS, JavaScript)
- ✅ 비즈니스 로직 (CRUD, 통계)
- ✅ API 엔드포인트 (완전 호환)

### v1.0.0 (2025-10-15)

- 🎉 Initial release (HttpServer 기반)

---

## 🤝 기여

이 프로젝트는 **교육용**으로 제작되었습니다.

---

## 📄 라이선스

MIT License

---

## 👥 작성자

**개발팀**
**마지막 업데이트**: 2025-10-22
**버전**: 2.0.0-servlet

---

## 🔗 관련 링크

### Jakarta EE 및 Servlet
- [Jakarta EE 11 Platform](https://jakarta.ee/specifications/platform/11/)
- [Jakarta Servlet 6.1 Specification](https://jakarta.ee/specifications/servlet/6.1/)
- [What's New in Servlet 6.1](https://jakarta.ee/specifications/servlet/6.1/jakarta-servlet-spec-6.1.html#_what_s_new_in_servlet_6_1)

### Apache Tomcat
- [Apache Tomcat 11.0 Documentation](https://tomcat.apache.org/tomcat-11.0-doc/)
- [Tomcat Version Specifications](https://tomcat.apache.org/whichversion.html)

### 빌드 도구
- [Maven Documentation](https://maven.apache.org/guides/)
