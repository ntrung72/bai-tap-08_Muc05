package vn.iotstar.service;

import java.util.List;

import org.springframework.data.domain.Page;

import vn.iotstar.model.Product;

public interface ProductService {
    List<Product> findAll(String keyword);

    List<Product> findAllByPriceAsc();

    List<Product> findByCategory(Integer categoryId);

    Page<Product> findPage(String keyword, int page, int size);

    Product findById(Integer id);

    Product save(Product product);

    void deleteById(Integer id);

    boolean nameExists(String name, Integer excludedId);
}