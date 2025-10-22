package com.madang.dao;

import com.madang.model.Customer;
import com.madang.model.PageRequest;
import com.madang.model.PageResponse;
import com.madang.util.DBConnection;
import com.madang.util.SqlLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer 테이블 데이터 접근 객체
 */
public class CustomerDAO {

    private static final String DEFAULT_SORT_COLUMN = "custid";

    /**
     * 고객 목록 조회 (검색 & 정렬)
     */
    public List<Customer> getCustomers(String name, String phone, String address,
                                       String sortBy, String direction) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT custid, name, address, phone, role FROM Customer"
        );

        boolean hasCondition = false;
        if (name != null && !name.isBlank()) {
            sql.append(hasCondition ? " AND" : " WHERE");
            sql.append(" name LIKE ?");
            params.add("%" + name.trim() + "%");
            hasCondition = true;
        }

        if (phone != null && !phone.isBlank()) {
            sql.append(hasCondition ? " AND" : " WHERE");
            sql.append(" phone LIKE ?");
            params.add("%" + phone.trim() + "%");
            hasCondition = true;
        }

        if (address != null && !address.isBlank()) {
            sql.append(hasCondition ? " AND" : " WHERE");
            sql.append(" address LIKE ?");
            params.add("%" + address.trim() + "%");
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
                Customer customer = new Customer();
                customer.setCustid(rs.getInt("custid"));
                customer.setName(rs.getString("name"));
                customer.setAddress(rs.getString("address"));
                customer.setPhone(rs.getString("phone"));
                customer.setRole(rs.getString("role"));
                customers.add(customer);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return customers;
    }

    /**
     * 고객 목록 조회 (페이징 지원)
     */
    public PageResponse<Customer> getCustomersPaged(PageRequest pageRequest, String name,
                                                     String phone, String address) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        // WHERE 조건 생성
        StringBuilder whereClause = new StringBuilder();
        boolean hasCondition = false;

        if (name != null && !name.isBlank()) {
            whereClause.append(hasCondition ? " AND" : " WHERE");
            whereClause.append(" name LIKE ?");
            params.add("%" + name.trim() + "%");
            hasCondition = true;
        }

        if (phone != null && !phone.isBlank()) {
            whereClause.append(hasCondition ? " AND" : " WHERE");
            whereClause.append(" phone LIKE ?");
            params.add("%" + phone.trim() + "%");
            hasCondition = true;
        }

        if (address != null && !address.isBlank()) {
            whereClause.append(hasCondition ? " AND" : " WHERE");
            whereClause.append(" address LIKE ?");
            params.add("%" + address.trim() + "%");
        }

        // 1. 전체 개수 조회
        long totalItems = countCustomers(whereClause.toString(), params);

        // 2. 페이징된 데이터 조회
        StringBuilder sql = new StringBuilder("SELECT custid, name, address, phone, role FROM Customer");
        sql.append(whereClause);
        sql.append(" ORDER BY ")
           .append(resolveSortColumn(pageRequest.getSortBy()))
           .append(" ")
           .append(resolveSortDirection(pageRequest.getDirection()));
        sql.append(" LIMIT ? OFFSET ?");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            // WHERE 파라미터 바인딩
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            // LIMIT, OFFSET 바인딩
            pstmt.setInt(params.size() + 1, pageRequest.getPageSize());
            pstmt.setInt(params.size() + 2, pageRequest.getOffset());

            SqlLogger.logQuery(sql.toString(), params.toArray());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Customer customer = new Customer();
                customer.setCustid(rs.getInt("custid"));
                customer.setName(rs.getString("name"));
                customer.setAddress(rs.getString("address"));
                customer.setPhone(rs.getString("phone"));
                customer.setRole(rs.getString("role"));
                customers.add(customer);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        // 3. PageResponse 생성
        return new PageResponse<>(
            customers,
            pageRequest.getPage(),
            pageRequest.getPageSize(),
            totalItems
        );
    }

    /**
     * 고객 개수 조회 (필터링 조건 포함)
     */
    private long countCustomers(String whereClause, List<Object> params) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM Customer" + whereClause;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            SqlLogger.logQuery(sql, params.toArray());
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
     * 고객 ID로 조회
     */
    public Customer getCustomerById(int custId) throws SQLException {
        String sql = "SELECT custid, name, address, phone, role FROM Customer WHERE custid = ?";

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
                Customer customer = new Customer();
                customer.setCustid(rs.getInt("custid"));
                customer.setName(rs.getString("name"));
                customer.setAddress(rs.getString("address"));
                customer.setPhone(rs.getString("phone"));
                customer.setRole(rs.getString("role"));
                return customer;
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return null;
    }

    /**
     * 로그인 처리 (custid로 고객 조회)
     */
    public Customer login(int custId) throws SQLException {
        return getCustomerById(custId);
    }

    /**
     * 고객 등록
     * @return 생성된 custid
     */
    public int createCustomer(String name, String address, String phone) throws SQLException {
        String sql = "INSERT INTO Customer (custid, name, address, phone) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            int nextId = getNextCustomerId(conn);

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, nextId);
            pstmt.setString(2, name);
            pstmt.setString(3, address);
            pstmt.setString(4, phone);

            SqlLogger.logUpdate(sql, nextId, name, address, phone);
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
     * 고객 수정
     */
    public boolean updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE Customer SET name = ?, address = ?, phone = ? WHERE custid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getAddress());
            pstmt.setString(3, customer.getPhone());
            pstmt.setInt(4, customer.getCustid());

            SqlLogger.logUpdate(sql, customer.getName(), customer.getAddress(), customer.getPhone(), customer.getCustid());
            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            DBConnection.close(conn, pstmt);
        }
    }

    /**
     * 고객 삭제
     */
    public boolean deleteCustomer(int custId) throws SQLException {
        String sql = "DELETE FROM Customer WHERE custid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, custId);
            SqlLogger.logUpdate(sql, custId);
            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            DBConnection.close(conn, pstmt);
        }
    }

    private int getNextCustomerId(Connection conn) throws SQLException {
        String sql = "SELECT IFNULL(MAX(custid), 0) + 1 AS next_id FROM Customer";

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
            case "name" -> "name";
            case "address" -> "address";
            case "phone" -> "phone";
            case "custid" -> "custid";
            default -> DEFAULT_SORT_COLUMN;
        };
    }

    private String resolveSortDirection(String direction) {
        if (direction == null) return "ASC";
        return "desc".equalsIgnoreCase(direction) ? "DESC" : "ASC";
    }
}
