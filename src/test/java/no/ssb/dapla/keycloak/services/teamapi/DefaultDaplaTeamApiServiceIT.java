package no.ssb.dapla.keycloak.services.teamapi;

import com.fasterxml.jackson.databind.JsonNode;
import no.ssb.dapla.keycloak.services.teamapi.DefaultDaplaTeamApiService.Config;
import no.ssb.dapla.keycloak.utils.Env;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

@Tag("integration")
@Disabled
class DefaultDaplaTeamApiServiceIT {
    private DefaultDaplaTeamApiService service;

    @BeforeEach
    public void setup() {
        service = new DefaultDaplaTeamApiService(Config.builder()
                .teamApiUrl(URI.create("https://dapla-team-api-v2.staging-bip-app.ssb.no"))
                .keycloakUrl(URI.create("https://auth.external.test.ssb.cloud.nais.io"))
                .daplaUserProps(Set.of("section_code"))
                .daplaTeamProps(Set.of("section_code"))
                .nestedTeams(true)
                .build());
    }

    @Test
    public void testRequiredEnvVarsSet() {
        Env.requiredVar("DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_ID");
        Env.requiredVar("DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_SECRET");
        Env.requiredVar("TEST_USER_PRINCIPAL_NAME");
    }

    @Test
    public void testGetKeycloakToken() {
        service.getAuthToken();
    }

    @Test
    public void testGetDaplaUserInfo() {
        String userPrincipalName = Env.requiredVar("TEST_USER_PRINCIPAL_NAME");
        JsonNode daplaInfoJson = service.getDaplaUserInfo(userPrincipalName);
        System.out.println(daplaInfoJson);
    }

}