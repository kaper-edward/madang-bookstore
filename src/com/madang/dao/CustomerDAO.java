package com.madang.dao;

import com.madang.model.Customer;
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
            "SELECT custid, name, address, phone FROM Customer"
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
                customers.add(customer);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return customers;
    }

    /**
     * 고객 ID로 조회
     */
    public Customer getCustomerById(int custId) throws SQLException {
        String sql = "SELECT custid, name, address, phone FROM Customer WHERE custid = ?";

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
                return customer;
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return null;
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
