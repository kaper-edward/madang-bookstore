package com.madang.model;

import java.sql.Date;

/**
 * Orders 테이블 모델
 */
public class Order {
    private int orderid;
    private int custid;
    private int bookid;
    private int saleprice;
    private Date orderdate;

    // JOIN용 추가 필드
    private String bookname;
    private String publisher;
    private String customerName;
    private Integer listPrice;

    public Order() {}

    public Order(int orderid, int custid, int bookid, int saleprice, Date orderdate) {
        this.orderid = orderid;
        this.custid = custid;
        this.bookid = bookid;
        this.saleprice = saleprice;
        this.orderdate = orderdate;
    }

    public int getOrderid() {
        return orderid;
    }

    public void setOrderid(int orderid) {
        this.orderid = orderid;
    }

    public int getCustid() {
        return custid;
    }

    public void setCustid(int custid) {
        this.custid = custid;
    }

    public int getBookid() {
        return bookid;
    }

    public void setBookid(int bookid) {
        this.bookid = bookid;
    }

    public int getSaleprice() {
        return saleprice;
    }

    public void setSaleprice(int saleprice) {
        this.saleprice = saleprice;
    }

    public Date getOrderdate() {
        return orderdate;
    }

    public void setOrderdate(Date orderdate) {
        this.orderdate = orderdate;
    }

    public String getBookname() {
        return bookname;
    }

    public void setBookname(String bookname) {
        this.bookname = bookname;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getListPrice() {
        return listPrice;
    }

    public void setListPrice(Integer listPrice) {
        this.listPrice = listPrice;
    }

    /**
     * JSON 형식으로 변환 (기본 필드)
     */
    public String toJson() {
        return String.format(
            "{\"orderid\":%d,\"custid\":%d,\"bookid\":%d,\"saleprice\":%d,\"orderdate\":\"%s\"}",
            orderid, custid, bookid, saleprice, orderdate
        );
    }

    /**
     * JSON 형식으로 변환 (JOIN 필드 포함)
     */
    public String toJsonWithDetails() {
        return String.format(
            "{\"orderid\":%d,\"custid\":%d,\"bookid\":%d,\"saleprice\":%d,\"orderdate\":\"%s\"," +
            "\"bookname\":\"%s\",\"publisher\":\"%s\",\"customerName\":\"%s\",\"listPrice\":%s}",
            orderid, custid, bookid, saleprice, orderdate,
            escapeJson(bookname), escapeJson(publisher), escapeJson(customerName),
            listPrice == null ? "null" : listPrice.toString()
        );
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderid=" + orderid +
                ", custid=" + custid +
                ", bookid=" + bookid +
                ", saleprice=" + saleprice +
                ", orderdate=" + orderdate +
                ", bookname='" + bookname + '\'' +
                ", publisher='" + publisher + '\'' +
                ", customerName='" + customerName + '\'' +
                ", listPrice=" + listPrice +
                '}';
    }
}
