package com.madang.server;

import com.sun.net.httpserver.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * API 핸들러 기본 클래스
 * 모든 API 핸들러는 이 클래스를 상속받아 구현
 */
public abstract class ApiHandler implements HttpHandler {

    private static final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS 헤더 설정
        setCorsHeaders(exchange);

        // OPTIONS 요청 처리 (CORS preflight)
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            String method = exchange.getRequestMethod();
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String requestBody = readRequestBody(exchange);

            // HTTP 메서드에 따라 처리
            String response;
            switch (method) {
                case "GET":
                    response = handleGet(params);
                    break;
                case "POST":
                    response = handlePost(params, requestBody);
                    break;
                case "PUT":
                    response = handlePut(params, requestBody);
                    break;
                case "DELETE":
                    response = handleDelete(params);
                    break;
                default:
                    response = errorResponse("지원하지 않는 HTTP 메서드입니다.");
            }

            sendJsonResponse(exchange, 200, response);

        } catch (Exception e) {
            e.printStackTrace();
            String errorResponse = errorResponse(e.getMessage() == null ? "알 수 없는 오류가 발생했습니다." : e.getMessage());
            sendJsonResponse(exchange, 500, errorResponse);
        }
    }

    /**
     * GET 요청 처리 (하위 클래스에서 구현)
     */
    protected String handleGet(Map<String, String> params) throws Exception {
        return errorResponse("GET 메서드가 구현되지 않았습니다.");
    }

    /**
     * POST 요청 처리 (하위 클래스에서 구현)
     */
    protected String handlePost(Map<String, String> params, String body) throws Exception {
        return errorResponse("POST 메서드가 구현되지 않았습니다.");
    }

    /**
     * PUT 요청 처리 (하위 클래스에서 구현)
     */
    protected String handlePut(Map<String, String> params, String body) throws Exception {
        return errorResponse("PUT 메서드가 구현되지 않았습니다.");
    }

    /**
     * DELETE 요청 처리 (하위 클래스에서 구현)
     */
    protected String handleDelete(Map<String, String> params) throws Exception {
        return errorResponse("DELETE 메서드가 구현되지 않았습니다.");
    }

    /**
     * 쿼리 파라미터 파싱
     */
    protected Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
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
    protected String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        }
    }

    /**
     * JSON 응답 전송
     */
    protected void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * CORS 헤더 설정
     */
    protected void setCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
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
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
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
     * JSON 본문에서 정수 추출 (선택)
     */
    // protected Integer parseJsonInt(String json, String key) {
    //     Pattern pattern = Pattern.compile("\\\\\"" + Pattern.quote(key) + "\\\\\"\\s*:\\s*(-?\\d+)");
    //     Matcher matcher = pattern.matcher(json);
    //     if (matcher.find()) {
    //         return Integer.parseInt(matcher.group(1));
    //     }
    //     return null;
    // }

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
     * JSON 본문에서 문자열 추출 (선택)
     */
    // protected String parseJsonString(String json, String key) {
    //     Pattern pattern = Pattern.compile("\\\\\"" + Pattern.quote(key) + "\\\\\"\\s*:\\s*\\\\\"((?:\\\\\\\\.|[^\\\\\"])*)\\\\\"");
    //     Matcher matcher = pattern.matcher(json);
    //     if (matcher.find()) {
    //         return unescapeJson(matcher.group(1));
    //     }
    //     return null;
    // }

    // private String unescapeJson(String value) {
    //     return value.replace("\\\\\"", "\"").replace("\\\\\\\\", "\\");
    // }
}
