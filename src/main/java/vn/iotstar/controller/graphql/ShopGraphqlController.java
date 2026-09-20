package vn.iotstar.controller.graphql;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import vn.iotstar.dto.graphql.CategoryInput;
import vn.iotstar.dto.graphql.CategoryMutationPayload;
import vn.iotstar.dto.graphql.CategoryPage;
import vn.iotstar.dto.graphql.DeleteMutationPayload;
import vn.iotstar.dto.graphql.ProductInput;
import vn.iotstar.dto.graphql.ProductMutationPayload;
import vn.iotstar.dto.graphql.ProductPage;
import vn.iotstar.model.Category;
import vn.iotstar.model.Product;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.IStorageService;
import vn.iotstar.service.ProductService;

@Controller
public class ShopGraphqlController {
    private static final int MAX_PAGE_SIZE = 100;

    private final CategoryService categoryService;
    private final ProductService productService;
    private final IStorageService storageService;

    public ShopGraphqlController(
            CategoryService categoryService,
            ProductService productService,
            IStorageService storageService) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.storageService = storageService;
    }

    @QueryMapping
    public List<Category> categories() {
        return categoryService.findAllByName();
    }

    @QueryMapping
    public Category categoryById(@Argument Integer id) {
        return categoryService.findById(id);
    }

    @QueryMapping
    public CategoryPage categoryPage(
            @Argument String keyword,
            @Argument int page,
            @Argument int size) {
        return new CategoryPage(
                categoryService.findPage(keyword, page, normalizeSize(size)));
    }

    @QueryMapping
    public List<Product> productsByPriceAsc() {
        return productService.findAllByPriceAsc();
    }

    @QueryMapping
    public List<Product> productsByCategory(@Argument Integer categoryId) {
        categoryService.findById(categoryId);
        return productService.findByCategory(categoryId);
    }

    @QueryMapping
    public Product productById(@Argument Integer id) {
        return productService.findById(id);
    }

    @QueryMapping
    public ProductPage productPage(
            @Argument String keyword,
            @Argument int page,
            @Argument int size) {
        return new ProductPage(
                productService.findPage(keyword, page, normalizeSize(size)));
    }

    @MutationMapping
    public CategoryMutationPayload createCategory(
            @Argument CategoryInput input) {
        String validationMessage = validateCategory(input, null);
        if (validationMessage != null) {
            return new CategoryMutationPayload(false, validationMessage, null);
        }

        Category category = new Category();
        category.setName(input.getName());
        category.setIcon(trimToNull(input.getIcon()));
        Category savedCategory = categoryService.save(category);

        return new CategoryMutationPayload(true, "Thêm danh mục thành công.", savedCategory);
    }

    @MutationMapping
    public CategoryMutationPayload updateCategory(
            @Argument Integer id,
            @Argument CategoryInput input) {
        Category category;

        try {
            category = categoryService.findById(id);
        } catch (IllegalArgumentException ex) {
            return new CategoryMutationPayload(false, ex.getMessage(), null);
        }

        String validationMessage = validateCategory(input, id);
        if (validationMessage != null) {
            return new CategoryMutationPayload(false, validationMessage, null);
        }

        String oldIcon = category.getIcon();
        String newIcon = trimToNull(input.getIcon());
        category.setName(input.getName());

        if (newIcon != null) {
            category.setIcon(newIcon);
        }

        Category savedCategory = categoryService.save(category);
        if (newIcon != null && !newIcon.equals(oldIcon)) {
            deleteImage(oldIcon);
        }

        return new CategoryMutationPayload(true, "Cập nhật danh mục thành công.", savedCategory);
    }

    @MutationMapping
    public DeleteMutationPayload deleteCategory(@Argument Integer id) {
        Category category;

        try {
            category = categoryService.findById(id);
            categoryService.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            return new DeleteMutationPayload(false, "Không thể xóa vì danh mục đang được sản phẩm sử dụng.", null);
        } catch (IllegalArgumentException ex) {
            return new DeleteMutationPayload(false, ex.getMessage(), null);
        }

        deleteImage(category.getIcon());
        return new DeleteMutationPayload(true, "Xóa danh mục thành công.", id);
    }

    @MutationMapping
    public ProductMutationPayload createProduct(
            @Argument ProductInput input) {
        String validationMessage = validateProduct(input, null);
        if (validationMessage != null) {
            return new ProductMutationPayload(false, validationMessage, null);
        }

        Category category;
        try {
            category = categoryService.findById(input.getCategoryId());
        } catch (IllegalArgumentException ex) {
            return new ProductMutationPayload(false, "Danh mục không tồn tại.", null);
        }

        Product product = new Product();
        setProductData(product, input, category, trimToNull(input.getImage()));
        Product savedProduct = productService.save(product);

        return new ProductMutationPayload(true, "Thêm sản phẩm thành công.", savedProduct);
    }

    @MutationMapping
    public ProductMutationPayload updateProduct(
            @Argument Integer id,
            @Argument ProductInput input) {
        Product product;

        try {
            product = productService.findById(id);
        } catch (IllegalArgumentException ex) {
            return new ProductMutationPayload(false, ex.getMessage(), null);
        }

        String validationMessage = validateProduct(input, id);
        if (validationMessage != null) {
            return new ProductMutationPayload(false, validationMessage, null);
        }

        Category category;
        try {
            category = categoryService.findById(input.getCategoryId());
        } catch (IllegalArgumentException ex) {
            return new ProductMutationPayload(false, "Danh mục không tồn tại.", null);
        }

        String oldImage = product.getImage();
        String newImage = trimToNull(input.getImage());
        String productImage = newImage == null ? oldImage : newImage;
        setProductData(product, input, category, productImage);
        Product savedProduct = productService.save(product);

        if (newImage != null && !newImage.equals(oldImage)) {
            deleteImage(oldImage);
        }

        return new ProductMutationPayload(true, "Cập nhật sản phẩm thành công.", savedProduct);
    }

    @MutationMapping
    public DeleteMutationPayload deleteProduct(@Argument Integer id) {
        Product product;

        try {
            product = productService.findById(id);
            productService.deleteById(id);
        } catch (IllegalArgumentException ex) {
            return new DeleteMutationPayload(false, ex.getMessage(), null);
        }

        deleteImage(product.getImage());
        return new DeleteMutationPayload(true, "Xóa sản phẩm thành công.", id);
    }

    private String validateCategory(CategoryInput input, Integer excludedId) {
        if (input.getName() == null || input.getName().isBlank()) {
            return "Tên danh mục không được để trống.";
        }

        String name = input.getName().trim();
        if (name.length() < 2 || name.length() > 255) {
            return "Tên danh mục phải từ 2 đến 255 ký tự.";
        }

        if (categoryService.nameExists(name, excludedId)) {
            return "Tên danh mục đã tồn tại.";
        }

        return null;
    }

    private String validateProduct(ProductInput input, Integer excludedId) {
        if (input.getName() == null || input.getName().isBlank()) {
            return "Tên sản phẩm không được để trống.";
        }

        String name = input.getName().trim();
        if (name.length() > 255) {
            return "Tên sản phẩm không được vượt quá 255 ký tự.";
        }

        if (input.getPrice() == null
                || input.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return "Đơn giá phải lớn hơn hoặc bằng 0.";
        }

        if (input.getQuantity() == null || input.getQuantity() < 0) {
            return "Số lượng phải lớn hơn hoặc bằng 0.";
        }

        if (input.getCategoryId() == null || input.getCategoryId() <= 0) {
            return "Danh mục không hợp lệ.";
        }

        if (productService.nameExists(name, excludedId)) {
            return "Tên sản phẩm đã tồn tại.";
        }

        return null;
    }

    private void setProductData(
            Product product,
            ProductInput input,
            Category category,
            String image) {
        product.setName(input.getName());
        product.setQuantity(input.getQuantity());
        product.setPrice(input.getPrice());
        product.setImage(image);
        product.setDescription(trimToNull(input.getDescription()));
        product.setCategory(category);
    }

    private int normalizeSize(int size) {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void deleteImage(String storeFilename) {
        if (storeFilename == null || storeFilename.isBlank()) {
            return;
        }

        try {
            storageService.delete(storeFilename);
        } catch (Exception ex) {
        }
    }
}