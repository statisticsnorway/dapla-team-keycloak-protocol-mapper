package no.ssb.dapla.keycloak.services.teamapi;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import okhttp3.*;
import org.jboss.logging.Logger;

import static no.ssb.dapla.keycloak.Env.Var.DAPLA_TEAM_PROTOCOL_MAPPER_DAPLA_API_SA_TOKEN;
import static no.ssb.dapla.keycloak.Env.requiredEnv;

/**
 * Implementation that queries the
 * <a href="https://github.com/statisticsnorway/dapla-api">dapla-api</a>
 * (GraphQL):
 */
@RequiredArgsConstructor
public class DaplaApiService implements DaplaTeamApiService {

    public static final String NAME = "Dapla-api";
    private static final Logger log = Logger.getLogger(DaplaApiService.class);
    private final OkHttpClient httpClient = new OkHttpClient();
    private final Config config;

    @Override
    public JsonNode getDaplaUserInfo(String userPrincipalName) {
        String saToken = requiredEnv(DAPLA_TEAM_PROTOCOL_MAPPER_DAPLA_API_SA_TOKEN);

        String body = """
                {"query":"TODO"}
                """;

        Request request = new Request.Builder()
                .url(config.apiUrl)
                .header("Authorization", "Bearer " + saToken)
                .post(RequestBody.create(body, MediaType.get("application/json; charset=utf-8")))
                .build();

        return DaplaTeamApiService.fetchUserInfo(request, httpClient, userPrincipalName, log);
    }

    @Value
    @Builder
    static public class Config {
        String apiUrl;
    }
}
