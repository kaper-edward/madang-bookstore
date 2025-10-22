package com.madang.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.madang.util.DBConnection;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * /api/health 서블릿 (헬스체크)
 * 서버 상태 및 데이터베이스 연결 확인
 */
@WebServlet("/api/health")
public class HealthServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;
    private static final long START_TIME = System.currentTimeMillis();

    @Override
    protected String handleGet(Map<String, String> params, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Map<String, Object> healthStatus = new HashMap<>();

        // 서버 상태
        healthStatus.put("status", "UP");
        healthStatus.put("service", "Madang Bookstore API");
        healthStatus.put("version", "2.0.0-servlet");
        healthStatus.put("timestamp", System.currentTimeMillis());

        // 서버 가동 시간 (초)
        long uptimeSeconds = (System.currentTimeMillis() - START_TIME) / 1000;
        healthStatus.put("uptime_seconds", uptimeSeconds);
        healthStatus.put("uptime_formatted", formatUptime(uptimeSeconds));

        // 데이터베이스 연결 확인
        Map<String, Object> dbStatus = checkDatabaseConnection();
        healthStatus.put("database", dbStatus);

        // 시스템 정보
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("java_version", System.getProperty("java.version"));
        systemInfo.put("os_name", System.getProperty("os.name"));
        systemInfo.put("os_arch", System.getProperty("os.arch"));
        healthStatus.put("system", systemInfo);

        return successResponse(mapToJson(healthStatus));
    }

    /**
     * 데이터베이스 연결 상태 확인
     */
    private Map<String, Object> checkDatabaseConnection() {
        Map<String, Object> dbStatus = new HashMap<>();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                dbStatus.put("status", "UP");
                dbStatus.put("message", "Database connection successful");
            } else {
                dbStatus.put("status", "DOWN");
                dbStatus.put("message", "Database connection is closed");
            }
        } catch (Exception e) {
            dbStatus.put("status", "DOWN");
            dbStatus.put("message", "Database connection failed: " + e.getMessage());
        }

        return dbStatus;
    }

    /**
     * 가동 시간 포맷팅 (초 → "Xd Yh Zm Ws")
     */
    private String formatUptime(long totalSeconds) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    /**
     * Map을 JSON 문자열로 변환 (중첩 Map 지원)
     */
    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append(valueToJson(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 값을 JSON 문자열로 변환 (재귀적 처리)
     */
    @SuppressWarnings("unchecked")
    private String valueToJson(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + escapeJson((String) value) + "\"";
        } else if (value instanceof Map) {
            return mapToJson((Map<String, Object>) value);
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else {
            return "\"" + value.toString() + "\"";
        }
    }
}
