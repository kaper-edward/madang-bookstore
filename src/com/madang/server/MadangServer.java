package com.madang.server;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.concurrent.Executors;

/**
 * 마당 서점 간단한 HTTP 서버
 * Java 21의 내장 HTTP 서버 사용 (Tomcat 불필요)
 */
public class MadangServer {

    private static final int PORT = 8080;
    private static final String FRONTEND_DIR = "frontend";

    public static void main(String[] args) throws IOException {
        // HTTP 서버 생성
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);

        // 스레드 풀 설정 (최대 10개 동시 요청 처리)
        server.setExecutor(Executors.newFixedThreadPool(10));

        // API 라우트 등록
        registerApiRoutes(server);

        // 정적 파일 서빙 (HTML, CSS, JS)
        server.createContext("/", new StaticFileHandler(FRONTEND_DIR));

        // 서버 시작
        server.start();

        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║   마당 온라인 서점 서버 시작됨! 🚀         ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("📍 서버 주소: http://localhost:" + PORT);
        System.out.println("📁 프론트엔드: " + new File(FRONTEND_DIR).getAbsolutePath());
        System.out.println();
        System.out.println("🌐 브라우저에서 접속하세요:");
        System.out.println("   http://localhost:" + PORT + "/index.html");
        System.out.println();
        System.out.println("⚠️  종료하려면 Ctrl+C를 누르세요");
        System.out.println();
    }

    /**
     * API 라우트 등록
     */
    private static void registerApiRoutes(HttpServer server) {
        // API 핸들러 등록
        server.createContext("/api/books", new com.madang.handler.BookHandler());
        server.createContext("/api/customers", new com.madang.handler.CustomerHandler());
        server.createContext("/api/orders", new com.madang.handler.OrderHandler());
        server.createContext("/api/stats", new com.madang.handler.StatsHandler());

        // 테스트 핸들러
        server.createContext("/api/test", exchange -> {
            String response = "{\"success\": true, \"message\": \"API 서버 작동 중! ✅\"}";

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        System.out.println("✓ API 라우트 등록 완료:");
        System.out.println("  - /api/books");
        System.out.println("  - /api/customers");
        System.out.println("  - /api/orders");
        System.out.println("  - /api/stats");
    }

    /**
     * 정적 파일 핸들러 (HTML, CSS, JS, 이미지 등)
     */
    static class StaticFileHandler implements HttpHandler {
        private final String rootDirectory;

        public StaticFileHandler(String rootDirectory) {
            this.rootDirectory = rootDirectory;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            // 루트 경로는 index.html로 리다이렉트
            if (path.equals("/")) {
                path = "/index.html";
            }

            File file = new File(rootDirectory + path).getCanonicalFile();

            // 보안: 루트 디렉토리 밖의 파일 접근 방지
            if (!file.getPath().startsWith(new File(rootDirectory).getCanonicalPath())) {
                send404(exchange);
                return;
            }

            // 파일이 존재하지 않으면 404
            if (!file.exists() || file.isDirectory()) {
                send404(exchange);
                return;
            }

            // Content-Type 설정
            String contentType = getContentType(file.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);

            // 파일 전송
            exchange.sendResponseHeaders(200, file.length());
            try (OutputStream os = exchange.getResponseBody();
                 FileInputStream fs = new FileInputStream(file)) {
                fs.transferTo(os);
            }

            System.out.println("✓ 200 " + path);
        }

        /**
         * 404 응답 전송
         */
        private void send404(HttpExchange exchange) throws IOException {
            String response = "<html><body><h1>404 Not Found</h1></body></html>";
            exchange.sendResponseHeaders(404, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            System.out.println("✗ 404 " + exchange.getRequestURI().getPath());
        }

        /**
         * 파일 확장자에 따른 Content-Type 반환
         */
        private String getContentType(String filename) {
            if (filename.endsWith(".html")) return "text/html; charset=UTF-8";
            if (filename.endsWith(".css")) return "text/css; charset=UTF-8";
            if (filename.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (filename.endsWith(".json")) return "application/json; charset=UTF-8";
            if (filename.endsWith(".png")) return "image/png";
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
            if (filename.endsWith(".gif")) return "image/gif";
            if (filename.endsWith(".svg")) return "image/svg+xml";
            if (filename.endsWith(".ico")) return "image/x-icon";
            return "text/plain";
        }
    }
}
