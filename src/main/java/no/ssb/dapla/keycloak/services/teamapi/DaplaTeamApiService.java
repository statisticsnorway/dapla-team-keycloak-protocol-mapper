package no.ssb.dapla.keycloak.services.teamapi;

import com.fasterxml.jackson.databind.JsonNode;
import no.ssb.dapla.keycloak.DaplaKeycloakException;
import no.ssb.dapla.keycloak.utils.Json;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jboss.logging.Logger;

import java.io.IOException;

public interface DaplaTeamApiService {
    /**
     * Retrieve dapla user info from Dapla Team API
     *
     * @param userPrincipalName the user principal name to retrieve info for
     * @return the user info as a JsonNode
     */
    JsonNode getDaplaUserInfo(String userPrincipalName);


    static JsonNode fetchUserInfo(Request request, OkHttpClient httpClient, String userPrincipalName,
                                  Logger log) {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch Dapla team user info for %s. Error: %s"
                        .formatted(userPrincipalName, response));
            }
            String jsonResponse = response.body().string();
            log.debug("Response body: " + jsonResponse);
            return Json.toJsonNode(jsonResponse);
        } catch (Exception e) {
            throw new DaplaKeycloakException("Error fetching Dapla userinfo for " + userPrincipalName, e);
        }
    }
}
