package no.ssb.dapla.keycloak.services.teamapi;

import com.fasterxml.jackson.databind.JsonNode;
import no.ssb.dapla.keycloak.utils.Json;
import org.jboss.logging.Logger;

public class DummyDaplaTeamApiService implements DaplaTeamApiService {
    private static final Logger log = Logger.getLogger(DummyDaplaTeamApiService.class);
    public static final String NAME = "Dummy";

    public DummyDaplaTeamApiService() {
        log.debug("Using DummyDaplaTeamApiService");
    }

    @Override
    public JsonNode getDaplaUserInfo(String userPrincipalName) {
        return Json.toJsonNode("""
                {
                  "teams": [
                    {
                      "uniform_name": "play-foeniks-a",
                      "section_code": "724",
                      "groups": [
                        "developers"
                      ]
                    }
                  ]
                }
                """);
    }
}
