package vn.iotstar.dto.graphql;

import vn.iotstar.model.Product;

public class ProductMutationPayload {
    private final boolean success;
    private final String message;
    private final Product product;

    public ProductMutationPayload(
            boolean success,
            String message,
            Product product) {
        this.success = success;
        this.message = message;
        this.product = product;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Product getProduct() {
        return product;
    }
}
