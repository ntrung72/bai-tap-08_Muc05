package vn.iotstar.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findAllByOrderByIdDesc();

    List<Product> findByNameContainingIgnoreCaseOrderByIdDesc(String keyword);

    List<Product> findAllByOrderByPriceAsc();

    List<Product> findByCategoryIdOrderByPriceAsc(Integer categoryId);

    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}
