package no.ssb.dapla.keycloak.services.teamapi;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import no.ssb.dapla.keycloak.DaplaKeycloakException;
import no.ssb.dapla.keycloak.utils.Jq;
import no.ssb.dapla.keycloak.utils.Json;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;

import static no.ssb.dapla.keycloak.Env.Var.*;
import static no.ssb.dapla.keycloak.Env.requiredEnv;

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
    public JsonNode getDaplaUserInfo(String userPrincipalName) {
        String authToken = getAuthToken();
        String url = config.getTeamApiUrl().resolve("/users/%s?embed=teams,groups&select=groups.uniform_name".formatted(userPrincipalName)).toString();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", "Bearer " + authToken)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch Dapla team user info for %s. Error: %s".formatted(userPrincipalName, response));
            }
            String jsonResponse = response.body().string();
            log.debug("Response body: " + jsonResponse);
            return Json.toJsonNode(jsonResponse);
        } catch (Exception e) {
            throw new DaplaKeycloakException("Error fetching Dapla userinfo for " + userPrincipalName, e);
        }
    }
/*
    public JsonNode getDaplaUserInfo2(String userPrincipalName) {
        String authToken = getAuthToken();
        String url = config.getTeamApiUrl().resolve("/users/%s?embed=teams,groups&select=groups.uniform_name".formatted(userPrincipalName)).toString();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", "Bearer " + authToken)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch Dapla team user info for %s. Error: %s".formatted(userPrincipalName, response));
            }
            String jsonResponse = response.body().string();
            log.debug("Response body: " + jsonResponse);

            Map<String, Object> daplaInfo = new HashMap<>();
            Optional<Pattern> groupIncludeFilter = Optional.ofNullable(config.getGroupSuffixIncludeRegex())
                    .map(suffixRegex -> Pattern.compile(".*-(" + suffixRegex + ")"));

            List<String> groups = Jq.queryOne("[._embedded.groups[].uniform_name]", jsonResponse, new TypeReference<List<String>>() {
                    })
                    .orElse(Collections.emptyList())
                    .stream()
                    // If a filter is specified, only keep groups with suffixes matching the regex:
                    .filter(group -> groupIncludeFilter.map(regex -> regex.matcher(group).matches()).orElse(true))
                    .toList();

            List<ObjectNode> teams = Jq.queryOne("[._embedded.teams[]]", jsonResponse, new TypeReference<List<ObjectNode>>() {})
                    .orElse(Collections.emptyList());

            // Filter out unwanted team properties
            teams.forEach(team -> {
                Iterator<String> fields = team.fieldNames();
                while (fields.hasNext()) {
                    String field = fields.next();
                    if (!"uniform_name".equals(field) && !config.getDaplaTeamProps().contains(field)) {
                        fields.remove();
                    }
                }
            });

            for (String userProp : config.getDaplaUserProps()) {
                Jq.queryOne(".%s".formatted(userProp), jsonResponse, String.class)
                        .ifPresent(value -> daplaInfo.put(userProp, value));
            }
            if (config.isNestedTeams()) {
                daplaInfo.put("teams", nestGroupsIntoTeams(teams, groups));
            } else {
                daplaInfo.put("teams", teams.stream().map(team -> team.get("uniform_name").asText()).toList());
                daplaInfo.put("groups", groups);
            }

            return Json.toJsonNode(daplaInfo);
        } catch (Exception e) {
            throw new DaplaKeycloakException("Error fetching Dapla userinfo for " + userPrincipalName, e);
        }
    }

    private List<ObjectNode> nestGroupsIntoTeams(List<ObjectNode> teams, List<String> groups) {
        return teams.stream()
                .map(team -> {
                    String teamUniformName = team.get("uniform_name").asText();
                    ArrayNode teamGroupsArrayNode = team.putArray("groups");
                    groups.stream()
                            .filter(group -> group.startsWith(teamUniformName))
                            .map(group -> group.substring(teamUniformName.length() + 1))
                            .forEach(suffix -> teamGroupsArrayNode.add(suffix));
                    return team;
                }).toList();
    }
*/

    @Value
    @Builder
    static public class Config {
        URI teamApiUrl;
    }
}
