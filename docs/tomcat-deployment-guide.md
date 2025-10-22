# Tomcat 배포 가이드

**대상**: 마당 온라인 서점 Servlet 버전 (Jakarta EE 11, Servlet 6.1)
**작성일**: 2025-10-22
**최종 수정**: 2025-10-22
**목적**: Apache Tomcat 11.0 설치 및 WAR 배포 가이드

---

## 📋 목차

1. [사전 준비](#1-사전-준비)
2. [Tomcat 설치](#2-tomcat-설치)
3. [WAR 파일 빌드](#3-war-파일-빌드)
4. [Tomcat 배포](#4-tomcat-배포)
5. [실행 및 테스트](#5-실행-및-테스트)
6. [문제 해결](#6-문제-해결)

---

## 1. 사전 준비

### 1.1 필수 요구사항

- **Java 17 이상** 설치 (Jakarta EE 11 요구사항, Java 21 권장)
- **MySQL 5.7 이상** 실행 중
- **Git** (선택사항)
- **Maven** (선택사항, 수동 빌드 스크립트 제공)

### 1.2 Java 버전 확인

```bash
java -version
# 출력: openjdk version "17.0.x" 또는 "21.0.x" 이상
```

**참고**: Jakarta EE 11은 Java 17 이상을 요구합니다.

Java가 설치되어 있지 않으면:

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# macOS (Homebrew)
brew install openjdk@21
```

---

## 2. Tomcat 설치

### 2.1 Tomcat 다운로드

**Apache Tomcat 11.0** (Jakarta EE 11, Servlet 6.1 지원) 권장:

```bash
# 1. Tomcat 11.0.13 다운로드
cd ~
wget https://dlcdn.apache.org/tomcat/tomcat-11/v11.0.13/bin/apache-tomcat-11.0.13.tar.gz

# 2. 압축 해제
tar xzf apache-tomcat-11.0.13.tar.gz

# 3. /opt/tomcat으로 이동 (권장)
sudo mv apache-tomcat-11.0.13 /opt/tomcat
```

**참고**:
- Tomcat 11.0.x는 **Servlet 6.1, JSP 4.0, EL 6.0, WebSocket 2.2, Authentication 3.1**을 지원합니다
- Java 17 이상 필수
- 최신 릴리스는 [Apache Tomcat 11 다운로드 페이지](https://tomcat.apache.org/download-11.cgi)에서 확인

### 2.2 환경 변수 설정

```bash
# ~/.bashrc 또는 ~/.zshrc에 추가
export CATALINA_HOME=/opt/tomcat
export PATH=$CATALINA_HOME/bin:$PATH

# 적용
source ~/.bashrc  # 또는 source ~/.zshrc
```

### 2.3 실행 권한 부여

```bash
chmod +x $CATALINA_HOME/bin/*.sh
```

### 2.4 Tomcat 테스트

```bash
# Tomcat 시작
$CATALINA_HOME/bin/startup.sh

# 로그 확인
tail -f $CATALINA_HOME/logs/catalina.out

# 브라우저에서 접속
# http://localhost:8080

# Tomcat 중지
$CATALINA_HOME/bin/shutdown.sh
```

---

## 3. WAR 파일 빌드

### 3.1 Option 1: Maven 사용 (권장)

```bash
# Maven 설치 (Ubuntu/Debian)
sudo apt install maven

# Maven 빌드
cd /path/to/madang
mvn clean package

# 결과: target/madang.war 생성
```

### 3.2 Option 2: 수동 빌드 스크립트

Maven이 없는 경우 제공된 스크립트 사용:

```bash
cd /path/to/madang
./build-manual.sh

# 결과: target/madang.war 생성
```

### 3.3 빌드 검증

```bash
# WAR 파일 확인
ls -lh target/madang.war

# WAR 파일 내용 확인
unzip -l target/madang.war | head -20
```

---

## 4. Tomcat 배포

### 4.1 WAR 파일 배포

```bash
# 1. 기존 배포 제거 (있다면)
rm -rf $CATALINA_HOME/webapps/madang
rm -f $CATALINA_HOME/webapps/madang.war

# 2. 새 WAR 파일 복사
cp target/madang.war $CATALINA_HOME/webapps/

# 3. Tomcat이 실행 중이면 자동으로 배포됨
# Tomcat이 중지 상태면 시작
$CATALINA_HOME/bin/startup.sh
```

### 4.2 배포 확인

```bash
# 로그 모니터링 (배포 진행 상황 확인)
tail -f $CATALINA_HOME/logs/catalina.out

# 배포 완료 메시지 예시:
# INFO: Deployment of web application archive [madang.war] has finished in [1,234] ms
```

배포된 디렉토리 확인:

```bash
ls -la $CATALINA_HOME/webapps/madang/
```

### 4.3 데이터베이스 설정

**중요**: WAR 배포 전 또는 후에 데이터베이스 연결 정보를 확인하세요.

**방법 1: web.xml 수정**

```bash
vi $CATALINA_HOME/webapps/madang/WEB-INF/web.xml

# <env-entry> 값 수정
# DB_URL, DB_USER, DB_PASSWORD
```

**방법 2: Tomcat context.xml 사용**

`$CATALINA_HOME/conf/Catalina/localhost/madang.xml` 생성:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>
    <Environment name="DB_URL" value="jdbc:mysql://localhost:3306/madangdb?useSSL=false&amp;serverTimezone=UTC&amp;allowPublicKeyRetrieval=true" type="java.lang.String" override="false"/>
    <Environment name="DB_USER" value="madang" type="java.lang.String" override="false"/>
    <Environment name="DB_PASSWORD" value="madang" type="java.lang.String" override="false"/>
</Context>
```

---

## 5. 실행 및 테스트

### 5.1 Tomcat 시작

```bash
# 시작
$CATALINA_HOME/bin/startup.sh

# 로그 확인
tail -f $CATALINA_HOME/logs/catalina.out
```

### 5.2 API 테스트

```bash
# 1. Health Check (서버 및 DB 상태 확인)
curl http://localhost:8080/madang/api/health

# 예상 응답:
# {
#   "success": true,
#   "data": {
#     "status": "UP",
#     "service": "Madang Bookstore API",
#     "version": "2.0.0-servlet",
#     "timestamp": 1729574400000,
#     "uptime_seconds": 3600,
#     "uptime_formatted": "1h 0m 0s",
#     "database": {
#       "status": "UP",
#       "message": "Database connection successful"
#     },
#     "system": {
#       "java_version": "21.0.1",
#       "os_name": "Linux",
#       "os_arch": "amd64"
#     }
#   }
# }

# 2. 도서 목록 조회
curl http://localhost:8080/madang/api/books?action=list

# 3. 고객 목록 조회
curl http://localhost:8080/madang/api/customers?action=list
```

### 5.3 Frontend 접속

브라우저에서 다음 URL 접속:

- **메인 페이지**: http://localhost:8080/madang/
- **도서 목록**: http://localhost:8080/madang/books.html
- **고객 로그인**: http://localhost:8080/madang/customer-login.html
- **관리자**: http://localhost:8080/madang/admin/

### 5.4 로그 모니터링

```bash
# Tomcat 로그
tail -f $CATALINA_HOME/logs/catalina.out

# 애플리케이션 로그 (있다면)
tail -f $CATALINA_HOME/logs/madang.log
```

---

## 6. 문제 해결

### 6.1 일반적인 오류

#### 오류 1: "ClassNotFoundException: jakarta.servlet.http.HttpServlet"

**원인**: Tomcat 버전이 너무 낮음 (Jakarta EE 10 이하)

**해결**:
- **Tomcat 11.0 이상 사용** (Jakarta EE 11, Servlet 6.1 - **권장**)
- Tomcat 10.1도 가능 (Jakarta EE 10, Servlet 6.0 - 일부 기능 제한)
- 또는 `javax.servlet` API로 변경 (Tomcat 9 이하)

#### 오류 2: "Port 8080 already in use"

**원인**: 포트 충돌

**해결**:

```bash
# 1. 기존 프로세스 확인
lsof -i :8080

# 2. 프로세스 종료
kill -9 <PID>

# 3. 또는 Tomcat 포트 변경
vi $CATALINA_HOME/conf/server.xml
# <Connector port="8080" → port="8081"
```

#### 오류 3: "Cannot load JDBC driver"

**원인**: MySQL Connector JAR가 없음

**해결**:

```bash
# WAR 파일에 MySQL Connector 포함 확인
unzip -l target/madang.war | grep mysql

# 없다면 WEB-INF/lib에 수동 추가
cp lib/mysql-connector-*.jar $CATALINA_HOME/webapps/madang/WEB-INF/lib/
```

#### 오류 4: "Access denied for user 'madang'@'localhost'"

**원인**: 데이터베이스 권한 문제

**해결**:

```sql
-- MySQL에서 실행
GRANT ALL PRIVILEGES ON madangdb.* TO 'madang'@'localhost' IDENTIFIED BY 'madang';
FLUSH PRIVILEGES;
```

#### 오류 5: 브라우저에서 데이터가 로드되지 않음

**증상**: 브라우저에서 "도서 데이터를 불러오지 못했습니다" 메시지 표시

**원인**: JavaScript API 경로가 Tomcat context path와 불일치

**해결 방법**:

1. **브라우저 개발자 도구로 확인** (F12):
   - Network 탭 확인
   - 404 에러 확인: `http://localhost:8080/api/books` (잘못됨)
   - 올바른 경로: `http://localhost:8080/madang/api/books`

2. **API_BASE_URL 수정**:

   ```bash
   # 파일 위치
   vi src/main/webapp/js/api.js
   ```

   ```javascript
   // 수정 전 (잘못된 설정)
   const API_BASE_URL = '';

   // 수정 후 (올바른 설정)
   const API_BASE_URL = '/madang';  // WAR 파일명과 일치
   ```

3. **재빌드 및 배포**:

   ```bash
   # Maven 빌드
   mvn clean package

   # 기존 배포 제거
   rm -rf $CATALINA_HOME/webapps/madang*

   # 새 WAR 배포
   cp target/madang.war $CATALINA_HOME/webapps/

   # 배포 완료까지 대기 (자동 배포)
   # 또는 Tomcat 재시작
   $CATALINA_HOME/bin/shutdown.sh
   $CATALINA_HOME/bin/startup.sh
   ```

4. **브라우저 캐시 삭제**:
   - **Windows/Linux**: Ctrl+Shift+R
   - **macOS**: Cmd+Shift+R

**참고**:
- WAR 파일명이 context path가 됨 (`madang.war` → `/madang`)
- ROOT.war로 배포하면 context path가 `/`가 되어 `API_BASE_URL = ''`로 설정 가능
- context path 변경 시 JavaScript도 함께 수정 필요

### 6.2 로그 레벨 조정

`$CATALINA_HOME/conf/logging.properties` 수정:

```properties
# 상세 로그 출력
.level = FINE
com.madang.level = FINE
```

### 6.3 Hot Reload 설정

개발 중 자동 재배포 설정:

```xml
<!-- $CATALINA_HOME/conf/Catalina/localhost/madang.xml -->
<Context reloadable="true">
    <!-- ... -->
</Context>
```

### 6.4 메모리 설정

`$CATALINA_HOME/bin/setenv.sh` 생성:

```bash
#!/bin/bash
export CATALINA_OPTS="$CATALINA_OPTS -Xms512m -Xmx1024m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:MaxPermSize=256m"
```

---

## 7. 프로덕션 배포 (추가 사항)

### 7.1 보안 설정

```bash
# 1. Tomcat 관리자 비밀번호 설정
vi $CATALINA_HOME/conf/tomcat-users.xml

# 2. 불필요한 애플리케이션 제거
rm -rf $CATALINA_HOME/webapps/docs
rm -rf $CATALINA_HOME/webapps/examples
rm -rf $CATALINA_HOME/webapps/host-manager
rm -rf $CATALINA_HOME/webapps/manager  # 필요 시 유지

# 3. 포트 변경 (선택사항)
vi $CATALINA_HOME/conf/server.xml
```

### 7.2 HTTPS 설정

```bash
# 1. SSL 인증서 생성
keytool -genkey -alias tomcat -keyalg RSA -keystore tomcat.keystore

# 2. server.xml에 Connector 추가
```

```xml
<Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
           maxThreads="150" scheme="https" secure="true"
           keystoreFile="/opt/tomcat/tomcat.keystore"
           keystorePass="changeit"
           clientAuth="false" sslProtocol="TLS"/>
```

### 7.3 systemd 서비스 등록

`/etc/systemd/system/tomcat.service` 생성:

```ini
[Unit]
Description=Apache Tomcat Web Application Container
After=network.target

[Service]
Type=forking

Environment=JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
Environment=CATALINA_PID=/opt/tomcat/temp/tomcat.pid
Environment=CATALINA_HOME=/opt/tomcat
Environment=CATALINA_BASE=/opt/tomcat
Environment='CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC'

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh

User=tomcat
Group=tomcat
UMask=0007
RestartSec=10
Restart=always

[Install]
WantedBy=multi-user.target
```

서비스 시작:

```bash
sudo systemctl daemon-reload
sudo systemctl enable tomcat
sudo systemctl start tomcat
sudo systemctl status tomcat
```

---

## 8. 참고 자료

### 8.1 공식 문서

**Jakarta EE & Servlet**:
- [Jakarta EE 11 Platform](https://jakarta.ee/specifications/platform/11/)
- [Jakarta Servlet 6.1 Specification](https://jakarta.ee/specifications/servlet/6.1/)
- [What's New in Servlet 6.1](https://jakarta.ee/specifications/servlet/6.1/jakarta-servlet-spec-6.1.html#_what_s_new_in_servlet_6_1)

**Apache Tomcat**:
- [Apache Tomcat 11.0 Documentation](https://tomcat.apache.org/tomcat-11.0-doc/)
- [Tomcat Version Specifications](https://tomcat.apache.org/whichversion.html)
- [Apache Tomcat 11 다운로드](https://tomcat.apache.org/download-11.cgi)

### 8.2 내부 문서

- `docs/servlet-migration-plan.md` - Servlet 전환 계획
- `docs/backend-architecture-review.md` - 아키텍처 검토
- `CLAUDE.md` - 프로젝트 개요

### 8.3 도움말

```bash
# Tomcat 버전 확인
$CATALINA_HOME/bin/version.sh

# 실행 중인 Tomcat 확인
ps aux | grep tomcat

# 포트 사용 확인
netstat -tuln | grep 8080
```

---

## 9. 빠른 참조

### Tomcat 기본 명령어

```bash
# 시작
$CATALINA_HOME/bin/startup.sh

# 중지
$CATALINA_HOME/bin/shutdown.sh

# 재시작
$CATALINA_HOME/bin/shutdown.sh && $CATALINA_HOME/bin/startup.sh

# 로그 확인
tail -f $CATALINA_HOME/logs/catalina.out
```

### WAR 배포 명령어

```bash
# 빌드
mvn clean package

# 배포
cp target/madang.war $CATALINA_HOME/webapps/

# 언디플로이
rm -rf $CATALINA_HOME/webapps/madang*
```

### 주요 URL

- Tomcat 메인: http://localhost:8080
- 마당 서점: http://localhost:8080/madang/
- Health Check: http://localhost:8080/madang/api/health

---

**작성자**: 개발팀
**최종 수정일**: 2025-10-22
