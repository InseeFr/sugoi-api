package fr.insee.sugoi.sugoiapicore.utils.Exceptions;

public class EntityNotFoundException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String message;

    public EntityNotFoundException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
