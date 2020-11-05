package fr.insee.sugoi.sugoiapicore.utils.Exceptions;

public class RealmNotFoundException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String message;

    public RealmNotFoundException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
