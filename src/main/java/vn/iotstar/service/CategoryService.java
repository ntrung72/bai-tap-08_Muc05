package vn.iotstar.service;

import java.util.List;

import org.springframework.data.domain.Page;

import vn.iotstar.model.Category;

public interface CategoryService {
    List<Category> findAll(String keyword);

    List<Category> findAllByName();

    Page<Category> findPage(String keyword, int page, int size);

    Category findById(Integer id);

    Category save(Category category);

    void deleteById(Integer id);

    boolean nameExists(String name, Integer excludedId);
}