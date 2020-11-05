package fr.insee.sugoi.services.utils;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import fr.insee.sugoi.model.Technique.MyPage;

@Component
public class ResponseUtils {

    private static <T> URI determinerResultatsSuivants(MyPage<T> page) {
        UriComponentsBuilder uriComponents = UriComponentsBuilder.newInstance().scheme("http").host("www.google.com")
                .path("/");
        if (page.isHasMoreResult()) {
            if (page.getSearchCookie() != null) {

            }
            if (page.getNextStart() > 0) {
                uriComponents = uriComponents.queryParam("start", page.getNextStart());
            }
        }
        return uriComponents.build().toUri();
    }

    public static HttpHeaders ajouterHeadersTotalSizeEtNextLocation(MyPage<?> page, boolean premiereRecherche,
            int sizeDemande) {
        HttpHeaders headers = new HttpHeaders();
        // Détermination du nombre de résultats totaux si possible (toujours renvoyé
        // eventuellement -1
        // si taille inconnue )
        if (page.getPageSize() == -1 && premiereRecherche && page.getResults().size() < sizeDemande) {
            headers.add("X-Total-Size", Integer.toString(page.getResults().size()));
        } else {
            headers.add("X-Total-Size", Integer.toString(page.getTotalElements()));
        }

        // Ajout nextLocation le cas échéant
        if (page.isHasMoreResult()) {
            URI uri = determinerResultatsSuivants(page);
            headers.add("nextLocation", uri.toString());
        }
        return headers;
    }
}
