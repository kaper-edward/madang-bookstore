package com.madang.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import com.madang.util.SessionManager;
import com.madang.util.SessionManager.Session;

/**
 * API 서블릿 기본 클래스
 * 모든 API 서블릿은 이 클래스를 상속받아 구현
 *
 * ApiHandler (HttpServer 기반)에서 ApiServlet (Servlet 기반)으로 변환
 */
public abstract class ApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Gson gson = new Gson();

    /**
     * 모든 HTTP 메서드를 처리하는 통합 service 메서드
     * ApiHandler.handle()과 동일한 역할
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // CORS 헤더 설정
        setCorsHeaders(resp);

        // OPTIONS 요청 처리 (CORS preflight)
        if ("OPTIONS".equals(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        try {
            String method = req.getMethod();
            Map<String, String> params = parseQueryParams(req);
            String requestBody = readRequestBody(req);

            // HTTP 메서드에 따라 처리
            String response;
            switch (method) {
                case "GET":
                    response = handleGet(params, req, resp);
                    break;
                case "POST":
                    response = handlePost(params, requestBody, req, resp);
                    break;
                case "PUT":
                    response = handlePut(params, requestBody, req, resp);
                    break;
                case "DELETE":
                    response = handleDelete(params, req, resp);
                    break;
                default:
                    response = errorResponse("지원하지 않는 HTTP 메서드입니다.");
            }

            sendJsonResponse(resp, HttpServletResponse.SC_OK, response);

        } catch (IllegalAccessException e) {
            // 권한 오류 (403 Forbidden)
            e.printStackTrace();
            sendJsonResponse(resp, HttpServletResponse.SC_FORBIDDEN,
                errorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = e.getMessage() == null ? "알 수 없는 오류가 발생했습니다." : e.getMessage();
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                errorResponse(errorMsg));
        }
    }

    /**
     * GET 요청 처리 (하위 클래스에서 구현)
     */
    protected String handleGet(Map<String, String> params, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        return errorResponse("GET 메서드가 구현되지 않았습니다.");
    }

    /**
     * POST 요청 처리 (하위 클래스에서 구현)
     */
    protected String handlePost(Map<String, String> params, String body, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        return errorResponse("POST 메서드가 구현되지 않았습니다.");
    }

    /**
     * PUT 요청 처리 (하위 클래스에서 구현)
     */
    protected String handlePut(Map<String, String> params, String body, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        return errorResponse("PUT 메서드가 구현되지 않았습니다.");
    }

    /**
     * DELETE 요청 처리 (하위 클래스에서 구현)
     */
    protected String handleDelete(Map<String, String> params, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        return errorResponse("DELETE 메서드가 구현되지 않았습니다.");
    }

    /**
     * 쿼리 파라미터 파싱
     */
    protected Map<String, String> parseQueryParams(HttpServletRequest req) {
        Map<String, String> params = new HashMap<>();
        String query = req.getQueryString();

        if (query == null || query.isEmpty()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                params.put(keyValue[0], URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
            } else if (keyValue.length == 1) {
                params.put(keyValue[0], "");
            }
        }
        return params;
    }

    /**
     * 요청 본문 읽기
     */
    protected String readRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString();
    }

    /**
     * JSON 응답 전송
     */
    protected void sendJsonResponse(HttpServletResponse resp, int statusCode, String jsonResponse)
            throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = resp.getWriter()) {
            writer.write(jsonResponse);
            writer.flush();
        }
    }

    /**
     * CORS 헤더 설정
     */
    protected void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, X-Session-Id");
        resp.setHeader("Access-Control-Expose-Headers", "X-Session-Id");
    }

    /**
     * 성공 응답 생성
     */
    protected String successResponse(String data) {
        return "{\"success\": true, \"data\": " + data + "}";
    }

    /**
     * 에러 응답 생성
     */
    protected String errorResponse(String message) {
        return "{\"success\": false, \"error\": \"" + escapeJson(message) + "\"}";
    }

    /**
     * JSON 문자열 이스케이프
     */
    protected String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * 객체를 JSON 문자열로 변환 (간단한 구현)
     */
    protected String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escapeJson((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        return obj.toString();
    }

    /**
     * JSON 본문에서 정수 추출 (필수)
     */
    protected int requireJsonInt(String json, String key) {
        try {
            JsonObject obj = gson.fromJson(json, JsonObject.class);
            if (!obj.has(key) || obj.get(key).isJsonNull()) {
                throw new IllegalArgumentException(key + " 값이 필요합니다.");
            }
            return obj.get(key).getAsInt();
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("잘못된 JSON 형식입니다.");
        }
    }

    /**
     * JSON 본문에서 문자열 추출 (필수)
     */
    protected String requireJsonString(String json, String key) {
        try {
            JsonObject obj = gson.fromJson(json, JsonObject.class);
            if (!obj.has(key) || obj.get(key).isJsonNull()) {
                throw new IllegalArgumentException(key + " 값이 필요합니다.");
            }
            String value = obj.get(key).getAsString();
            if (value.isBlank()) {
                throw new IllegalArgumentException(key + " 값이 비어있습니다.");
            }
            return value;
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("잘못된 JSON 형식입니다.");
        }
    }

    /**
     * HTTP 헤더에서 세션 ID 추출
     */
    protected String getSessionId(HttpServletRequest req) {
        return req.getHeader("X-Session-Id");
    }

    /**
     * 세션 조회
     */
    protected Session getSession(HttpServletRequest req) {
        String sessionId = getSessionId(req);
        return SessionManager.getSession(sessionId);
    }

    /**
     * 권한 검증: 관리자 또는 매니저만 허용
     * @throws IllegalAccessException 권한이 없으면 예외 발생
     */
    protected void requireAdmin(HttpServletRequest req) throws IllegalAccessException {
        Session session = getSession(req);
        if (session == null) {
            throw new IllegalAccessException("로그인이 필요합니다.");
        }

        String role = session.getRole();
        if (!"admin".equals(role) && !"manager".equals(role)) {
            throw new IllegalAccessException("관리자 권한이 필요합니다.");
        }
    }

    /**
     * 권한 검증: 특정 role만 허용
     * @throws IllegalAccessException 권한이 없으면 예외 발생
     */
    protected void requireRole(HttpServletRequest req, String... allowedRoles) throws IllegalAccessException {
        Session session = getSession(req);
        if (session == null) {
            throw new IllegalAccessException("로그인이 필요합니다.");
        }

        String role = session.getRole();
        for (String allowedRole : allowedRoles) {
            if (allowedRole.equals(role)) {
                return;
            }
        }

        throw new IllegalAccessException("권한이 부족합니다.");
    }

    /**
     * 권한 검증: 본인 또는 관리자만 허용
     * @throws IllegalAccessException 권한이 없으면 예외 발생
     */
    protected void requireSelfOrAdmin(HttpServletRequest req, int targetCustid) throws IllegalAccessException {
        Session session = getSession(req);
        if (session == null) {
            throw new IllegalAccessException("로그인이 필요합니다.");
        }

        String role = session.getRole();
        int custid = session.getCustid();

        // 관리자/매니저이거나 본인인 경우 허용
        if ("admin".equals(role) || "manager".equals(role) || custid == targetCustid) {
            return;
        }

        throw new IllegalAccessException("권한이 부족합니다.");
    }

    /**
     * 로그인 여부 확인
     */
    protected boolean isLoggedIn(HttpServletRequest req) {
        return getSession(req) != null;
    }
}
