package com.example.brettmacdonald.kingabis;

public class Item {
    private String name;
    private String price;
    private String type;
    private String count;

    public Item(String name, String price,  String type, String count) {
        this.name = name;
        this.price = price;
        this.type = type;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
