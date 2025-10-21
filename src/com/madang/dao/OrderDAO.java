package com.madang.dao;

import com.madang.model.Order;
import com.madang.model.PageRequest;
import com.madang.model.PageResponse;
import com.madang.util.DBConnection;
import com.madang.util.SqlLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orders 테이블 데이터 접근 객체
 */
public class OrderDAO {

    private static final String DEFAULT_ORDER_SORT = "o.orderdate";
    private static final String DEFAULT_CUSTOMER_STATS_SORT = "totalAmount";
    private static final String DEFAULT_PUBLISHER_STATS_SORT = "totalRevenue";
    private static final String DEFAULT_BOOK_STATS_SORT = "salesCount";

    /**
     * 고객별 주문 내역 조회 (정렬 지원)
     */
    public List<Order> getOrdersByCustomer(int custId, String sortBy, String direction) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.orderid, o.custid, o.bookid, o.saleprice, o.orderdate, " +
                     "b.bookname, b.publisher, b.price AS listPrice " +
                     "FROM Orders o " +
                     "JOIN Book b ON o.bookid = b.bookid " +
                     "WHERE o.custid = ? " +
                     "ORDER BY " + resolveOrderSortColumn(sortBy) + " " + resolveDirection(direction);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, custId);
            SqlLogger.logQuery(sql, custId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setOrderid(rs.getInt("orderid"));
                order.setCustid(rs.getInt("custid"));
                order.setBookid(rs.getInt("bookid"));
                order.setSaleprice(rs.getInt("saleprice"));
                order.setOrderdate(rs.getDate("orderdate"));
                order.setBookname(rs.getString("bookname"));
                order.setPublisher(rs.getString("publisher"));
                order.setListPrice(rs.getInt("listPrice"));
                orders.add(order);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return orders;
    }

    /**
     * 고객별 주문 내역 조회 (페이징 지원)
     */
    public PageResponse<Order> getOrdersByCustomerPaged(PageRequest pageRequest, int custId) throws SQLException {
        List<Order> orders = new ArrayList<>();

        // 1. 전체 개수 조회
        long totalItems = countOrdersByCustomer(custId);

        // 2. 페이징된 데이터 조회
        String sql = "SELECT o.orderid, o.custid, o.bookid, o.saleprice, o.orderdate, " +
                     "b.bookname, b.publisher, b.price AS listPrice " +
                     "FROM Orders o " +
                     "JOIN Book b ON o.bookid = b.bookid " +
                     "WHERE o.custid = ? " +
                     "ORDER BY " + resolveOrderSortColumn(pageRequest.getSortBy()) + " " +
                     resolveDirection(pageRequest.getDirection()) +
                     " LIMIT ? OFFSET ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, custId);
            pstmt.setInt(2, pageRequest.getPageSize());
            pstmt.setInt(3, pageRequest.getOffset());

            SqlLogger.logQuery(sql, custId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setOrderid(rs.getInt("orderid"));
                order.setCustid(rs.getInt("custid"));
                order.setBookid(rs.getInt("bookid"));
                order.setSaleprice(rs.getInt("saleprice"));
                order.setOrderdate(rs.getDate("orderdate"));
                order.setBookname(rs.getString("bookname"));
                order.setPublisher(rs.getString("publisher"));
                order.setListPrice(rs.getInt("listPrice"));
                orders.add(order);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        // 3. PageResponse 생성
        return new PageResponse<>(
            orders,
            pageRequest.getPage(),
            pageRequest.getPageSize(),
            totalItems
        );
    }

    /**
     * 고객별 주문 개수 조회
     */
    private long countOrdersByCustomer(int custId) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM Orders WHERE custid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, custId);

            SqlLogger.logQuery(sql, custId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("total");
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return 0;
    }

    /**
     * 주문 ID로 조회
     */
    public Order getOrderById(int orderId) throws SQLException {
        String sql = "SELECT orderid, custid, bookid, saleprice, orderdate FROM Orders WHERE orderid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            SqlLogger.logQuery(sql, orderId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Order order = new Order();
                order.setOrderid(rs.getInt("orderid"));
                order.setCustid(rs.getInt("custid"));
                order.setBookid(rs.getInt("bookid"));
                order.setSaleprice(rs.getInt("saleprice"));
                order.setOrderdate(rs.getDate("orderdate"));
                return order;
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return null;
    }

    /**
     * 다음 주문 ID 가져오기
     */
    public int getNextOrderId(Connection conn) throws SQLException {
        String sql = "SELECT IFNULL(MAX(orderid), 0) + 1 AS next_orderid FROM Orders";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            SqlLogger.logQuery(sql);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("next_orderid");
                }
            }
        }
        return 1;
    }

    /**
     * 주문 생성 (INSERT)
     */
    public int createOrder(int custId, int bookId, int salePrice) throws SQLException {
        String sql = "INSERT INTO Orders (orderid, custid, bookid, saleprice, orderdate) " +
                     "VALUES (?, ?, ?, ?, CURDATE())";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            int nextOrderId = getNextOrderId(conn);

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, nextOrderId);
            pstmt.setInt(2, custId);
            pstmt.setInt(3, bookId);
            pstmt.setInt(4, salePrice);

            SqlLogger.logUpdate(sql, nextOrderId, custId, bookId, salePrice);
            int result = pstmt.executeUpdate();
            if (result > 0) {
                return nextOrderId;
            }
        } finally {
            DBConnection.close(conn, pstmt);
        }

        return -1;
    }

    /**
     * 주문 금액 수정
     */
    public boolean updateOrderPrice(int orderId, int custId, int salePrice) throws SQLException {
        String sql = "UPDATE Orders SET saleprice = ? WHERE orderid = ? AND custid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, salePrice);
            pstmt.setInt(2, orderId);
            pstmt.setInt(3, custId);
            SqlLogger.logUpdate(sql, salePrice, orderId, custId);
            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            DBConnection.close(conn, pstmt);
        }
    }

    /**
     * 주문 취소 (DELETE)
     */
    public boolean deleteOrder(int orderId, int custId) throws SQLException {
        String sql = "DELETE FROM Orders WHERE orderid = ? AND custid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, custId);
            SqlLogger.logUpdate(sql, orderId, custId);
            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            DBConnection.close(conn, pstmt);
        }
    }

    /**
     * 고객별 주문 통계
     */
    public Map<String, Object> getCustomerOrderStats(int custId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT COUNT(*) as orderCount, " +
                     "IFNULL(SUM(saleprice), 0) as totalAmount, " +
                     "IFNULL(AVG(saleprice), 0) as avgAmount " +
                     "FROM Orders WHERE custid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, custId);
            SqlLogger.logQuery(sql, custId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                stats.put("orderCount", rs.getInt("orderCount"));
                stats.put("totalAmount", rs.getInt("totalAmount"));
                stats.put("avgAmount", rs.getDouble("avgAmount"));
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return stats;
    }

    /**
     * 전체 통계 (대시보드용)
     */
    public Map<String, Object> getOverallStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT " +
                     "(SELECT COUNT(*) FROM Book) as totalBooks, " +
                     "(SELECT COUNT(*) FROM Customer) as totalCustomers, " +
                     "(SELECT COUNT(*) FROM Orders) as totalOrders, " +
                     "(SELECT IFNULL(SUM(saleprice), 0) FROM Orders) as totalRevenue, " +
                     "(SELECT IFNULL(AVG(saleprice), 0) FROM Orders) as avgSalePrice";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            SqlLogger.logQuery(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                stats.put("totalBooks", rs.getInt("totalBooks"));
                stats.put("totalCustomers", rs.getInt("totalCustomers"));
                stats.put("totalOrders", rs.getInt("totalOrders"));
                stats.put("totalRevenue", rs.getInt("totalRevenue"));
                stats.put("avgSalePrice", rs.getDouble("avgSalePrice"));
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return stats;
    }

    /**
     * 베스트셀러 조회 (GROUP BY)
     */
    public List<Map<String, Object>> getBestsellers(int limit) throws SQLException {
        List<Map<String, Object>> bestsellers = new ArrayList<>();
        String sql = "SELECT b.bookname, b.publisher, COUNT(*) as salesCount " +
                     "FROM Orders o " +
                     "JOIN Book b ON o.bookid = b.bookid " +
                     "GROUP BY b.bookid, b.bookname, b.publisher " +
                     "ORDER BY salesCount DESC " +
                     "LIMIT ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            SqlLogger.logQuery(sql, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> book = new HashMap<>();
                book.put("bookname", rs.getString("bookname"));
                book.put("publisher", rs.getString("publisher"));
                book.put("salesCount", rs.getInt("salesCount"));
                bestsellers.add(book);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return bestsellers;
    }

    /**
     * 주간 베스트셀러 (지난 7일)
     */
    public List<Map<String, Object>> getWeeklyBestsellers(int limit) throws SQLException {
        List<Map<String, Object>> bestsellers = new ArrayList<>();
        String sql = "SELECT b.bookname, b.publisher, COUNT(*) as salesCount " +
                     "FROM Orders o " +
                     "JOIN Book b ON o.bookid = b.bookid " +
                     "WHERE o.orderdate >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                     "GROUP BY b.bookid, b.bookname, b.publisher " +
                     "ORDER BY salesCount DESC " +
                     "LIMIT ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            SqlLogger.logQuery(sql, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> book = new HashMap<>();
                book.put("bookname", rs.getString("bookname"));
                book.put("publisher", rs.getString("publisher"));
                book.put("salesCount", rs.getInt("salesCount"));
                bestsellers.add(book);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return bestsellers;
    }

    /**
     * 최근 주문 조회
     */
    public List<Map<String, Object>> getRecentOrders(int limit, String sortBy, String direction) throws SQLException {
        List<Map<String, Object>> orders = new ArrayList<>();
        String sql = "SELECT o.orderdate, c.name as customerName, b.bookname, o.saleprice " +
                     "FROM Orders o " +
                     "JOIN Customer c ON o.custid = c.custid " +
                     "JOIN Book b ON o.bookid = b.bookid " +
                     "ORDER BY " + resolveRecentSortColumn(sortBy) + " " + resolveDirection(direction) +
                     " LIMIT ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            SqlLogger.logQuery(sql, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> order = new HashMap<>();
                order.put("orderdate", rs.getDate("orderdate").toString());
                order.put("customerName", rs.getString("customerName"));
                order.put("bookname", rs.getString("bookname"));
                order.put("saleprice", rs.getInt("saleprice"));
                orders.add(order);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return orders;
    }

    /**
     * 고객별 통계
     */
    public List<Map<String, Object>> getStatsByCustomer(String sortBy, String direction) throws SQLException {
        List<Map<String, Object>> stats = new ArrayList<>();
        String sql = "SELECT c.name, " +
                     "COUNT(o.orderid) as orderCount, " +
                     "IFNULL(SUM(o.saleprice), 0) as totalAmount, " +
                     "IFNULL(AVG(o.saleprice), 0) as avgAmount " +
                     "FROM Customer c " +
                     "LEFT JOIN Orders o ON c.custid = o.custid " +
                     "GROUP BY c.custid, c.name " +
                     "ORDER BY " + resolveCustomerStatsSort(sortBy) + " " + resolveDirection(direction);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            SqlLogger.logQuery(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("name", rs.getString("name"));
                stat.put("orderCount", rs.getInt("orderCount"));
                stat.put("totalAmount", rs.getInt("totalAmount"));
                stat.put("avgAmount", rs.getDouble("avgAmount"));
                stats.add(stat);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return stats;
    }

    /**
     * 출판사별 통계
     */
    public List<Map<String, Object>> getStatsByPublisher(String sortBy, String direction) throws SQLException {
        List<Map<String, Object>> stats = new ArrayList<>();
        String sql = "SELECT b.publisher, " +
                     "COUNT(DISTINCT b.bookid) as bookCount, " +
                     "COUNT(o.orderid) as salesCount, " +
                     "IFNULL(SUM(o.saleprice), 0) as totalRevenue " +
                     "FROM Book b " +
                     "LEFT JOIN Orders o ON b.bookid = o.bookid " +
                     "GROUP BY b.publisher " +
                     "ORDER BY " + resolvePublisherStatsSort(sortBy) + " " + resolveDirection(direction);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            SqlLogger.logQuery(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("publisher", rs.getString("publisher"));
                stat.put("bookCount", rs.getInt("bookCount"));
                stat.put("salesCount", rs.getInt("salesCount"));
                stat.put("totalRevenue", rs.getInt("totalRevenue"));
                stats.add(stat);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return stats;
    }

    /**
     * 도서별 판매 현황
     */
    public List<Map<String, Object>> getStatsByBook(String sortBy, String direction) throws SQLException {
        List<Map<String, Object>> stats = new ArrayList<>();
        String sql = "SELECT b.bookname, b.publisher, b.price, " +
                     "COUNT(o.orderid) as salesCount, " +
                     "IFNULL(AVG(o.saleprice), 0) as avgSalePrice " +
                     "FROM Book b " +
                     "LEFT JOIN Orders o ON b.bookid = o.bookid " +
                     "GROUP BY b.bookid, b.bookname, b.publisher, b.price " +
                     "ORDER BY " + resolveBookStatsSort(sortBy) + " " + resolveDirection(direction);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            SqlLogger.logQuery(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("bookname", rs.getString("bookname"));
                stat.put("publisher", rs.getString("publisher"));
                stat.put("price", rs.getInt("price"));
                stat.put("salesCount", rs.getInt("salesCount"));
                stat.put("avgSalePrice", rs.getDouble("avgSalePrice"));
                stats.add(stat);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return stats;
    }

    private String resolveOrderSortColumn(String sortBy) {
        if (sortBy == null) return DEFAULT_ORDER_SORT;
        return switch (sortBy.toLowerCase()) {
            case "orderid" -> "o.orderid";
            case "orderdate" -> "o.orderdate";
            case "bookname" -> "b.bookname";
            case "publisher" -> "b.publisher";
            case "saleprice" -> "o.saleprice";
            default -> DEFAULT_ORDER_SORT;
        };
    }

    private String resolveRecentSortColumn(String sortBy) {
        if (sortBy == null) return "o.orderdate";
        return switch (sortBy.toLowerCase()) {
            case "orderdate" -> "o.orderdate";
            case "customername" -> "c.name";
            case "bookname" -> "b.bookname";
            case "saleprice" -> "o.saleprice";
            default -> "o.orderdate";
        };
    }

    private String resolveCustomerStatsSort(String sortBy) {
        if (sortBy == null) return DEFAULT_CUSTOMER_STATS_SORT;
        return switch (sortBy.toLowerCase()) {
            case "name" -> "c.name";
            case "ordercount" -> "orderCount";
            case "totalamount" -> "totalAmount";
            case "avgamount" -> "avgAmount";
            default -> DEFAULT_CUSTOMER_STATS_SORT;
        };
    }

    private String resolvePublisherStatsSort(String sortBy) {
        if (sortBy == null) return DEFAULT_PUBLISHER_STATS_SORT;
        return switch (sortBy.toLowerCase()) {
            case "publisher" -> "b.publisher";
            case "bookcount" -> "bookCount";
            case "salescount" -> "salesCount";
            case "totalrevenue" -> "totalRevenue";
            default -> DEFAULT_PUBLISHER_STATS_SORT;
        };
    }

    private String resolveBookStatsSort(String sortBy) {
        if (sortBy == null) return DEFAULT_BOOK_STATS_SORT;
        return switch (sortBy.toLowerCase()) {
            case "bookname" -> "b.bookname";
            case "publisher" -> "b.publisher";
            case "price" -> "b.price";
            case "salescount" -> "salesCount";
            case "avgsaleprice" -> "avgSalePrice";
            default -> DEFAULT_BOOK_STATS_SORT;
        };
    }

    private String resolveDirection(String direction) {
        if (direction == null) return "DESC";
        return "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC";
    }

    /**
     * 월별 판매 통계 조회 (최근 N개월)
     * 대시보드 차트용
     */
    public List<Map<String, Object>> getMonthlySales(int months) throws SQLException {
        List<Map<String, Object>> stats = new ArrayList<>();

        String sql = "SELECT " +
                     "DATE_FORMAT(orderdate, '%Y-%m') AS month, " +
                     "COUNT(*) AS orderCount, " +
                     "SUM(saleprice) AS totalRevenue, " +
                     "AVG(saleprice) AS avgPrice " +
                     "FROM Orders " +
                     "WHERE orderdate >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                     "GROUP BY DATE_FORMAT(orderdate, '%Y-%m') " +
                     "ORDER BY month ASC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, months);

            SqlLogger.logQuery(sql, months);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("month", rs.getString("month"));
                row.put("orderCount", rs.getInt("orderCount"));
                row.put("totalRevenue", rs.getInt("totalRevenue"));
                row.put("avgPrice", rs.getDouble("avgPrice"));
                stats.add(row);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return stats;
    }

    /**
     * 고객 세그먼트 분석 (구매 금액별)
     * 대시보드 차트용
     */
    public List<Map<String, Object>> getCustomerSegments() throws SQLException {
        List<Map<String, Object>> segments = new ArrayList<>();

        String sql = "SELECT " +
                     "CASE " +
                     "    WHEN totalAmount >= 100000 THEN 'VIP' " +
                     "    WHEN totalAmount >= 50000 THEN '우수' " +
                     "    WHEN totalAmount >= 10000 THEN '일반' " +
                     "    ELSE '신규' " +
                     "END AS segment, " +
                     "COUNT(*) AS customerCount, " +
                     "SUM(totalAmount) AS totalRevenue " +
                     "FROM ( " +
                     "    SELECT c.custid, c.name, SUM(o.saleprice) AS totalAmount " +
                     "    FROM Customer c " +
                     "    LEFT JOIN Orders o ON c.custid = o.custid " +
                     "    GROUP BY c.custid, c.name " +
                     ") AS customer_totals " +
                     "GROUP BY segment " +
                     "ORDER BY FIELD(segment, 'VIP', '우수', '일반', '신규')";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            SqlLogger.logQuery(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("segment", rs.getString("segment"));
                row.put("customerCount", rs.getInt("customerCount"));
                row.put("totalRevenue", rs.getInt("totalRevenue"));
                segments.add(row);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return segments;
    }

    /**
     * 특정 월의 고객 세그먼트 분석 (해당 월까지 누적 구매 금액 기준)
     * 대시보드 차트용
     */
    public List<Map<String, Object>> getCustomerSegmentsByMonth(String month) throws SQLException {
        List<Map<String, Object>> segments = new ArrayList<>();

        // month 형식: YYYY-MM
        String sql = "SELECT " +
                     "CASE " +
                     "    WHEN totalAmount >= 100000 THEN 'VIP' " +
                     "    WHEN totalAmount >= 50000 THEN '우수' " +
                     "    WHEN totalAmount >= 10000 THEN '일반' " +
                     "    ELSE '신규' " +
                     "END AS segment, " +
                     "COUNT(*) AS customerCount, " +
                     "SUM(totalAmount) AS totalRevenue " +
                     "FROM ( " +
                     "    SELECT c.custid, c.name, IFNULL(SUM(o.saleprice), 0) AS totalAmount " +
                     "    FROM Customer c " +
                     "    LEFT JOIN Orders o ON c.custid = o.custid " +
                     "        AND DATE_FORMAT(o.orderdate, '%Y-%m') <= ? " +
                     "    GROUP BY c.custid, c.name " +
                     ") AS customer_totals " +
                     "GROUP BY segment " +
                     "ORDER BY FIELD(segment, 'VIP', '우수', '일반', '신규')";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, month);

            SqlLogger.logQuery(sql, month);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("segment", rs.getString("segment"));
                row.put("customerCount", rs.getInt("customerCount"));
                row.put("totalRevenue", rs.getInt("totalRevenue"));
                segments.add(row);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return segments;
    }

    /**
     * 특정 월의 매출 TOP 고객 조회
     * 대시보드 차트/테이블용
     */
    public List<Map<String, Object>> getTopCustomersByMonth(String month, int limit) throws SQLException {
        List<Map<String, Object>> customers = new ArrayList<>();

        // month 형식: YYYY-MM
        String sql = "SELECT c.name, COUNT(o.orderid) AS orderCount, " +
                     "IFNULL(SUM(o.saleprice), 0) AS totalAmount, " +
                     "IFNULL(AVG(o.saleprice), 0) AS avgAmount " +
                     "FROM Customer c " +
                     "INNER JOIN Orders o ON c.custid = o.custid " +
                     "WHERE DATE_FORMAT(o.orderdate, '%Y-%m') = ? " +
                     "GROUP BY c.custid, c.name " +
                     "ORDER BY totalAmount DESC " +
                     "LIMIT ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, month);
            pstmt.setInt(2, limit);

            SqlLogger.logQuery(sql, month, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("name", rs.getString("name"));
                row.put("orderCount", rs.getInt("orderCount"));
                row.put("totalAmount", rs.getInt("totalAmount"));
                row.put("avgAmount", rs.getDouble("avgAmount"));
                customers.add(row);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return customers;
    }

    /**
     * 특정 월의 출판사별 판매 현황 조회
     * 대시보드 차트용
     */
    public List<Map<String, Object>> getPublisherStatsByMonth(String month) throws SQLException {
        List<Map<String, Object>> stats = new ArrayList<>();

        // month 형식: YYYY-MM
        String sql = "SELECT b.publisher, " +
                     "COUNT(DISTINCT b.bookid) AS bookCount, " +
                     "COUNT(o.orderid) AS salesCount, " +
                     "IFNULL(SUM(o.saleprice), 0) AS totalRevenue " +
                     "FROM Book b " +
                     "LEFT JOIN Orders o ON b.bookid = o.bookid " +
                     "    AND DATE_FORMAT(o.orderdate, '%Y-%m') = ? " +
                     "GROUP BY b.publisher " +
                     "ORDER BY totalRevenue DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, month);

            SqlLogger.logQuery(sql, month);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("publisher", rs.getString("publisher"));
                row.put("bookCount", rs.getInt("bookCount"));
                row.put("salesCount", rs.getInt("salesCount"));
                row.put("totalRevenue", rs.getInt("totalRevenue"));
                stats.add(row);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return stats;
    }

    /**
     * 특정 월의 도서별 판매 통계
     */
    public List<Map<String, Object>> getBookStatsByMonth(String month) throws SQLException {
        List<Map<String, Object>> stats = new ArrayList<>();

        String sql = "SELECT b.bookname, b.publisher, b.price, " +
                     "COUNT(o.orderid) as salesCount, " +
                     "IFNULL(AVG(o.saleprice), 0) as avgSalePrice " +
                     "FROM Book b " +
                     "LEFT JOIN Orders o ON b.bookid = o.bookid " +
                     "    AND DATE_FORMAT(o.orderdate, '%Y-%m') = ? " +
                     "GROUP BY b.bookid, b.bookname, b.publisher, b.price " +
                     "ORDER BY salesCount DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, month);

            SqlLogger.logQuery(sql, month);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("bookname", rs.getString("bookname"));
                stat.put("publisher", rs.getString("publisher"));
                stat.put("price", rs.getInt("price"));
                stat.put("salesCount", rs.getInt("salesCount"));
                stat.put("avgSalePrice", rs.getDouble("avgSalePrice"));
                stats.add(stat);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return stats;
    }
}
