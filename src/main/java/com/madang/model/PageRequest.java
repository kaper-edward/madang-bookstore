package com.madang.model;

/**
 * 페이지네이션 요청 정보를 담는 클래스
 */
public class PageRequest {
    private int page;           // 현재 페이지 번호 (1부터 시작)
    private int pageSize;       // 페이지당 항목 수
    private String sortBy;      // 정렬 기준 컬럼
    private String direction;   // 정렬 방향 (ASC, DESC)

    /**
     * 기본 생성자 (페이지 1, 20개 항목, bookid ASC 정렬)
     */
    public PageRequest() {
        this(1, 20, "bookid", "ASC");
    }

    /**
     * 페이지와 사이즈만 지정하는 생성자
     */
    public PageRequest(int page, int pageSize) {
        this(page, pageSize, "bookid", "ASC");
    }

    /**
     * 모든 필드를 지정하는 생성자
     */
    public PageRequest(int page, int pageSize, String sortBy, String direction) {
        this.page = Math.max(1, page);  // 최소 1페이지
        this.pageSize = Math.max(1, Math.min(100, pageSize));  // 1~100 사이
        this.sortBy = sortBy != null ? sortBy : "bookid";
        this.direction = "DESC".equalsIgnoreCase(direction) ? "DESC" : "ASC";
    }

    /**
     * SQL OFFSET 값 계산 (0부터 시작)
     */
    public int getOffset() {
        return (page - 1) * pageSize;
    }

    // Getters and Setters
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(1, page);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = Math.max(1, Math.min(100, pageSize));
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = "DESC".equalsIgnoreCase(direction) ? "DESC" : "ASC";
    }

    @Override
    public String toString() {
        return "PageRequest{" +
                "page=" + page +
                ", pageSize=" + pageSize +
                ", sortBy='" + sortBy + '\'' +
                ", direction='" + direction + '\'' +
                ", offset=" + getOffset() +
                '}';
    }
}
