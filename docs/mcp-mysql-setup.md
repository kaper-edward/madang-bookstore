# MCP MySQL Server 설정 가이드

## 개요

MCP (Model Context Protocol) MySQL 서버를 사용하면 Claude Code가 MySQL 데이터베이스에 직접 접근하여 쿼리를 실행하고 스키마를 분석할 수 있습니다.

**저장소**: https://github.com/benborla/mcp-server-mysql

## ⚠️ 알려진 이슈 (중요!)

**NPM 설치 시 심볼릭 링크 버그** ([GitHub Issue #74](https://github.com/benborla/mcp-server-mysql/issues/74))

현재 NPM으로 설치된 버전(2.0.4+)에는 심볼릭 링크 처리 문제가 있어 서버가 정상적으로 실행되지 않습니다. 이는 `index.ts` 386번 줄에서 심볼릭 링크를 제대로 처리하지 못해 `isMainModule` 판정이 실패하는 버그입니다.

**해결 방법은 아래 설치 섹션을 참조하세요.**

## 설치 방법

### 방법 1: 로컬 저장소 클론 (강력 권장) ⭐

메인 브랜치에서 버그가 수정되었으나 아직 NPM에 배포되지 않았습니다. 가장 안정적인 방법입니다.

```bash
# 저장소 클론
git clone https://github.com/benborla/mcp-server-mysql.git
cd mcp-server-mysql

# 의존성 설치 및 빌드
npm install
npm run build

# 전역 링크 생성
npm link
```

설치 후 `which mcp-server-mysql`로 경로 확인:
```bash
which mcp-server-mysql
# 출력 예: /home/edward/mcp-server-mysql/bin/mcp-server-mysql
```

### 방법 2: NPM 글로벌 설치 + Workaround

NPM으로 설치 후 심볼릭 링크 문제를 우회합니다.

```bash
# 설치
npm install -g @benborla29/mcp-server-mysql

# 실제 경로 확인
readlink -f $(which mcp-server-mysql)
```

**주의**: 이 방법은 Claude Code 설정 시 특별한 명령어가 필요합니다 (아래 참조).

### 방법 3: 구버전 사용 (임시 해결책)

버전 2.0.3은 심볼릭 링크 문제가 없습니다.

```bash
npm install -g @benborla29/mcp-server-mysql@2.0.3
```

**단점**: 최신 기능과 버그 수정이 반영되지 않습니다.

## Claude Code 설정

설치 방법에 따라 적절한 설정 방법을 선택하세요.

### A. 로컬 저장소 클론 방식 (방법 1로 설치한 경우) ⭐

#### A-1. 자동 설정

```bash
# npm link로 설치한 경로 확인
MYSQL_PATH=$(which mcp-server-mysql)

# Claude Code에 추가
claude mcp add mcp_server_mysql \
  -e MYSQL_HOST="localhost" \
  -e MYSQL_PORT="3306" \
  -e MYSQL_USER="madang" \
  -e MYSQL_PASS="madang" \
  -e MYSQL_DB="madangdb" \
  -e ALLOW_INSERT_OPERATION="true" \
  -e ALLOW_DELETE_OPERATION="true" \
  -e ENABLE_LOGGING="true" \
  -e MYSQL_LOG_LEVEL="info" \
  -- node $MYSQL_PATH
```

#### A-2. 수동 설정

`~/.config/claude-code/mcp.json` 파일 수정:

```json
{
  "mcpServers": {
    "mcp_server_mysql": {
      "command": "node",
      "args": ["/path/to/mcp-server-mysql/bin/mcp-server-mysql"],
      "env": {
        "MYSQL_HOST": "localhost",
        "MYSQL_PORT": "3306",
        "MYSQL_USER": "madang",
        "MYSQL_PASS": "madang",
        "MYSQL_DB": "madangdb",
        "ALLOW_INSERT_OPERATION": "true",
        "ALLOW_UPDATE_OPERATION": "false",
        "ALLOW_DELETE_OPERATION": "true",
        "MYSQL_POOL_SIZE": "10",
        "MYSQL_QUERY_TIMEOUT": "30000",
        "ENABLE_LOGGING": "true",
        "MYSQL_LOG_LEVEL": "info"
      }
    }
  }
}
```

**주의**: `/path/to/mcp-server-mysql/bin/mcp-server-mysql`를 실제 경로로 변경하세요.

### B. NPM 글로벌 설치 방식 (방법 2로 설치한 경우)

#### B-1. 자동 설정 (심볼릭 링크 우회)

```bash
# 심볼릭 링크를 실제 경로로 해석
claude mcp add mcp_server_mysql \
  -e MYSQL_HOST="localhost" \
  -e MYSQL_PORT="3306" \
  -e MYSQL_USER="madang" \
  -e MYSQL_PASS="madang" \
  -e MYSQL_DB="madangdb" \
  -e ALLOW_INSERT_OPERATION="true" \
  -e ALLOW_DELETE_OPERATION="true" \
  -e ENABLE_LOGGING="true" \
  -e MYSQL_LOG_LEVEL="info" \
  -- node $(readlink -f $(which mcp-server-mysql))
```

**핵심**: `-- node $(readlink -f $(which mcp-server-mysql))` 부분이 심볼릭 링크 문제를 해결합니다.

#### B-2. 수동 설정

```bash
# 실제 경로 확인
readlink -f $(which mcp-server-mysql)
# 출력 예: /usr/local/lib/node_modules/@benborla29/mcp-server-mysql/bin/mcp-server-mysql
```

`~/.config/claude-code/mcp.json` 파일에 위에서 확인한 실제 경로를 사용:

```json
{
  "mcpServers": {
    "mcp_server_mysql": {
      "command": "node",
      "args": ["/usr/local/lib/node_modules/@benborla29/mcp-server-mysql/bin/mcp-server-mysql"],
      "env": {
        "MYSQL_HOST": "localhost",
        "MYSQL_PORT": "3306",
        "MYSQL_USER": "madang",
        "MYSQL_PASS": "madang",
        "MYSQL_DB": "madangdb",
        "ALLOW_INSERT_OPERATION": "true",
        "ALLOW_UPDATE_OPERATION": "false",
        "ALLOW_DELETE_OPERATION": "true",
        "ENABLE_LOGGING": "true",
        "MYSQL_LOG_LEVEL": "info"
      }
    }
  }
}
```

### C. 구버전 사용 방식 (방법 3으로 설치한 경우)

```bash
# 일반적인 방법으로 추가 가능
claude mcp add mcp_server_mysql \
  -e MYSQL_HOST="localhost" \
  -e MYSQL_PORT="3306" \
  -e MYSQL_USER="madang" \
  -e MYSQL_PASS="madang" \
  -e MYSQL_DB="madangdb" \
  -e ALLOW_INSERT_OPERATION="true" \
  -e ALLOW_DELETE_OPERATION="true" \
  -- npx @benborla29/mcp-server-mysql
```

## 환경 변수 설명

### 필수 설정

| 변수 | 값 | 설명 |
|------|-----|------|
| `MYSQL_HOST` | `localhost` | MySQL 서버 주소 |
| `MYSQL_PORT` | `3306` | MySQL 포트 |
| `MYSQL_USER` | `madang` | 데이터베이스 사용자명 |
| `MYSQL_PASS` | `madang` | 데이터베이스 비밀번호 |
| `MYSQL_DB` | `madangdb` | 대상 데이터베이스 |

### 쓰기 권한 (교육용 프로젝트)

| 변수 | 값 | 설명 |
|------|-----|------|
| `ALLOW_INSERT_OPERATION` | `true` | INSERT 허용 (주문 생성) |
| `ALLOW_UPDATE_OPERATION` | `false` | UPDATE 비활성화 (미사용) |
| `ALLOW_DELETE_OPERATION` | `true` | DELETE 허용 (주문 취소) |

### 성능 최적화

| 변수 | 값 | 설명 |
|------|-----|------|
| `MYSQL_POOL_SIZE` | `10` | 커넥션 풀 크기 |
| `MYSQL_QUERY_TIMEOUT` | `30000` | 쿼리 타임아웃 (ms) |
| `MYSQL_CACHE_TTL` | `60000` | 캐시 유효기간 (ms) |

### 보안 설정

| 변수 | 값 | 설명 |
|------|-----|------|
| `MYSQL_RATE_LIMIT` | `100` | 분당 최대 쿼리 수 |
| `MYSQL_SSL` | `false` | SSL 연결 (개발: false) |

## 사용 가능한 도구

MCP MySQL 서버가 활성화되면 다음 도구를 사용할 수 있습니다:

### `mcp__mcp_server_mysql__mysql_query`

**설명**: SQL 쿼리 실행

**예제**:
```javascript
// 전체 도서 조회
SELECT * FROM Book ORDER BY bookid;

// 특정 고객의 주문 내역 (JOIN)
SELECT o.orderid, o.saleprice, o.orderdate, b.bookname
FROM Orders o
JOIN Book b ON o.bookid = b.bookid
WHERE o.custid = 1;

// 주문 생성 (INSERT)
INSERT INTO Orders (orderid, custid, bookid, saleprice, orderdate)
VALUES (
    (SELECT IFNULL(MAX(orderid), 0) + 1 FROM Orders AS o),
    1, 1, 6000, CURDATE()
);

// 주문 취소 (DELETE)
DELETE FROM Orders WHERE orderid = 1 AND custid = 1;
```

## 프로젝트별 권한 설정

각 프로젝트 디렉토리의 `.claude/settings.local.json`에서 도구 사용 권한을 관리할 수 있습니다:

```json
{
  "permissions": {
    "allow": [
      "mcp__mcp_server_mysql__mysql_query"
    ]
  }
}
```

## 활용 시나리오

### 1. 스키마 분석

```sql
-- 테이블 목록 확인
SHOW TABLES;

-- 테이블 구조 확인
DESCRIBE Book;
DESCRIBE Customer;
DESCRIBE Orders;

-- 외래키 확인
SELECT * FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'madangdb';
```

### 2. 데이터 검증

```sql
-- 데이터 개수 확인
SELECT
    (SELECT COUNT(*) FROM Book) as book_count,
    (SELECT COUNT(*) FROM Customer) as customer_count,
    (SELECT COUNT(*) FROM Orders) as order_count;

-- 외래키 무결성 확인
SELECT o.orderid FROM Orders o
LEFT JOIN Customer c ON o.custid = c.custid
WHERE c.custid IS NULL;
```

### 3. 통계 쿼리 테스트

```sql
-- 베스트셀러 조회
SELECT b.bookname, COUNT(*) as sales_count
FROM Orders o
JOIN Book b ON o.bookid = b.bookid
GROUP BY b.bookid, b.bookname
ORDER BY sales_count DESC
LIMIT 5;

-- 고객별 구매 통계
SELECT c.name, COUNT(o.orderid) as order_count,
       SUM(o.saleprice) as total_amount
FROM Customer c
LEFT JOIN Orders o ON c.custid = o.custid
GROUP BY c.custid, c.name;
```

## 문제 해결

### 1. MCP 서버가 "아무 출력 없이 종료" (가장 흔한 문제)

**증상**: Claude Code에서 mcp_server_mysql이 연결되지 않고 조용히 종료됨

**원인**: 심볼릭 링크 버그 (GitHub Issue #74)

**해결 방법**:

#### 방법 A: 로컬 저장소 클론 사용 (권장)

```bash
# 기존 NPM 설치 제거
npm uninstall -g @benborla29/mcp-server-mysql

# 저장소 클론 및 설치
git clone https://github.com/benborla/mcp-server-mysql.git
cd mcp-server-mysql
npm install
npm run build
npm link

# Claude Code 설정 재등록 (위의 A-1 참조)
```

#### 방법 B: readlink 사용 (NPM 설치 유지)

```bash
# 실제 경로 확인
readlink -f $(which mcp-server-mysql)

# 해당 경로를 ~/.config/claude-code/mcp.json에서 사용
# (위의 B-2 참조)
```

#### 방법 C: 구버전 사용

```bash
npm install -g @benborla29/mcp-server-mysql@2.0.3
```

### 2. 연결 실패 (MySQL 자체 문제)

```bash
# MySQL 서비스 상태 확인
sudo systemctl status mysql

# MySQL 접속 테스트
mysql -u madang -p madangdb

# 포트 확인
sudo netstat -tlnp | grep 3306
```

### 3. 권한 문제

```sql
-- MySQL에서 사용자 권한 확인
SHOW GRANTS FOR 'madang'@'localhost';

-- 필요시 권한 부여
GRANT ALL PRIVILEGES ON madangdb.* TO 'madang'@'localhost';
FLUSH PRIVILEGES;
```

### 4. MCP 서버 상태 확인

```bash
# Claude Code 로그 확인
tail -f ~/.claude/debug/*.txt

# mcp.json 설정 확인
cat ~/.config/claude-code/mcp.json | jq '.mcpServers.mcp_server_mysql'
```

### 5. MCP 서버 재시작

```bash
# Claude Code 완전히 종료
pkill -f claude

# Claude Code 재시작
claude

# 또는 VS Code에서 Claude Code 확장 재시작
```

### 6. 환경 변수 문제

```bash
# 환경 변수가 제대로 전달되는지 확인
# mcp.json의 env 섹션을 점검
cat ~/.config/claude-code/mcp.json

# 비밀번호 특수문자 문제 시 따옴표로 감싸기
"MYSQL_PASS": "your'password\"here"
```

### 7. 디버깅 모드 활성화

`~/.config/claude-code/mcp.json`에서:

```json
{
  "mcpServers": {
    "mcp_server_mysql": {
      "command": "node",
      "args": ["/path/to/mcp-server-mysql"],
      "env": {
        "MYSQL_LOG_LEVEL": "debug",
        "ENABLE_LOGGING": "true"
      }
    }
  }
}
```

### 8. 수동 테스트

MCP 서버를 직접 실행하여 문제 확인:

```bash
# 환경 변수 설정
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_USER=madang
export MYSQL_PASS=madang
export MYSQL_DB=madangdb

# 직접 실행
node /path/to/mcp-server-mysql/bin/mcp-server-mysql

# 또는
npx @benborla29/mcp-server-mysql
```

## 보안 주의사항

⚠️ **교육용 설정입니다!**

- `ALLOW_INSERT_OPERATION=true`: 주문 생성 기능에 필요
- `ALLOW_DELETE_OPERATION=true`: 주문 취소 기능에 필요
- **프로덕션 환경에서는 절대 사용하지 마세요**
- 비밀번호는 반드시 .env 파일에 저장하고 .gitignore에 추가

## 참고 자료

- [MCP MySQL Server GitHub](https://github.com/benborla/mcp-server-mysql)
- [Model Context Protocol 공식 문서](https://modelcontextprotocol.io/)
- [Claude Code MCP 가이드](https://docs.claude.com/claude-code/mcp)
