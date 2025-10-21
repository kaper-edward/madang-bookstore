package com.madang.model;

/**
 * Customer 테이블 모델
 */
public class Customer {
    private int custid;
    private String name;
    private String address;
    private String phone;

    public Customer() {}

    public Customer(int custid, String name, String address, String phone) {
        this.custid = custid;
        this.name = name;
        this.address = address;
        this.phone = phone;
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

    /**
     * JSON 형식으로 변환
     */
    public String toJson() {
        return String.format(
            "{\"custid\":%d,\"name\":\"%s\",\"address\":\"%s\",\"phone\":\"%s\"}",
            custid, escapeJson(name), escapeJson(address), escapeJson(phone)
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
