package com.madang.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;

/**
 * 데이터베이스 연결 관리 클래스 (Connection Pool 사용)
 * MySQL madangdb에 연결
 * ConfigManager를 통해 설정 관리 (환경 변수 > properties 파일 > 기본값)
 * HikariCP Connection Pool로 성능 최적화
 */
public class DBConnection {

    // ConfigManager에서 설정 읽어오기
    private static final String URL = ConfigManager.getString("db.url",
        "jdbc:mysql://localhost:3306/madangdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
    private static final String USER = ConfigManager.getString("db.user", "madang");
    private static final String PASSWORD = ConfigManager.getString("db.password", "madang");

    // HikariCP Connection Pool (싱글톤)
    private static HikariDataSource dataSource;

    static {
        try {
            // MySQL JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // HikariCP 설정
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);

            // Connection Pool 설정 (ConfigManager에서 읽기)
            config.setMaximumPoolSize(ConfigManager.getInt("db.pool.maximum.size", 10));
            config.setMinimumIdle(ConfigManager.getInt("db.pool.minimum.idle", 2));
            config.setConnectionTimeout(ConfigManager.getLong("db.pool.connection.timeout", 30000));
            config.setIdleTimeout(ConfigManager.getLong("db.pool.idle.timeout", 600000));
            config.setMaxLifetime(ConfigManager.getLong("db.pool.max.lifetime", 1800000));

            // 풀 이름 설정 (로깅용)
            config.setPoolName(ConfigManager.getString("db.pool.name", "MadangDB-Pool"));

            // DataSource 생성
            dataSource = new HikariDataSource(config);

            System.out.println("✅ HikariCP Connection Pool initialized successfully");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC 드라이버를 찾을 수 없습니다.", e);
        }
    }

    /**
     * 데이터베이스 연결 생성 (Connection Pool에서 가져오기)
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 리소스 정리 (ResultSet, PreparedStatement, Connection)
     */
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (pstmt != null) pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 리소스 정리 (PreparedStatement, Connection)
     */
    public static void close(Connection conn, PreparedStatement pstmt) {
        close(conn, pstmt, null);
    }

    /**
     * 연결 테스트
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Connection Pool 종료 (애플리케이션 종료 시 호출)
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("✅ HikariCP Connection Pool shut down successfully");
        }
    }

    /**
     * Connection Pool 정보 조회
     */
    public static String getPoolInfo() {
        if (dataSource != null) {
            return String.format("Pool: %s | Active: %d | Idle: %d | Total: %d | Waiting: %d",
                    dataSource.getPoolName(),
                    dataSource.getHikariPoolMXBean().getActiveConnections(),
                    dataSource.getHikariPoolMXBean().getIdleConnections(),
                    dataSource.getHikariPoolMXBean().getTotalConnections(),
                    dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
            );
        }
        return "Pool not initialized";
    }
}
