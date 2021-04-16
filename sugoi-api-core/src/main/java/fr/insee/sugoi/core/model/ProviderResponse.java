package fr.insee.sugoi.core.model;

public class ProviderResponse {

    ProviderResponseStatus status;
    String RequestId;
    String EntityId;
    RuntimeException exception;

    public enum ProviderResponseStatus {
        OK, KO,
        // Request is saved but can be refused
        REQUESTED,
        // Resquest is executed but can be visible in few time
        ACCEPTED;
    }

    public ProviderResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ProviderResponseStatus status) {
        this.status = status;
    }

    public String getEntityId() {
        return EntityId;
    }

    public void setEntityId(String entityId) {
        EntityId = entityId;
    }

    public String getRequestId() {
        return RequestId;
    }

    public void setRequestId(String requestId) {
        RequestId = requestId;
    }

    public RuntimeException getException() {
        return exception;
    }

    public void setException(RuntimeException exception) {
        this.exception = exception;
    }

}
