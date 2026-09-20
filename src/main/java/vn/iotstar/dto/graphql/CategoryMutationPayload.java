package vn.iotstar.dto.graphql;

import vn.iotstar.model.Category;

public class CategoryMutationPayload {
    private final boolean success;
    private final String message;
    private final Category category;

    public CategoryMutationPayload(
            boolean success,
            String message,
            Category category) {
        this.success = success;
        this.message = message;
        this.category = category;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Category getCategory() {
        return category;
    }
}