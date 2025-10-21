package com.madang.model;

/**
 * Book 테이블 모델
 */
public class Book {
    private int bookid;
    private String bookname;
    private String publisher;
    private int price;

    public Book() {}

    public Book(int bookid, String bookname, String publisher, int price) {
        this.bookid = bookid;
        this.bookname = bookname;
        this.publisher = publisher;
        this.price = price;
    }

    public int getBookid() {
        return bookid;
    }

    public void setBookid(int bookid) {
        this.bookid = bookid;
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * JSON 형식으로 변환
     */
    public String toJson() {
        return String.format(
            "{\"bookid\":%d,\"bookname\":\"%s\",\"publisher\":\"%s\",\"price\":%d}",
            bookid, escapeJson(bookname), escapeJson(publisher), price
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
        return "Book{" +
                "bookid=" + bookid +
                ", bookname='" + bookname + '\'' +
                ", publisher='" + publisher + '\'' +
                ", price=" + price +
                '}';
    }
}
