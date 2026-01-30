package no.ssb.dapla.keycloak.services.teamapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import no.ssb.dapla.keycloak.DaplaKeycloakException;
import no.ssb.dapla.keycloak.services.model.DaplaGroup;
import no.ssb.dapla.keycloak.services.model.DaplaTeam;
import no.ssb.dapla.keycloak.services.model.DaplaUser;
import no.ssb.dapla.keycloak.services.model.DaplaUserInfo;
import no.ssb.dapla.keycloak.utils.Jq;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static no.ssb.dapla.keycloak.Env.Var.*;
import static no.ssb.dapla.keycloak.Env.requiredEnv;
import static no.ssb.dapla.keycloak.mappers.daplauserinfo.GroupCategory.ALLOWED_GROUP_CATEGORIES_PIPE_SEPARATED;

/**
 * Implementation that works against <a href=
 * "https://github.com/statisticsnorway/dapla-team-api">dapla-team-api</a>
 * ('old' rest API).
 */
@RequiredArgsConstructor
public class DefaultDaplaTeamApiService implements DaplaTeamApiService {

    public static final String NAME = "Default";
    private static final Logger log = Logger.getLogger(DefaultDaplaTeamApiService.class);
    private final OkHttpClient httpClient = new OkHttpClient();
    private final Config config;

    String getAuthToken() {
        String clientId = requiredEnv(DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_ID);
        String url = requiredEnv(DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_AUTH_URL);

        Request request = new Request.Builder()
                .url(url)
                .post(new FormBody.Builder()
                        .add("grant_type", "client_credentials")
                        .add("client_id", clientId)
                        .add("client_secret", requiredEnv(DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_SECRET))
                        .build())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch Dapla Team API keycloak token (client_id=%s, url=%s). Error: %s".formatted(clientId, url, response));
            }
            return Jq.queryOne(".access_token", response.body().string(), String.class)
                    .orElseThrow(() -> new DaplaKeycloakException("Missing access_token in response"));
        } catch (Exception e) {
            throw new DaplaKeycloakException("Error fetching keycloak token from " + url, e);
        }
    }

    @Override
    public DaplaUserInfo getDaplaUserInfo(String userPrincipalName, Pattern groupCategoriesToInclude) {

        String authToken = getAuthToken();
        String url = config.getTeamApiUrl().resolve("/users/%s?embed=teams,groups&select=groups.uniform_name".formatted(userPrincipalName)).toString();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", "Bearer " + authToken)
                .build();

        JsonNode userInfo = DaplaTeamApiService.fetchUserInfo(request, httpClient, userPrincipalName, log);

        DaplaUser user = jsonNodeToDaplaUser(userInfo);

        Map<String, DaplaTeam> teamNameToDaplaTeam = jsonNodeToDaplaTeamMap(userInfo);

        Set<String> groups = jsonNodeToGroupNames(userInfo);
        nestGroupsIntoTeams(groups, teamNameToDaplaTeam, groupCategoriesToInclude);

        return new DaplaUserInfo(user, new ArrayList<>(teamNameToDaplaTeam.values()));
    }

    private static DaplaUser jsonNodeToDaplaUser(JsonNode userInfo) {
        return Jq.queryOne("{email: .principal_name, name: .display_name, section_name, section_code}", userInfo, new TypeReference<Map<String, String>>() {
                })
                .map(fields -> new DaplaUser(fields.get("email"), fields.get("email"), fields.get("section_code"), fields.get("section_name"), null))
                .orElseThrow();
    }


    private Map<String, DaplaTeam> jsonNodeToDaplaTeamMap(JsonNode userInfo) {
        return Jq.queryOne("[._embedded.teams[]]", userInfo, new TypeReference<List<Map<String, String>>>() {
                })
                .orElse(Collections.emptyList())
                .stream()
                .map(fields -> new DaplaTeam(
                        fields.get("uniform_name"),
                        fields.get("display_name"),
                        fields.get("section_code"),
                        fields.get("section_name"),
                        fields.get("autonomy_level")
                ))
                .collect(Collectors.toMap(DaplaTeam::uniformName, team -> team));
    }

    private static void nestGroupsIntoTeams(Set<String> groupNames, Map<String, DaplaTeam> teams, Pattern groupCategoriesToInclude) {
        groupNames.stream()
                // If a filter is specified, only keep groups with suffixes matching the regex:
                // Note that this is only an intermediate filtering, since the regex will include disallowed suffixes if we have team names that share a prefix
                .filter(group -> Optional.ofNullable(groupCategoriesToInclude).map(regex -> regex.matcher(group).matches()).orElse(true))
                .map(DaplaGroup::new)
                .forEach((DaplaGroup group) -> {
                    // New API allows suffix after group category, so we'll handle it here as well
                    String teamName = group.name().split("-(" + ALLOWED_GROUP_CATEGORIES_PIPE_SEPARATED + ")(-.*)?")[0];

                    Optional.ofNullable(teams.get(teamName))
                            .orElseThrow()
                            .groups().add(group);
                });
    }

    private static Set<String> jsonNodeToGroupNames(JsonNode userInfo) {
        return Jq.queryOne("[._embedded.groups[].uniform_name]", userInfo, new TypeReference<Set<String>>() {
                })
                .orElse(Collections.emptySet());
    }

    @Value
    @Builder
    static public class Config {
        URI teamApiUrl;
    }
}
