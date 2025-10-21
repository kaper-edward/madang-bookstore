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

# Run server with environment variables (recommended)
./start-server.sh

# Or run server directly (uses default values from DBConnection.java)
java -cp "bin:lib/*" com.madang.server.MadangServer
```

### Database Connection

**Connection Details** (DBConnection.java):

DBConnection.java supports environment variables for secure credential management:

**Environment Variables** (recommended for production):
- `DB_URL` - Database URL (default: `jdbc:mysql://localhost:3306/madangdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`)
- `DB_USER` - Database username (default: `madang`)
- `DB_PASSWORD` - Database password (default: `madang`)

**Setup**:
1. Copy `.env.example` to `.env`
2. Update `.env` with your actual credentials
3. Run server with `./start-server.sh` (automatically loads `.env`)

**Direct credentials** (for development only):
- URL: `jdbc:mysql://localhost:3306/madangdb`
- User: `madang`
- Password: `madang`

Test database connectivity:
```bash
# Use MCP MySQL server tools or direct MySQL client
mysql -u madang -p madangdb
```

**Security Note**: `.env` file is in `.gitignore` and will not be committed to Git.

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
- `DBConnection.java` - Manages JDBC connections with HikariCP Connection Pool (max 10 connections, min 2 idle)
- `SessionManager.java` - In-memory session management for user authentication (2-hour timeout)
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
  - Allowed values: 'customer', 'publisher', 'admin', 'manager'
  - CHECK constraint: `chk_customer_role`

**Orders**:
- orderid INT PRIMARY KEY AUTO_INCREMENT
- custid INT NOT NULL (FK → Customer.custid)
- bookid INT NOT NULL (FK → Book.bookid)
- saleprice INT NOT NULL DEFAULT 0
- orderdate DATE NOT NULL DEFAULT CURDATE()

Foreign key constraints enforced. No additional tables or columns allowed.

## Security Considerations

**Implemented Security Features**:
- SQL injection prevention: PreparedStatement only
- XSS prevention: Frontend uses textContent over innerHTML where possible
- **Server-side session management**: In-memory SessionManager with 2-hour timeout
- **Session-based authentication**: X-Session-Id header with UUID tokens
- **Server-side role verification**: ApiHandler provides requireAdmin(), requireRole(), requireSelfOrAdmin() methods
- Connection pool security: HikariCP with connection limits
- Environment variable support: Credentials stored in .env (Git-ignored)
- CORS: Permissive for development (includes X-Session-Id header)

**Production Upgrades** (not yet implemented):
- Password hashing (BCrypt) - currently uses simple custid-based login
- Persistent session storage (Redis/Database) - currently in-memory only
- JWT authentication - alternative to current session approach
- HTTPS
- CSRF tokens
- Input validation strengthening
- Rate limiting

## Role-Based Access Control (RBAC)

The system implements role-based access control through the `role` field in the Customer table.

### Defined Roles

| Role | Description | Key Permissions |
|------|-------------|-----------------|
| **customer** | Default role for all users | Browse books, place orders, view own orders |
| **manager** | Operations manager | All customer permissions + book management, all orders, statistics |
| **publisher** | Publishing company staff | Customer permissions + manage own publisher's books (partially implemented) |
| **admin** | System administrator | Full system access, user management, role assignment |

### Admin Accounts

Test accounts for development:
- **ID 1** (박지성): admin
- **ID 2** (김연아): admin
- **ID 3-5** (김연경, 추신수, 박세리): manager

### Role Implementation

**Frontend**: Role-based UI rendering using `localStorage.getItem('role')`
```javascript
const userRole = localStorage.getItem('role');
if (userRole === 'admin' || userRole === 'manager') {
    // Show admin features
}
```

**Backend** (recommended for production):
```java
// Add role validation in handlers
String role = params.get("role");
if (!"admin".equals(role) && !"manager".equals(role)) {
    return errorResponse("Unauthorized", 403);
}
```

**Important**: Current implementation uses client-side role checking for simplicity. Production systems should validate roles server-side.

See `docs/rbac.md` for complete documentation.

## File Generation Scripts

Located in `gen/` directory - Python scripts for generating test data and managing roles:

**Data Generation**:
- `gen_book_mysql.py` - Generate book test data
- `gen_cust_mysql.py` - Generate customer test data
- `gen_orders_mysql.py` - Generate order test data

**Role Management**:
- `alter_role_constraint.py` - Modify Customer table CHECK constraint for roles
- `update_roles.py` - Assign admin/manager roles to specific users

Usage examples:
```bash
# Generate 1000 books
python3 gen/gen_book_mysql.py -n 1000

# Generate 100 customers
python3 gen/gen_cust_mysql.py -n 100

# Generate 2000 orders
python3 gen/gen_orders_mysql.py -n 2000

# Update user roles
python3 gen/update_roles.py
```

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
- Role-Based Access Control: `docs/rbac.md`
- SQL learning queries: `sql/queries.sql`
