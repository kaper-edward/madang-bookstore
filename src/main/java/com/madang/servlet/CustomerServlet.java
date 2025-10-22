package com.madang.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.madang.dao.CustomerDAO;
import com.madang.model.Customer;
import com.madang.model.PageRequest;
import com.madang.model.PageResponse;
import com.madang.util.SessionManager;

import java.util.List;
import java.util.Map;

/**
 * /api/customers 서블릿
 * CustomerHandler (HttpServer 기반)에서 변환
 */
@WebServlet("/api/customers")
public class CustomerServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;
    private final CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected String handleGet(Map<String, String> params, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String action = params.getOrDefault("action", "list");

        if ("detail".equals(action)) {
            int custId = Integer.parseInt(params.get("id"));
            Customer customer = customerDAO.getCustomerById(custId);
            if (customer == null) {
                return errorResponse("고객을 찾을 수 없습니다.");
            }
            return successResponse(customer.toJson());
        }

        String name = params.getOrDefault("name", params.get("keyword"));
        String phone = params.get("phone");
        String address = params.get("address");
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
            PageResponse<Customer> pageResponse = customerDAO.getCustomersPaged(pageRequest, name, phone, address);
            return successResponse(pageResponseToJson(pageResponse));
        } else {
            // 기존 방식 (하위 호환성 유지)
            List<Customer> customers = customerDAO.getCustomers(name, phone, address, sortBy, direction);
            return successResponse(toJsonArray(customers));
        }
    }

    @Override
    protected String handlePost(Map<String, String> params, String body, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String action = params.getOrDefault("action", "create");

        if ("login".equals(action)) {
            int custId = requireJsonInt(body, "custid");
            Customer customer = customerDAO.login(custId);

            if (customer == null) {
                return errorResponse("고객을 찾을 수 없습니다.");
            }

            // 세션 생성
            String sessionId = SessionManager.createSession(
                customer.getCustid(),
                customer.getName(),
                customer.getRole()
            );

            // 세션 ID를 응답 헤더에 추가 (Servlet 방식)
            resp.setHeader("X-Session-Id", sessionId);

            // 고객 정보와 세션 ID 반환
            String response = String.format(
                "{\"customer\":%s,\"sessionId\":\"%s\"}",
                customer.toJson(), sessionId
            );
            return successResponse(response);
        }

        if ("create".equals(action)) {
            String name = requireJsonString(body, "name");
            String address = requireJsonString(body, "address");
            String phone = requireJsonString(body, "phone");

            int newId = customerDAO.createCustomer(name, address, phone);
            if (newId > 0) {
                Customer created = new Customer(newId, name, address, phone);
                return successResponse(created.toJson());
            }
            return errorResponse("고객 등록에 실패했습니다.");
        }

        return errorResponse("알 수 없는 action: " + action);
    }

    @Override
    protected String handlePut(Map<String, String> params, String body, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String action = params.getOrDefault("action", "update");

        if ("update".equals(action)) {
            int custId = requireJsonInt(body, "custid");
            String name = requireJsonString(body, "name");
            String address = requireJsonString(body, "address");
            String phone = requireJsonString(body, "phone");

            Customer customer = new Customer(custId, name, address, phone);
            boolean success = customerDAO.updateCustomer(customer);
            if (success) {
                return successResponse(customer.toJson());
            }
            return errorResponse("고객 정보를 수정할 수 없습니다.");
        }

        return errorResponse("알 수 없는 action: " + action);
    }

    @Override
    protected String handleDelete(Map<String, String> params, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String action = params.getOrDefault("action", "delete");

        if ("delete".equals(action)) {
            int custId = Integer.parseInt(params.getOrDefault("id", "0"));
            if (custId == 0) {
                return errorResponse("고객 ID가 필요합니다.");
            }

            boolean success = customerDAO.deleteCustomer(custId);
            if (success) {
                return successResponse("{\"deleted\":true}");
            }
            return errorResponse("고객을 삭제할 수 없습니다.");
        }

        return errorResponse("알 수 없는 action: " + action);
    }

    // ===== 유틸리티 메서드 =====

    private String toJsonArray(List<Customer> customers) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < customers.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(customers.get(i).toJson());
        }
        sb.append("]");
        return sb.toString();
    }

    private String pageResponseToJson(PageResponse<Customer> pageResponse) {
        StringBuilder itemsJson = new StringBuilder("[");
        List<Customer> items = pageResponse.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) itemsJson.append(",");
            itemsJson.append(items.get(i).toJson());
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
