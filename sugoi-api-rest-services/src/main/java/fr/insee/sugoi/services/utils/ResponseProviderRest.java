package fr.insee.sugoi.services.utils;

import org.springframework.http.HttpStatus;

import fr.insee.sugoi.core.model.ProviderResponse;

public class ResponseProviderRest {

    public static HttpStatus getHttpCodeFromResponseStatus(ProviderResponse response) {
        switch (response.getStatus()) {
        case OK:
            return HttpStatus.OK;
        case ACCEPTED:
            return HttpStatus.ACCEPTED;
        default:
            return null;
        }

    }

}
