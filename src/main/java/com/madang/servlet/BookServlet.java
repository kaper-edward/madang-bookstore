package com.madang.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

import com.madang.dao.BookDAO;
import com.madang.model.Book;
import com.madang.model.PageRequest;
import com.madang.model.PageResponse;

import java.util.List;
import java.util.Map;

/**
 * /api/books 서블릿
 * BookHandler (HttpServer 기반)에서 변환
 */
@WebServlet("/api/books")
public class BookServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;
    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected String handleGet(Map<String, String> params, HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws Exception {
        String action = params.getOrDefault("action", "list");

        switch (action) {
            case "detail":
                int bookId = Integer.parseInt(params.get("id"));
                Book book = bookDAO.getBookById(bookId);
                if (book == null) {
                    return errorResponse("도서를 찾을 수 없습니다.");
                }
                return successResponse(book.toJson());

            case "publishers":
                List<String> publishers = bookDAO.getDistinctPublishers();
                return successResponse(toJsonArrayString(publishers));

            case "stats":
                int statsBookId = Integer.parseInt(params.get("id"));
                Map<String, Object> stats = bookDAO.getBookStats(statsBookId);
                return successResponse(mapToJson(stats));

            case "search":
            case "publisher":
            case "list":
            default:
                String title = params.get("title");
                if (title == null || title.isBlank()) {
                    title = params.get("keyword");
                }
                String publisherFilter = params.getOrDefault("publisher", params.get("name"));
                Integer minPrice = parseInteger(params.get("priceMin"));
                Integer maxPrice = parseInteger(params.get("priceMax"));
                String sortBy = params.get("sortBy");
                String direction = params.get("direction");

                // 페이지네이션 파라미터 확인
                String pageParam = params.get("page");
                String pageSizeParam = params.get("pageSize");

                // 페이지네이션 사용 여부 결정
                if (pageParam != null || pageSizeParam != null) {
                    // 페이지네이션 사용
                    int page = parseInteger(pageParam) != null ? parseInteger(pageParam) : 1;
                    int pageSize = parseInteger(pageSizeParam) != null ? parseInteger(pageSizeParam) : 20;

                    PageRequest pageRequest = new PageRequest(page, pageSize, sortBy, direction);
                    PageResponse<Book> pageResponse = bookDAO.getBooksPaged(pageRequest, title, publisherFilter, minPrice, maxPrice);
                    return successResponse(pageResponseToJson(pageResponse));
                } else {
                    // 기존 방식 (하위 호환성 유지)
                    List<Book> books = bookDAO.getBooks(title, publisherFilter, minPrice, maxPrice, sortBy, direction);
                    return successResponse(toJsonArray(books));
                }
        }
    }

    @Override
    protected String handlePost(Map<String, String> params, String body, HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws Exception {
        String action = params.getOrDefault("action", "create");

        if ("create".equals(action)) {
            String bookname = requireJsonString(body, "bookname");
            String publisher = requireJsonString(body, "publisher");
            int price = requireJsonInt(body, "price");

            int newBookId = bookDAO.createBook(bookname, publisher, price);
            if (newBookId > 0) {
                Book created = new Book(newBookId, bookname, publisher, price);
                return successResponse(created.toJson());
            }
            return errorResponse("도서 등록에 실패했습니다.");
        }

        return errorResponse("알 수 없는 action: " + action);
    }

    @Override
    protected String handlePut(Map<String, String> params, String body, HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws Exception {
        String action = params.getOrDefault("action", "update");

        if ("update".equals(action)) {
            int bookId = requireJsonInt(body, "bookid");
            String bookname = requireJsonString(body, "bookname");
            String publisher = requireJsonString(body, "publisher");
            int price = requireJsonInt(body, "price");

            Book book = new Book(bookId, bookname, publisher, price);
            boolean success = bookDAO.updateBook(book);
            if (success) {
                return successResponse(book.toJson());
            }
            return errorResponse("도서를 수정할 수 없습니다.");
        }

        return errorResponse("알 수 없는 action: " + action);
    }

    @Override
    protected String handleDelete(Map<String, String> params, HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws Exception {
        String action = params.getOrDefault("action", "delete");

        if ("delete".equals(action)) {
            int bookId = Integer.parseInt(params.getOrDefault("id", "0"));
            if (bookId == 0) {
                return errorResponse("도서 ID가 필요합니다.");
            }

            boolean success = bookDAO.deleteBook(bookId);
            if (success) {
                return successResponse("{\"deleted\":true}");
            }
            return errorResponse("도서를 삭제할 수 없습니다.");
        }

        return errorResponse("알 수 없는 action: " + action);
    }

    // ===== 유틸리티 메서드 (BookHandler와 동일) =====

    private String toJsonArray(List<Book> books) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < books.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(books.get(i).toJson());
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonArrayString(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(list.get(i))).append("\"");
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

    /**
     * PageResponse를 JSON 문자열로 변환
     */
    private String pageResponseToJson(PageResponse<Book> pageResponse) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"items\":").append(toJsonArray(pageResponse.getItems())).append(",");
        sb.append("\"page\":").append(pageResponse.getPage()).append(",");
        sb.append("\"pageSize\":").append(pageResponse.getPageSize()).append(",");
        sb.append("\"totalItems\":").append(pageResponse.getTotalItems()).append(",");
        sb.append("\"totalPages\":").append(pageResponse.getTotalPages()).append(",");
        sb.append("\"hasNext\":").append(pageResponse.isHasNext()).append(",");
        sb.append("\"hasPrevious\":").append(pageResponse.isHasPrevious());
        sb.append("}");
        return sb.toString();
    }
}
