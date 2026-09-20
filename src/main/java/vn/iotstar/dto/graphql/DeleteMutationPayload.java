package vn.iotstar.dto.graphql;

public class DeleteMutationPayload {
    private final boolean success;
    private final String message;
    private final Integer deletedId;

    public DeleteMutationPayload(
            boolean success,
            String message,
            Integer deletedId) {
        this.success = success;
        this.message = message;
        this.deletedId = deletedId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Integer getDeletedId() {
        return deletedId;
    }
}