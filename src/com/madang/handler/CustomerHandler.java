package com.madang.handler;

import com.madang.dao.CustomerDAO;
import com.madang.model.Customer;
import com.madang.server.ApiHandler;

import java.util.List;
import java.util.Map;

/**
 * /api/customers 핸들러
 */
public class CustomerHandler extends ApiHandler {

    private final CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected String handleGet(Map<String, String> params) throws Exception {
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

        List<Customer> customers = customerDAO.getCustomers(name, phone, address, sortBy, direction);
        return successResponse(toJsonArray(customers));
    }

    @Override
    protected String handlePost(Map<String, String> params, String body) throws Exception {
        String action = params.getOrDefault("action", "create");

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
    protected String handlePut(Map<String, String> params, String body) throws Exception {
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
    protected String handleDelete(Map<String, String> params) throws Exception {
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

    private String toJsonArray(List<Customer> customers) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < customers.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(customers.get(i).toJson());
        }
        sb.append("]");
        return sb.toString();
    }
}
