package vn.iotstar;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import vn.iotstar.model.Category;
import vn.iotstar.model.Product;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.ProductRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShopGraphqlIntegrationTests {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void cleanDatabaseBeforeTest() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @AfterEach
    void cleanDatabaseAfterTest() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void queriesSortFilterSearchAndPaginateProducts() throws Exception {
        Category milkTea = saveCategory("Trà sữa");
        Category coffee = saveCategory("Cà phê");

        saveProduct("Trà sữa đặc biệt", "35000", milkTea);
        saveProduct("Trà sữa truyền thống", "25000", milkTea);
        saveProduct("Cà phê sữa", "30000", coffee);

        String sortedResponse = executeGraphql("""
                query {
                    productsByPriceAsc {
                        name
                        price
                    }
                }
                """);
        assertNoGraphqlErrors(sortedResponse);
        assertBefore(
                sortedResponse,
                "Trà sữa truyền thống",
                "Cà phê sữa");
        assertBefore(
                sortedResponse,
                "Cà phê sữa",
                "Trà sữa đặc biệt");

        String filteredResponse = executeGraphql("""
                query {
                    productsByCategory(categoryId: %d) {
                        name
                        category {
                            name
                        }
                    }
                }
                """.formatted(milkTea.getId()));
        assertNoGraphqlErrors(filteredResponse);
        assertTrue(filteredResponse.contains("Trà sữa đặc biệt"));
        assertTrue(filteredResponse.contains("Trà sữa truyền thống"));
        assertFalse(filteredResponse.contains("Cà phê sữa"));

        String pageResponse = executeGraphql("""
                query {
                    productPage(keyword: "Trà sữa", page: 0, size: 1) {
                        content {
                            name
                        }
                        currentPage
                        totalPages
                        totalItems
                        pageSize
                    }
                }
                """);
        assertNoGraphqlErrors(pageResponse);
        assertTrue(pageResponse.contains("\"totalItems\":2"));
        assertTrue(pageResponse.contains("\"totalPages\":2"));
        assertTrue(pageResponse.contains("\"pageSize\":1"));
    }

    @Test
    void mutationsCreateUpdateAndDeleteCategoryAndProduct() throws Exception {
        String createCategoryResponse = executeGraphql("""
                mutation {
                    createCategory(input: {name: "Bánh ngọt"}) {
                        success
                        message
                        category {
                            id
                            name
                        }
                    }
                }
                """);
        assertNoGraphqlErrors(createCategoryResponse);
        assertTrue(createCategoryResponse.contains("\"success\":true"));

        Category category = categoryRepository
                .findByNameContainingIgnoreCaseOrderByIdDesc("Bánh ngọt")
                .get(0);

        String createProductResponse = executeGraphql("""
                mutation {
                    createProduct(input: {
                        name: "Bánh kem"
                        quantity: 8
                        price: 120000
                        description: "Bánh mới"
                        categoryId: %d
                    }) {
                        success
                        product {
                            id
                            name
                        }
                    }
                }
                """.formatted(category.getId()));
        assertNoGraphqlErrors(createProductResponse);
        assertTrue(createProductResponse.contains("\"success\":true"));

        Product product = productRepository
                .findByNameContainingIgnoreCaseOrderByIdDesc("Bánh kem")
                .get(0);

        String updateProductResponse = executeGraphql("""
                mutation {
                    updateProduct(
                        id: %d
                        input: {
                            name: "Bánh kem dâu"
                            quantity: 10
                            price: 135000
                            categoryId: %d
                        }
                    ) {
                        success
                        product {
                            name
                            quantity
                        }
                    }
                }
                """.formatted(product.getId(), category.getId()));
        assertNoGraphqlErrors(updateProductResponse);
        assertTrue(updateProductResponse.contains("Bánh kem dâu"));

        String deleteProductResponse = executeGraphql("""
                mutation {
                    deleteProduct(id: %d) {
                        success
                        deletedId
                    }
                }
                """.formatted(product.getId()));
        assertNoGraphqlErrors(deleteProductResponse);
        assertTrue(deleteProductResponse.contains("\"success\":true"));

        String deleteCategoryResponse = executeGraphql("""
                mutation {
                    deleteCategory(id: %d) {
                        success
                        deletedId
                    }
                }
                """.formatted(category.getId()));
        assertNoGraphqlErrors(deleteCategoryResponse);
        assertTrue(deleteCategoryResponse.contains("\"success\":true"));
    }

    private Category saveCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    private Product saveProduct(
            String name,
            String price,
            Category category) {
        Product product = new Product();
        product.setName(name);
        product.setQuantity(10);
        product.setPrice(new BigDecimal(price));
        product.setCategory(category);
        return productRepository.save(product);
    }

    private String executeGraphql(String query) throws Exception {
        String body = "{\"query\":\"" + escapeJson(query) + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/graphql"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300);
        return response.body();
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private void assertNoGraphqlErrors(String response) {
        assertFalse(response.contains("\"errors\""), response);
    }

    private void assertBefore(String value, String first, String second) {
        assertTrue(value.indexOf(first) >= 0, value);
        assertTrue(value.indexOf(second) >= 0, value);
        assertTrue(value.indexOf(first) < value.indexOf(second), value);
    }
}
