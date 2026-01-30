package no.ssb.dapla.keycloak.services.teamapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import no.ssb.dapla.keycloak.services.model.DaplaGroup;
import no.ssb.dapla.keycloak.services.model.DaplaTeam;
import no.ssb.dapla.keycloak.services.model.DaplaUser;
import no.ssb.dapla.keycloak.services.model.DaplaUserInfo;
import no.ssb.dapla.keycloak.utils.Jq;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
    public DaplaUserInfo getDaplaUserInfo(String userPrincipalName, Pattern groupCategoriesToInclude) {
        String saToken = requiredEnv(DAPLA_TEAM_PROTOCOL_MAPPER_DAPLA_API_SA_TOKEN);

        String body = "{user(email:\"" + userPrincipalName + "\"){name email section{name code}isSectionManager teams{nodes{team{slug displayName isManaged section{name code}}groups{name}}}}}";

        Request request = new Request.Builder()
                .url(config.apiUrl)
                .header("Authorization", "Bearer " + saToken)
                .post(RequestBody.create(body, MediaType.get("application/json; charset=utf-8")))
                .build();

        JsonNode userInfo = DaplaTeamApiService.fetchUserInfo(request, httpClient, userPrincipalName, log);

        DaplaUser user = Jq.queryOne(".data.user | { name, email, section_name: .section.name, section_code: .section.code, isSectionManager}", userInfo, new TypeReference<Map<String, String>>() {
                }).map(fields -> new DaplaUser(
                        fields.get("email"),
                        fields.get("email"),
                        fields.get("section_code"),
                        fields.get("section_name"),
                        Boolean.valueOf(fields.get("isSectionManager")))
                )
                .orElseThrow();


        List<DaplaTeam> teams = Jq.queryOne(".data.user.teams[]", userInfo, new TypeReference<List<ObjectNode>>() {
                })
                .orElse(Collections.emptyList())
                .stream()
                .map(teamAndGroupNode -> jsonNodeToDaplaTeamWithGroups(teamAndGroupNode, groupCategoriesToInclude))
                .toList();

        return new DaplaUserInfo(user, teams);
    }


    private static DaplaTeam jsonNodeToDaplaTeamWithGroups(ObjectNode teamAndGroupNode, Pattern categoriesToInclude) {
            var teamNode = teamAndGroupNode.get("team");
            String autonomyLevel = teamNode.get("isManged").booleanValue() ? "MANAGED" : "SELF_MANAGED";
            var team = new DaplaTeam(
                    teamNode.get("slug").textValue(),
                    teamNode.get("displayName").textValue(),
                    teamNode.get("section").get("code").textValue(),
                    teamNode.get("section").get("name").textValue(),
                    autonomyLevel
            );

            for (final JsonNode groupNode : teamAndGroupNode.get("groups")) {
                DaplaGroup group = new DaplaGroup(groupNode.get("name").asText());
                if (categoriesToInclude == null || categoriesToInclude.matcher(group.name()).matches()) {
                    team.groups().add(group);
                }
            }

            return team;
    }

    @Value
    @Builder
    static public class Config {
        String apiUrl;
    }
}
