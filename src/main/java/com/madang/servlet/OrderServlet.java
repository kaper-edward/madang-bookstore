package com.madang.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.madang.dao.OrderDAO;
import com.madang.model.Order;
import com.madang.model.PageRequest;
import com.madang.model.PageResponse;

import java.util.List;
import java.util.Map;

/**
 * /api/orders 서블릿
 * OrderHandler (HttpServer 기반)에서 변환
 */
@WebServlet("/api/orders")
public class OrderServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected String handleGet(Map<String, String> params, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String action = params.get("action");

        if ("list".equals(action)) {
            int custId = Integer.parseInt(params.get("custid"));
            String sortBy = params.get("sortBy");
            String direction = params.get("direction");

            // 페이지네이션 파라미터 확인
            String pageParam = params.get("page");
            String pageSizeParam = params.get("pageSize");

            // 페이지네이션 사용 여부 결정
            if (pageParam != null || pageSizeParam != null) {
                int page = parseInteger(pageParam) != null ? parseInteger(pageParam) : 1;
                int pageSize = parseInteger(pageSizeParam) != null ? parseInteger(pageSizeParam) : 10;

                PageRequest pageRequest = new PageRequest(page, pageSize, sortBy, direction);
                PageResponse<Order> pageResponse = orderDAO.getOrdersByCustomerPaged(pageRequest, custId);
                return successResponse(pageResponseToJson(pageResponse));
            } else {
                // 기존 방식 (하위 호환성 유지)
                List<Order> orders = orderDAO.getOrdersByCustomer(custId, sortBy, direction);
                return successResponse(toJsonArray(orders));
            }
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
    protected String handlePost(Map<String, String> params, String body, HttpServletRequest req, HttpServletResponse resp) throws Exception {
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
    protected String handlePut(Map<String, String> params, String body, HttpServletRequest req, HttpServletResponse resp) throws Exception {
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
    protected String handleDelete(Map<String, String> params, HttpServletRequest req, HttpServletResponse resp) throws Exception {
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

    // ===== 유틸리티 메서드 =====

    private String toJsonArray(List<Order> orders) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < orders.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(orders.get(i).toJsonWithDetails());
        }
        sb.append("]");
        return sb.toString();
    }

    private String pageResponseToJson(PageResponse<Order> pageResponse) {
        StringBuilder itemsJson = new StringBuilder("[");
        List<Order> items = pageResponse.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) itemsJson.append(",");
            itemsJson.append(items.get(i).toJsonWithDetails());
        }
        itemsJson.append("]");

        return String.format(
            "{\"items\":%s,\"page\":%d,\"pageSize\":%d,\"totalItems\":%d,\"totalPages\":%d,\"hasNext\":%b,\"hasPrevious\":%b}",
            itemsJson.toString(),
            pageResponse.getPage(),
            pageResponse.getPageSize(),
            pageResponse.getTotalItems(),
            pageResponse.getTotalPages(),
            pageResponse.isHasNext(),
            pageResponse.isHasPrevious()
        );
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

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
