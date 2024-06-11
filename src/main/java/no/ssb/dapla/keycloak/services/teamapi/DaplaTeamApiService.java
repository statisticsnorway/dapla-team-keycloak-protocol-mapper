package no.ssb.dapla.keycloak.services.teamapi;

import com.fasterxml.jackson.databind.JsonNode;

public interface DaplaTeamApiService {

    JsonNode getDaplaUserInfo(String userPrincipalName);

}
