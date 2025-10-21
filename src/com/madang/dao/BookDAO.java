package com.madang.dao;

import com.madang.model.Book;
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
 * Book 테이블 데이터 접근 객체
 */
public class BookDAO {

    private static final String DEFAULT_SORT_COLUMN = "bookid";

    /**
     * 조건에 맞는 도서 목록 조회 (검색/필터/정렬)
     */
    public List<Book> getBooks(String title, String publisher, Integer minPrice, Integer maxPrice,
                               String sortBy, String direction) throws SQLException {
        List<Book> books = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT bookid, bookname, publisher, price FROM Book");

        boolean hasCondition = false;
        if (title != null && !title.isBlank()) {
            sql.append(hasCondition ? " AND" : " WHERE");
            sql.append(" bookname LIKE ?");
            params.add("%" + title.trim() + "%");
            hasCondition = true;
        }

        if (publisher != null && !publisher.isBlank()) {
            sql.append(hasCondition ? " AND" : " WHERE");
            sql.append(" publisher LIKE ?");
            params.add("%" + publisher.trim() + "%");
            hasCondition = true;
        }

        if (minPrice != null) {
            sql.append(hasCondition ? " AND" : " WHERE");
            sql.append(" price >= ?");
            params.add(minPrice);
            hasCondition = true;
        }

        if (maxPrice != null) {
            sql.append(hasCondition ? " AND" : " WHERE");
            sql.append(" price <= ?");
            params.add(maxPrice);
        }

        sql.append(" ORDER BY ")
           .append(resolveSortColumn(sortBy))
           .append(" ")
           .append(resolveSortDirection(direction));

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            SqlLogger.logQuery(sql.toString(), params.toArray());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Book book = new Book();
                book.setBookid(rs.getInt("bookid"));
                book.setBookname(rs.getString("bookname"));
                book.setPublisher(rs.getString("publisher"));
                book.setPrice(rs.getInt("price"));
                books.add(book);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return books;
    }

    /**
     * 페이지네이션이 적용된 도서 목록 조회
     *
     * @param pageRequest 페이지 요청 정보 (page, pageSize, sortBy, direction)
     * @param title 도서명 검색어 (선택)
     * @param publisher 출판사 검색어 (선택)
     * @param minPrice 최소 가격 (선택)
     * @param maxPrice 최대 가격 (선택)
     * @return 페이지네이션 응답 (데이터 목록, 페이지 정보, 전체 개수 등)
     */
    public PageResponse<Book> getBooksPaged(PageRequest pageRequest, String title, String publisher,
                                            Integer minPrice, Integer maxPrice) throws SQLException {
        List<Book> books = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        // WHERE 조건 구성
        StringBuilder whereClause = new StringBuilder();
        boolean hasCondition = false;

        if (title != null && !title.isBlank()) {
            whereClause.append(hasCondition ? " AND" : " WHERE");
            whereClause.append(" bookname LIKE ?");
            params.add("%" + title.trim() + "%");
            hasCondition = true;
        }

        if (publisher != null && !publisher.isBlank()) {
            whereClause.append(hasCondition ? " AND" : " WHERE");
            whereClause.append(" publisher LIKE ?");
            params.add("%" + publisher.trim() + "%");
            hasCondition = true;
        }

        if (minPrice != null) {
            whereClause.append(hasCondition ? " AND" : " WHERE");
            whereClause.append(" price >= ?");
            params.add(minPrice);
            hasCondition = true;
        }

        if (maxPrice != null) {
            whereClause.append(hasCondition ? " AND" : " WHERE");
            whereClause.append(" price <= ?");
            params.add(maxPrice);
        }

        // 1. 전체 개수 조회
        long totalItems = countBooks(whereClause.toString(), params);

        // 2. 페이징된 데이터 조회
        StringBuilder sql = new StringBuilder("SELECT bookid, bookname, publisher, price FROM Book");
        sql.append(whereClause);
        sql.append(" ORDER BY ")
           .append(resolveSortColumn(pageRequest.getSortBy()))
           .append(" ")
           .append(pageRequest.getDirection());
        sql.append(" LIMIT ? OFFSET ?");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            // WHERE 조건 파라미터 설정
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            // LIMIT, OFFSET 파라미터 설정
            pstmt.setInt(params.size() + 1, pageRequest.getPageSize());
            pstmt.setInt(params.size() + 2, pageRequest.getOffset());

            SqlLogger.logQuery(sql.toString(), params.toArray());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Book book = new Book();
                book.setBookid(rs.getInt("bookid"));
                book.setBookname(rs.getString("bookname"));
                book.setPublisher(rs.getString("publisher"));
                book.setPrice(rs.getInt("price"));
                books.add(book);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        // 3. PageResponse 생성
        return new PageResponse<>(books, pageRequest.getPage(), pageRequest.getPageSize(), totalItems);
    }

    /**
     * 조건에 맞는 도서 전체 개수 조회
     *
     * @param whereClause WHERE 조건절 (예: " WHERE bookname LIKE ?")
     * @param params PreparedStatement 파라미터 리스트
     * @return 전체 도서 개수
     */
    private long countBooks(String whereClause, List<Object> params) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) as total FROM Book");
        sql.append(whereClause);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            SqlLogger.logQuery(sql.toString(), params.toArray());
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
     * 도서 ID로 조회
     */
    public Book getBookById(int bookId) throws SQLException {
        String sql = "SELECT bookid, bookname, publisher, price FROM Book WHERE bookid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            SqlLogger.logQuery(sql, bookId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Book book = new Book();
                book.setBookid(rs.getInt("bookid"));
                book.setBookname(rs.getString("bookname"));
                book.setPublisher(rs.getString("publisher"));
                book.setPrice(rs.getInt("price"));
                return book;
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return null;
    }

    /**
     * 도서 등록
     * @return 생성된 bookid
     */
    public int createBook(String bookname, String publisher, int price) throws SQLException {
        String sql = "INSERT INTO Book (bookid, bookname, publisher, price) VALUES (?, ?, ?, ?)";

        System.out.println("Executing SQL: " + sql);
        System.out.println(" > Parameters: [" + bookname + ", " + publisher + ", " + price + "]");


        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            int nextId = getNextBookId(conn);

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, nextId);
            pstmt.setString(2, bookname);
            pstmt.setString(3, publisher);
            pstmt.setInt(4, price);

            SqlLogger.logUpdate(sql, nextId, bookname, publisher, price);
            int result = pstmt.executeUpdate();

            if (result > 0) {
                return nextId;
            }
        } finally {
            DBConnection.close(conn, pstmt);
        }

        return -1;
    }

    /**
     * 도서 수정
     */
    public boolean updateBook(Book book) throws SQLException {
        String sql = "UPDATE Book SET bookname = ?, publisher = ?, price = ? WHERE bookid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, book.getBookname());
            pstmt.setString(2, book.getPublisher());
            pstmt.setInt(3, book.getPrice());
            pstmt.setInt(4, book.getBookid());

            SqlLogger.logUpdate(sql, book.getBookname(), book.getPublisher(), book.getPrice(), book.getBookid());
            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            DBConnection.close(conn, pstmt);
        }
    }

    /**
     * 도서 삭제
     */
    public boolean deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM Book WHERE bookid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            SqlLogger.logUpdate(sql, bookId);
            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            DBConnection.close(conn, pstmt);
        }
    }

    /**
     * 출판사 목록 조회 (중복 제거)
     */
    public List<String> getDistinctPublishers() throws SQLException {
        List<String> publishers = new ArrayList<>();
        String sql = "SELECT DISTINCT publisher FROM Book ORDER BY publisher";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            SqlLogger.logQuery(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                publishers.add(rs.getString("publisher"));
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return publishers;
    }

    /**
     * 도서별 판매 통계
     */
    public Map<String, Object> getBookStats(int bookId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT COUNT(*) as salesCount, " +
                     "IFNULL(AVG(saleprice), 0) as avgPrice, " +
                     "IFNULL(MAX(saleprice), 0) as maxPrice, " +
                     "IFNULL(MIN(saleprice), 0) as minPrice " +
                     "FROM Orders WHERE bookid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            SqlLogger.logQuery(sql, bookId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                stats.put("salesCount", rs.getInt("salesCount"));
                stats.put("avgPrice", rs.getDouble("avgPrice"));
                stats.put("maxPrice", rs.getInt("maxPrice"));
                stats.put("minPrice", rs.getInt("minPrice"));
            } else {
                stats.put("salesCount", 0);
                stats.put("avgPrice", 0.0);
                stats.put("maxPrice", 0);
                stats.put("minPrice", 0);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return stats;
    }

    private int getNextBookId(Connection conn) throws SQLException {
        String sql = "SELECT IFNULL(MAX(bookid), 0) + 1 AS next_id FROM Book";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            SqlLogger.logQuery(sql);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("next_id");
                }
            }
        }
        return 1;
    }

    private String resolveSortColumn(String sortBy) {
        if (sortBy == null) return DEFAULT_SORT_COLUMN;
        return switch (sortBy.toLowerCase()) {
            case "bookname" -> "bookname";
            case "publisher" -> "publisher";
            case "price" -> "price";
            case "bookid" -> "bookid";
            default -> DEFAULT_SORT_COLUMN;
        };
    }

    private String resolveSortDirection(String direction) {
        if (direction == null) return "ASC";
        return "desc".equalsIgnoreCase(direction) ? "DESC" : "ASC";
    }
}
