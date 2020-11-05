package fr.insee.sugoi.sugoiapicore.service.broker;

import java.util.HashMap;
import java.util.Map;

public class BrokerRequest {

    public String requestBody;
    public String method;
    public Map<String, String> queryParams = new HashMap<>();

    public String getRequestBody() {
        return this.requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getQueryParams() {
        return this.queryParams;
    }

}
