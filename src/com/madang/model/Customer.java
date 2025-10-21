package com.madang.model;

/**
 * Customer 테이블 모델
 */
public class Customer {
    private int custid;
    private String name;
    private String address;
    private String phone;
    private String role;  // customer, manager, publisher, admin

    public Customer() {}

    public Customer(int custid, String name, String address, String phone) {
        this.custid = custid;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.role = "customer";  // 기본값
    }

    public Customer(int custid, String name, String address, String phone, String role) {
        this.custid = custid;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.role = role;
    }

    public int getCustid() {
        return custid;
    }

    public void setCustid(int custid) {
        this.custid = custid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * JSON 형식으로 변환
     */
    public String toJson() {
        return String.format(
            "{\"custid\":%d,\"name\":\"%s\",\"address\":\"%s\",\"phone\":\"%s\",\"role\":\"%s\"}",
            custid, escapeJson(name), escapeJson(address), escapeJson(phone), escapeJson(role != null ? role : "customer")
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
        return "Customer{" +
                "custid=" + custid +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
