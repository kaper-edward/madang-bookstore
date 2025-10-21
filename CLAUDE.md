# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Educational online bookstore system ("Madang Bookstore") for learning CRUD SQL operations with a functional web application. Uses Java with built-in HTTP server (no Tomcat required), JDBC, and vanilla JavaScript frontend.

**Target Audience**: Students learning SQL and basic web development
**Database**: MySQL (`madangdb`) with 3 tables: Book, Customer, Orders (schema must not be modified)

## Technology Stack

- **Backend**: Java 21+ (uses `com.sun.net.httpserver.HttpServer`)
- **Database**: MySQL 5.7+ via JDBC (mysql-connector-java)
- **Frontend**: Vanilla JavaScript (ES6+), HTML5, utility-first CSS (Tailwind-style)
- **Server**: Java built-in HTTP server on port 8080 (educational alternative to Tomcat)

## Build and Run Commands

### Compile and Start Server

```bash
# Compile all Java files
javac -d bin -cp "lib/*" $(find src -name "*.java")

# Run server (from project root)
java -cp "bin:lib/*" com.madang.server.MadangServer
```

### Database Connection

**Connection Details** (DBConnection.java):
- URL: `jdbc:mysql://localhost:3306/madangdb`
- User: `madang`
- Password: `madang`

Test database connectivity:
```bash
# Use MCP MySQL server tools or direct MySQL client
mysql -u madang -p madangdb
```

### MCP MySQL Server (Optional Development Tool)

For database schema inspection and query testing during development, you can use the MCP MySQL server.

**Important**: Due to a known bug ([GitHub Issue #74](https://github.com/benborla/mcp-server-mysql/issues/74)), **clone the repository instead of using NPM**:

```bash
# Clone and build
git clone https://github.com/benborla/mcp-server-mysql.git
cd mcp-server-mysql
npm install && npm run build && npm link

# Add to Claude Code
claude mcp add mcp_server_mysql \
  -e MYSQL_HOST="localhost" \
  -e MYSQL_USER="madang" \
  -e MYSQL_PASS="madang" \
  -e MYSQL_DB="madangdb" \
  -e ALLOW_INSERT_OPERATION="true" \
  -e ALLOW_DELETE_OPERATION="true" \
  -- node $(which mcp-server-mysql)
```

**Alternative (NPM with workaround)**:
```bash
npm install -g @benborla29/mcp-server-mysql
claude mcp add mcp_server_mysql [...env vars...] \
  -- node $(readlink -f $(which mcp-server-mysql))
```

See `docs/mcp-mysql-setup.md` for detailed setup instructions.

### Access Application

- Frontend: `http://localhost:8080/index.html`
- API Test: `http://localhost:8080/api/test`

## Architecture Overview

### Three-Tier Architecture

```
Frontend (HTML/CSS/JS)
    ↓ HTTP REST API
Backend (Java HTTP Server + Handlers)
    ↓ JDBC
Database (MySQL madangdb)
```

### Key Components

**Server Layer** (`src/com/madang/server/`):
- `MadangServer.java` - Main server class, registers routes, serves static files
- `ApiHandler.java` - Abstract base class for all API handlers with HTTP method routing, CORS, JSON response utilities

**Handler Layer** (`src/com/madang/handler/`):
- Extend `ApiHandler` and implement `handleGet()`, `handlePost()`, `handleDelete()`
- `BookHandler`, `CustomerHandler`, `OrderHandler`, `StatsHandler`
- Each handler maps to `/api/{resource}` endpoints

**DAO Layer** (`src/com/madang/dao/`):
- Database access using PreparedStatement (SQL injection prevention)
- `BookDAO`, `CustomerDAO`, `OrderDAO`
- All SQL queries are in `sql/queries.sql` for reference

**Model Layer** (`src/com/madang/model/`):
- Simple POJOs: `Book`, `Customer`, `Order`
- Match database table structure exactly

**Utility** (`src/com/madang/util/`):
- `DBConnection.java` - Manages JDBC connections
- `SqlLogger.java` - Optional SQL query logging

### Frontend Structure

```
frontend/
├── index.html           # Main landing page
├── customer-login.html  # Customer selection (simple auth)
├── books.html          # Book listing with search/filter
├── book-detail.html    # Book details + purchase
├── order.html          # Order confirmation
├── my-orders.html      # Order history
├── dashboard.html      # Admin statistics dashboard
├── css/styles.css      # Utility-first CSS
└── js/
    ├── api.js          # Shared API utilities (fetchAPI, CORS)
    ├── books.js
    ├── orders.js
    ├── customers.js
    ├── login.js
    └── dashboard.js
```

## Important Design Patterns

### Handler Pattern

All API handlers follow this structure:

```java
public class ExampleHandler extends ApiHandler {
    @Override
    protected String handleGet(Map<String, String> params) throws Exception {
        String action = params.get("action");

        if ("list".equals(action)) {
            // Call DAO, return JSON
            return successResponse(data);
        }

        return errorResponse("Invalid action");
    }
}
```

### DAO Pattern

All database operations use PreparedStatement:

```java
public List<Book> getAllBooks() throws SQLException {
    String sql = "SELECT bookid, bookname, publisher, price FROM Book ORDER BY bookid";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {
        // Process results
    }
}
```

### API Response Format

**Success**:
```json
{
  "success": true,
  "data": [...],
  "message": "성공"
}
```

**Error**:
```json
{
  "success": false,
  "error": "에러 메시지",
  "code": 400
}
```

## CRUD SQL Learning Mapping

This project teaches SQL through practical implementation:

- **CREATE (INSERT)**: Order creation - `OrderDAO.createOrder()`
- **READ (SELECT)**: Books list, customer orders with JOINs - all DAOs
- **UPDATE**: Not implemented (students haven't learned yet)
- **DELETE**: Order cancellation - `OrderDAO.deleteOrder()`

**Advanced SQL**:
- JOINs: Customer orders with book details
- GROUP BY: Statistics by customer/publisher/book
- Aggregate functions: COUNT, SUM, AVG, MAX, MIN
- Subqueries: Best sellers, unused books/customers

See `sql/queries.sql` for comprehensive SQL examples.

## Common Development Tasks

### Adding a New API Endpoint

1. Create handler in `src/com/madang/handler/` extending `ApiHandler`
2. Implement required methods (`handleGet`, `handlePost`, etc.)
3. Register in `MadangServer.registerApiRoutes()`:
   ```java
   server.createContext("/api/newresource", new NewResourceHandler());
   ```

### Adding a New DAO Method

1. Write SQL query following PreparedStatement pattern
2. Add to appropriate DAO class
3. Use `DBConnection.getConnection()` for connections
4. Always close resources in try-with-resources or finally block

### Debugging Database Issues

- Check `DBConnection.java` for connection parameters
- Verify MySQL service is running: `systemctl status mysql`
- Test connection: Add `DBConnection.testConnection()` call in handler
- Enable SQL logging via `SqlLogger` if available

### Frontend-Backend Integration

- API base URL configured in `frontend/js/api.js`
- All API calls use `fetchAPI(endpoint, options)` helper
- Session management via `localStorage` (custid)
- CORS headers automatically added by `ApiHandler`

## Database Schema Constraints

**IMPORTANT**: The database schema (Book, Customer, Orders tables) cannot be modified as per educational requirements.

**Book**:
- bookid INT PRIMARY KEY AUTO_INCREMENT
- bookname VARCHAR(40) NOT NULL
- publisher VARCHAR(40) NOT NULL
- price INT NOT NULL DEFAULT 0

**Customer**:
- custid INT PRIMARY KEY AUTO_INCREMENT
- name VARCHAR(40)
- address VARCHAR(50)
- phone VARCHAR(20)
- role VARCHAR(20) NOT NULL DEFAULT 'customer'

**Orders**:
- orderid INT PRIMARY KEY AUTO_INCREMENT
- custid INT NOT NULL (FK → Customer.custid)
- bookid INT NOT NULL (FK → Book.bookid)
- saleprice INT NOT NULL DEFAULT 0
- orderdate DATE NOT NULL DEFAULT CURDATE()

Foreign key constraints enforced. No additional tables or columns allowed.

## Security Considerations

**Educational Level** (current implementation):
- SQL injection prevention: PreparedStatement only
- XSS prevention: Frontend uses textContent over innerHTML where possible
- Basic session: localStorage stores custid (not production-ready)
- CORS: Permissive for development

**Production Upgrades** (not implemented):
- Password hashing (BCrypt)
- JWT authentication
- HTTPS
- CSRF tokens
- Input validation strengthening

## File Generation Scripts

Located in `gen/` directory - Python scripts for generating test data:
- `gen_book_mysql.py` - Generate book test data
- `gen_cust_mysql.py` - Generate customer test data
- `gen_orders_mysql.py` - Generate order test data

These are utilities, not part of the main application.

## Port Configuration

If port 8080 is in use:

1. Change in `MadangServer.java`: `private static final int PORT = 8081;`
2. Update `frontend/js/api.js`: Modify API_BASE_URL to match new port
3. Recompile and restart

## Key Differences from Traditional Servlet Approach

- **No Tomcat**: Uses Java 21's built-in `HttpServer`
- **No web.xml**: Routes registered programmatically in `MadangServer.java`
- **No WAR deployment**: Run directly with `java -cp` command
- **No JSP**: Pure JSON API with vanilla JavaScript frontend
- **Simplified setup**: Single command to compile and run

Trade-off: Lower production capability but much simpler for educational purposes.

## Documentation References

- Architecture details: `docs/architecture.md`
- Page-by-page design specs: `docs/page-design.md`
- Simple server guide: `docs/simple-server-guide.md`
- SQL learning queries: `sql/queries.sql`
