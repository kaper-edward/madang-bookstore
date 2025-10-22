package com.madang.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.madang.dao.OrderDAO;

import java.util.List;
import java.util.Map;

/**
 * /api/stats 서블릿 (대시보드용)
 * StatsHandler (HttpServer 기반)에서 변환
 */
@WebServlet("/api/stats")
public class StatsServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected String handleGet(Map<String, String> params, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String action = params.get("action");

        if ("overview".equals(action)) {
            Map<String, Object> stats = orderDAO.getOverallStats();
            return successResponse(mapToJson(stats));
        }

        if ("bestsellers".equals(action)) {
            int limit = Integer.parseInt(params.getOrDefault("limit", "5"));
            List<Map<String, Object>> bestsellers = orderDAO.getBestsellers(limit);
            return successResponse(listMapToJsonArray(bestsellers));
        }

        if ("weekly-bestsellers".equals(action)) {
            int limit = Integer.parseInt(params.getOrDefault("limit", "5"));
            List<Map<String, Object>> bestsellers = orderDAO.getWeeklyBestsellers(limit);
            return successResponse(listMapToJsonArray(bestsellers));
        }

        if ("recent".equals(action)) {
            int limit = Integer.parseInt(params.getOrDefault("limit", "5"));
            String sortBy = params.get("sortBy");
            String direction = params.get("direction");
            List<Map<String, Object>> orders = orderDAO.getRecentOrders(limit, sortBy, direction);
            return successResponse(listMapToJsonArray(orders));
        }

        if ("customers".equals(action)) {
            String sortBy = params.get("sortBy");
            String direction = params.get("direction");
            List<Map<String, Object>> stats = orderDAO.getStatsByCustomer(sortBy, direction);
            return successResponse(listMapToJsonArray(stats));
        }

        if ("publishers".equals(action)) {
            String sortBy = params.get("sortBy");
            String direction = params.get("direction");
            List<Map<String, Object>> stats = orderDAO.getStatsByPublisher(sortBy, direction);
            return successResponse(listMapToJsonArray(stats));
        }

        if ("books".equals(action)) {
            String sortBy = params.get("sortBy");
            String direction = params.get("direction");
            List<Map<String, Object>> stats = orderDAO.getStatsByBook(sortBy, direction);
            return successResponse(listMapToJsonArray(stats));
        }

        if ("monthly".equals(action)) {
            int months = Integer.parseInt(params.getOrDefault("months", "12"));
            List<Map<String, Object>> stats = orderDAO.getMonthlySales(months);
            return successResponse(listMapToJsonArray(stats));
        }

        if ("customer-segments".equals(action)) {
            String month = params.get("month");
            List<Map<String, Object>> segments;

            if (month != null && !month.isEmpty()) {
                segments = orderDAO.getCustomerSegmentsByMonth(month);
            } else {
                segments = orderDAO.getCustomerSegments();
            }

            return successResponse(listMapToJsonArray(segments));
        }

        if ("top-customers".equals(action)) {
            String month = params.get("month");
            int limit = Integer.parseInt(params.getOrDefault("limit", "10"));

            if (month == null || month.isEmpty()) {
                return errorResponse("month 파라미터가 필요합니다");
            }

            List<Map<String, Object>> customers = orderDAO.getTopCustomersByMonth(month, limit);
            return successResponse(listMapToJsonArray(customers));
        }

        if ("publishers-by-month".equals(action)) {
            String month = params.get("month");

            if (month == null || month.isEmpty()) {
                return errorResponse("month 파라미터가 필요합니다");
            }

            List<Map<String, Object>> stats = orderDAO.getPublisherStatsByMonth(month);
            return successResponse(listMapToJsonArray(stats));
        }

        if ("books-by-month".equals(action)) {
            String month = params.get("month");

            if (month == null || month.isEmpty()) {
                return errorResponse("month 파라미터가 필요합니다");
            }

            List<Map<String, Object>> stats = orderDAO.getBookStatsByMonth(month);
            return successResponse(listMapToJsonArray(stats));
        }

        return errorResponse("알 수 없는 action: " + action);
    }

    // ===== 유틸리티 메서드 =====

    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(escapeJson((String) value)).append("\"");
            } else {
                sb.append(value);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String listMapToJsonArray(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(mapToJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }
}
