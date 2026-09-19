package vn.iotstar.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.iotstar.model.Product;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.service.ProductService;
import vn.iotstar.util.TextEncodingUtils;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Product> findAll(String keyword) {
        String value = keyword == null ? "" : keyword.trim();
        List<Product> products;

        if (value.isEmpty()) {
            products = repository.findAllByOrderByIdDesc();
        } else {
            products = repository.findByNameContainingIgnoreCaseOrderByIdDesc(value);
        }

        products.forEach(this::normalizeText);
        return products;
    }

    @Override
    public List<Product> findAllByPriceAsc() {
        List<Product> products = repository.findAllByOrderByPriceAsc();
        products.forEach(this::normalizeText);
        return products;
    }

    @Override
    public List<Product> findByCategory(Integer categoryId) {
        List<Product> products = repository
                .findByCategoryIdOrderByPriceAsc(categoryId);
        products.forEach(this::normalizeText);
        return products;
    }

    @Override
    public Page<Product> findPage(String keyword, int page, int size) {
        String value = keyword == null ? "" : keyword.trim();
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "id"));

        return repository
                .findByNameContainingIgnoreCase(value, pageable)
                .map(this::normalizeText);
    }

    @Override
    public Product findById(Integer id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy sản phẩm có mã " + id + "."));
        return normalizeText(product);
    }

    @Override
    @Transactional
    public Product save(Product product) {
        product.setName(TextEncodingUtils.normalize(product.getName()).trim());
        product.setImage(trimToNull(product.getImage()));
        product.setDescription(trimToNull(product.getDescription()));
        return repository.save(product);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        findById(id);
        repository.deleteById(id);
        repository.flush();
    }

    @Override
    public boolean nameExists(String name, Integer excludedId) {
        if (name == null || name.isBlank()) {
            return false;
        }

        String normalizedName = TextEncodingUtils.normalize(name).trim();
        if (excludedId == null) {
            return repository.existsByNameIgnoreCase(normalizedName);
        }

        return repository.existsByNameIgnoreCaseAndIdNot(normalizedName, excludedId);
    }

    private Product normalizeText(Product product) {
        product.setName(TextEncodingUtils.normalize(product.getName()));
        product.setDescription(TextEncodingUtils.normalize(product.getDescription()));

        if (product.getCategory() != null) {
            product.getCategory().setName(
                    TextEncodingUtils.normalize(product.getCategory().getName()));
        }
        return product;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return TextEncodingUtils.normalize(value).trim();
    }
}
