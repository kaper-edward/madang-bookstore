package com.madang.handler;

import com.madang.dao.OrderDAO;
import com.madang.server.ApiHandler;

import java.util.List;
import java.util.Map;

/**
 * /api/stats 핸들러 (대시보드용)
 */
public class StatsHandler extends ApiHandler {

    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected String handleGet(Map<String, String> params) throws Exception {
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

        return errorResponse("알 수 없는 action: " + action);
    }

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
