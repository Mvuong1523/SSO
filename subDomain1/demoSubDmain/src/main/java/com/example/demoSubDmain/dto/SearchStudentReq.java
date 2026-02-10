package com.example.demoSubDmain.dto;

public class SearchStudentReq {
    private String keyword;
    private String level;
    private int page = 1;
    private int size = 10;

    public SearchStudentReq() {
    }

    public SearchStudentReq(String keyword, String level, int page, int size) {
        this.keyword = keyword;
        this.level = level;
        this.page = page;
        this.size = size;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
