package vn.iotstar.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByNameContainingIgnoreCaseOrderByIdDesc(String keyword);

    List<Category> findAllByOrderByIdDesc();

    List<Category> findAllByOrderByNameAsc();

    Page<Category> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}
