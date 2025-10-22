package com.madang.model;

import java.util.List;

/**
 * 페이지네이션 응답 정보를 담는 제네릭 클래스
 * @param <T> 페이지에 포함될 데이터 타입 (Book, Order, Customer 등)
 */
public class PageResponse<T> {
    private List<T> items;          // 현재 페이지의 데이터 목록
    private int page;               // 현재 페이지 번호
    private int pageSize;           // 페이지당 항목 수
    private long totalItems;        // 전체 항목 수
    private int totalPages;         // 전체 페이지 수
    private boolean hasNext;        // 다음 페이지 존재 여부
    private boolean hasPrevious;    // 이전 페이지 존재 여부

    /**
     * 기본 생성자
     */
    public PageResponse() {
    }

    /**
     * 모든 필드를 계산하는 생성자
     *
     * @param items 현재 페이지의 데이터 목록
     * @param page 현재 페이지 번호
     * @param pageSize 페이지당 항목 수
     * @param totalItems 전체 항목 수
     */
    public PageResponse(List<T> items, int page, int pageSize, long totalItems) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = calculateTotalPages(totalItems, pageSize);
        this.hasNext = page < this.totalPages;
        this.hasPrevious = page > 1;
    }

    /**
     * 전체 페이지 수 계산
     */
    private int calculateTotalPages(long totalItems, int pageSize) {
        if (pageSize <= 0) return 0;
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    // Getters and Setters
    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
        this.totalPages = calculateTotalPages(totalItems, this.pageSize);
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    @Override
    public String toString() {
        return "PageResponse{" +
                "itemCount=" + (items != null ? items.size() : 0) +
                ", page=" + page +
                ", pageSize=" + pageSize +
                ", totalItems=" + totalItems +
                ", totalPages=" + totalPages +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                '}';
    }
}
