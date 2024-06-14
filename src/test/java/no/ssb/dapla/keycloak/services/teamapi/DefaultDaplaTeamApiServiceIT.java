package no.ssb.dapla.keycloak.services.teamapi;

import com.fasterxml.jackson.databind.JsonNode;
import no.ssb.dapla.keycloak.utils.Json;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static no.ssb.dapla.keycloak.Env.Var.*;
import static no.ssb.dapla.keycloak.Env.requiredEnv;

@Tag("integration")
@Disabled
class DefaultDaplaTeamApiServiceIT {
    private DefaultDaplaTeamApiService service;

    @BeforeEach
    public void setup() {
        service = new DefaultDaplaTeamApiService(DefaultDaplaTeamApiService.Config.builder()
                .teamApiUrl(URI.create("https://dapla-team-api-v2.prod-bip-app.ssb.no"))
                .build());
    }

    @Test
    public void testRequiredEnvVarsSet() {
        requiredEnv(DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_AUTH_URL);
        requiredEnv(DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_ID);
        requiredEnv(DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_SECRET);
        requiredEnv(TEST_USER_PRINCIPAL_NAME);
    }

    @Test
    public void testGetKeycloakToken() {
        String authToken = service.getAuthToken();
        System.out.println(authToken);
    }

    @Test
    public void testGetDaplaUserInfo() {
        String userPrincipalName = requiredEnv(TEST_USER_PRINCIPAL_NAME);
        JsonNode daplaInfoJson = service.getDaplaUserInfo(userPrincipalName);
        System.out.println(Json.prettyFrom(daplaInfoJson));
    }

}