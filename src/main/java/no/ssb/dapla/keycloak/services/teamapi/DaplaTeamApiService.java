package no.ssb.dapla.keycloak.services.teamapi;

import com.fasterxml.jackson.databind.JsonNode;

public interface DaplaTeamApiService {

    /**
     * Retrieve dapla user info from Dapla Team API
     *
     * @param userPrincipalName the user principal name to retrieve info for
     * @return the user info as a JsonNode
     */
    JsonNode getDaplaUserInfo(String userPrincipalName);

}
