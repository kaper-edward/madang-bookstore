package com.madang.handler;

import com.madang.dao.OrderDAO;
import com.madang.model.Order;
import com.madang.server.ApiHandler;

import java.util.List;
import java.util.Map;

/**
 * /api/orders 핸들러
 */
public class OrderHandler extends ApiHandler {

    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected String handleGet(Map<String, String> params) throws Exception {
        String action = params.get("action");

        if ("list".equals(action)) {
            int custId = Integer.parseInt(params.get("custid"));
            String sortBy = params.get("sortBy");
            String direction = params.get("direction");
            List<Order> orders = orderDAO.getOrdersByCustomer(custId, sortBy, direction);
            return successResponse(toJsonArray(orders));
        }

        if ("stats".equals(action)) {
            int custId = Integer.parseInt(params.get("custid"));
            Map<String, Object> stats = orderDAO.getCustomerOrderStats(custId);
            return successResponse(mapToJson(stats));
        }

        if ("recent".equals(action)) {
            int limit = Integer.parseInt(params.getOrDefault("limit", "5"));
            String sortBy = params.get("sortBy");
            String direction = params.get("direction");
            List<Map<String, Object>> recent = orderDAO.getRecentOrders(limit, sortBy, direction);
            return successResponse(listMapToJsonArray(recent));
        }

        return errorResponse("알 수 없는 action: " + action);
    }

    @Override
    protected String handlePost(Map<String, String> params, String body) throws Exception {
        String action = params.get("action");

        if ("create".equals(action)) {
            int custId = requireJsonInt(body, "custid");
            int bookId = requireJsonInt(body, "bookid");
            int salePrice = requireJsonInt(body, "saleprice");

            int orderId = orderDAO.createOrder(custId, bookId, salePrice);
            if (orderId > 0) {
                return successResponse("{\"orderid\":" + orderId + "}");
            } else {
                return errorResponse("주문 생성에 실패했습니다.");
            }
        }

        return errorResponse("알 수 없는 action: " + action);
    }

    @Override
    protected String handlePut(Map<String, String> params, String body) throws Exception {
        String action = params.get("action");

        if ("update".equals(action)) {
            int orderId = requireJsonInt(body, "orderid");
            int custId = requireJsonInt(body, "custid");
            int salePrice = requireJsonInt(body, "saleprice");

            boolean success = orderDAO.updateOrderPrice(orderId, custId, salePrice);
            if (success) {
                return successResponse("{\"updated\":true}");
            }
            return errorResponse("주문 정보를 수정할 수 없습니다.");
        }

        return errorResponse("알 수 없는 action: " + action);
    }

    @Override
    protected String handleDelete(Map<String, String> params) throws Exception {
        String action = params.get("action");

        if ("delete".equals(action)) {
            int orderId = Integer.parseInt(params.get("id"));
            int custId = Integer.parseInt(params.get("custid"));

            boolean success = orderDAO.deleteOrder(orderId, custId);
            if (success) {
                return successResponse("{\"deleted\":true}");
            } else {
                return errorResponse("주문을 찾을 수 없거나 권한이 없습니다.");
            }
        }

        return errorResponse("알 수 없는 action: " + action);
    }

    private String toJsonArray(List<Order> orders) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < orders.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(orders.get(i).toJsonWithDetails());
        }
        sb.append("]");
        return sb.toString();
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
