package vn.iotstar.dto.graphql;

import java.util.List;

import org.springframework.data.domain.Page;

import vn.iotstar.model.Product;

public class ProductPage {
    private final List<Product> content;
    private final int currentPage;
    private final int totalPages;
    private final long totalItems;
    private final int pageSize;
    private final boolean first;
    private final boolean last;

    public ProductPage(Page<Product> page) {
        this.content = page.getContent();
        this.currentPage = page.getNumber();
        this.totalPages = page.getTotalPages();
        this.totalItems = page.getTotalElements();
        this.pageSize = page.getSize();
        this.first = page.isFirst();
        this.last = page.isLast();
    }

    public List<Product> getContent() {
        return content;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }
}