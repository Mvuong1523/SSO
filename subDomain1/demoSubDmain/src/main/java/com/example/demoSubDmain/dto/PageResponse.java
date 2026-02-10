package com.example.demoSubDmain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class PageResponse<T> {
    private List<T> data;
    private int total;
    private int page;
    private int size;

    public PageResponse() {
    }

    public PageResponse(List<T> data, int total, int page, int size) {
        this.data = data;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
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

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private List<T> data;
        private int total;
        private int page;
        private int size;

        public Builder<T> data(List<T> data) {
            this.data = data;
            return this;
        }

        public Builder<T> total(int total) {
            this.total = total;
            return this;
        }

        public Builder<T> page(int page) {
            this.page = page;
            return this;
        }

        public Builder<T> size(int size) {
            this.size = size;
            return this;
        }

        public PageResponse<T> build() {
            return new PageResponse<>(data, total, page, size);
        }
    }
}
