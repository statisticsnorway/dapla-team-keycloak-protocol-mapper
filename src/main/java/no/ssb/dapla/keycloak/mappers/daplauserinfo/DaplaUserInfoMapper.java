package no.ssb.dapla.keycloak.mappers.daplauserinfo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.auto.service.AutoService;
import no.ssb.dapla.keycloak.DaplaKeycloakException;
import no.ssb.dapla.keycloak.mappers.AbstractTokenMapper;
import no.ssb.dapla.keycloak.mappers.ConfigPropertyType;
import no.ssb.dapla.keycloak.services.teamapi.DaplaTeamApiService;
import no.ssb.dapla.keycloak.services.teamapi.DefaultDaplaTeamApiService;
import no.ssb.dapla.keycloak.services.teamapi.DummyDaplaTeamApiService;
import no.ssb.dapla.keycloak.utils.Jq;
import no.ssb.dapla.keycloak.utils.Json;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.representations.IDToken;

import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static no.ssb.dapla.keycloak.Env.Var.TEST_USER_PRINCIPAL_NAME;
import static no.ssb.dapla.keycloak.Env.env;

@AutoService(ProtocolMapper.class)
public class DaplaUserInfoMapper extends AbstractTokenMapper {
    public static final String PROVIDER_ID = "oidc-dapla-userinfo-mapper";

    public static class ConfigPropertyKey {
        public static final String API_URL = "dapla.team-api-url";
        public static final String API_IMPL = "dapla.team-api-impl";
        public static final String NESTED_TEAMS = "dapla.userinfo.nested";
        public static final String GROUP_SUFFIX_INCLUDE_REGEX = "dapla.userinfo.group-suffix-include-regex";
        public static final String EXCLUDE_TEAMS_WITHOUT_GROUPS = "dapla.userinfo.exclude-teams-without-groups";
        public static final String DAPLA_USER_PROPS = "dapla.userinfo.user-props";
        public static final String DAPLA_TEAM_PROPS = "dapla.userinfo.team-props";
    }

    public DaplaUserInfoMapper() {
        super(PROVIDER_ID,
                configProperty()
                        .name(ConfigPropertyKey.API_IMPL)
                        .label("Dapla Team API Impl")
                        .helpText("""
                                The API implementation.
                                Default: Online Dapla Team API
                                Dummy: Offline, dummy replacement instead of a real API invocation.""")
                        .type(ConfigPropertyType.LIST)
                        .options(DefaultDaplaTeamApiService.NAME, DummyDaplaTeamApiService.NAME)
                        .defaultValue(DefaultDaplaTeamApiService.NAME)
                        .build(),

                configProperty()
                        .name(ConfigPropertyKey.API_URL)
                        .label("Dapla Team API URL")
                        .helpText("""
                                Root URL for the Dapla Team API.
                                This is not relevant if 'Dapla Team API Impl' is Dummy.""")
                        .type(ConfigPropertyType.STRING)
                        .defaultValue("https://dapla-team-api-v2.prod-bip-app.ssb.no")
                        .build(),

                configProperty()
                        .name(ConfigPropertyKey.NESTED_TEAMS)
                        .label("Nested teams")
                        .helpText("""
                                Nest groups and other team info in team objects.
                                If false, teams and groups and other info are included as properties on root level.""")
                        .type(ConfigPropertyType.BOOLEAN)
                        .defaultValue(true)
                        .build(),

                configProperty()
                        .name(ConfigPropertyKey.GROUP_SUFFIX_INCLUDE_REGEX)
                        .label("Group Suffix Include Regex")
                        .helpText("""
                                Filter group names by their suffix. Only groups with suffixes matching the regex will be included.
                                For example, to include only ‘developers’ and ‘data-admins’ groups, use the regex ‘developers|data-admins’.
                                If not specified, all groups are included.
                                """)
                        .type(ConfigPropertyType.STRING)
                        .build(),

                configProperty()
                        .name(ConfigPropertyKey.EXCLUDE_TEAMS_WITHOUT_GROUPS)
                        .label("Exclude teams without groups")
                        .helpText("""
                                If false, all teams are included regardless of group membership.
                                If true, only teams where the user is member of a relevant group are included. This works together with the 'Group Suffix Include Regex' setting.
                                """)
                        .type(ConfigPropertyType.BOOLEAN)
                        .defaultValue(true)
                        .build(),

                configProperty()
                        .name(ConfigPropertyKey.DAPLA_USER_PROPS)
                        .label("Dapla user properties to include")
                        .helpText("""
                                Comma-separated list of Dapla user properties to include on root level of the claim.
                                Such as: azure_ad_id, division_code, division_name, section_code, section_name""")
                        .type(ConfigPropertyType.STRING) // TODO: Until https://github.com/keycloak/keycloak/issues/26794 is fixed, we have to use a (comma-spearated) String here
                        .build(),

                configProperty()
                        .name(ConfigPropertyKey.DAPLA_TEAM_PROPS)
                        .label("Dapla team properties to include")
                        .helpText("""
                                 Comma-separated list of Dapla team properties to include in the claim's team objects.
                                 Only applicable if teams info are nested.
                                 Such as: autonomy_level, display_name, section_code, source_data_classification, statistical_products""")
                        .type(ConfigPropertyType.STRING) // TODO: Until https://github.com/keycloak/keycloak/issues/26794 is fixed, we have to use a (comma-spearated) String here
                        .build()
                );

        // Allow the mapper to configure the target value to be "JSON" (required)
        OIDCAttributeMapperHelper.addJsonTypeConfig(configProperties);
    }

    @Override
    protected String helpText() {
        return "Adds a 'dapla' user info claim, with selected data retrieved from Dapla Team API";
    }

    @Override
    protected Object mapToClaim(IDToken token, ProtocolMapperModel model, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
        debugLog(model,"Retrieve Dapla userinfo");
        DaplaTeamApiService teamApiService = teamApiService(model);
        JsonNode daplaUserInfoJson = teamApiService.getDaplaUserInfo(userPrincipalName(userSession));
        return createClaim(Json.from(daplaUserInfoJson), model);
    }

    /**
     * Get the current user principal name from the user session.
     *
     * @param userSession The user session
     * @return The user principal name, such as abc@domain.com
     */
    String userPrincipalName(UserSessionModel userSession) {
        String userPrincipalName = Optional.ofNullable(userSession.getUser().getEmail())
                .orElse(env(TEST_USER_PRINCIPAL_NAME, null));
        if (userPrincipalName == null) {
            throw new DaplaKeycloakException("Missing user principal name. Not found in user session or as TEST_USER_PRINCIPAL_NAME env var.");
        }
        return userPrincipalName;
    }

    /**
     * Instantiate Dapla Team API service based on the configuration.
     *
     * @param model
     * @return
     */
    DaplaTeamApiService teamApiService(ProtocolMapperModel model) {
        String apiImpl = getConfigString(model, ConfigPropertyKey.API_IMPL);
        debugLog(model, "Use " + apiImpl + " Dapla Team API implementation");
        if (DefaultDaplaTeamApiService.NAME.equals(apiImpl)) {
            return new DefaultDaplaTeamApiService(DefaultDaplaTeamApiService.Config.builder()
                    .teamApiUrl(URI.create(getConfigString(model, ConfigPropertyKey.API_URL)))
                    .build());
        }
        else if (DummyDaplaTeamApiService.NAME.equals(apiImpl)) {
            return new DummyDaplaTeamApiService();
        }
        else {
            throw new DaplaKeycloakException("Unsupported Team API implementation: " + apiImpl);
        }
    }

    /**
     * Create the claim from the Dapla user info JSON.
     * <p>
     *     The claim will contain the user properties specified in the configuration, as well as the groups and teams.
     * </p>
     *
     * @param daplaUserInfoJson The Dapla user info JSON as returned from DaplaTeamApiService
     * @param model The ProtocolMapperModel
     * @return The claim as a JSON string
     */
    String createClaim(String daplaUserInfoJson, ProtocolMapperModel model) {
        Map<String, Object> claim = new HashMap<>();

        List<ObjectNode> teams = Jq.queryOne("[._embedded.teams[]]", daplaUserInfoJson, new TypeReference<List<ObjectNode>>() {})
                .orElse(Collections.emptyList());

        Set<String> groups = Jq.queryOne("[._embedded.groups[].uniform_name]", daplaUserInfoJson, new TypeReference<Set<String>>() {})
                .orElse(Collections.emptySet()).stream()
                // If a filter is specified, only keep groups with suffixes matching the regex:
                // Note that this is only an intermediate filtering, since the regex will include disallowed suffixes if we have team names that share a prefix
                .filter(group -> groupIncludeFilter(model).map(regex -> regex.matcher(group).matches()).orElse(true))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Retain only configured team properties
        Set<String> teamProps = getConfigStringSet(model, ConfigPropertyKey.DAPLA_TEAM_PROPS);
        teams.forEach(team -> {
            Iterator<String> fields = team.fieldNames();
            while (fields.hasNext()) {
                String field = fields.next();
                if (!"uniform_name".equals(field) && !teamProps.contains(field)) {
                    fields.remove();
                }
            }
        });

        // Query for and add configured user properties to claim
        List<String> userProps = getConfigStringList(model, ConfigPropertyKey.DAPLA_USER_PROPS);
        for (String userProp : userProps) {
                Jq.queryOne(".%s".formatted(userProp), daplaUserInfoJson, String.class)
                        .ifPresent(value -> claim.put(userProp, value));
            }

        boolean nestTeams = getConfigBoolean(model, ConfigPropertyKey.NESTED_TEAMS);
        boolean excludeTeamsWithoutGroups = getConfigBoolean(model, ConfigPropertyKey.EXCLUDE_TEAMS_WITHOUT_GROUPS);

        List<ObjectNode> nestedTeams = nestGroupsIntoTeams(teams, groups, excludeTeamsWithoutGroups);
        if (nestTeams) {
            claim.put("teams", nestedTeams);
        } else {
            claim.put("teams", nestedTeams.stream().map(team -> team.get("uniform_name").asText()).toList());
            claim.put("groups", groups);
        }

        return Json.from(claim);
    }

    /**
     * Nest groups into teams. Each team object will contain a 'groups' array with the group suffixes.
     * In addition to other team properties, as configured.
     *
     * @param teams team objects
     * @param groupNames group uniform names
     * @return List of team objects with nested groups
     */
    List<ObjectNode> nestGroupsIntoTeams(List<ObjectNode> teams, Collection<String> groupNames, boolean excludeTeamsWithoutGroups) {
        Set<String> allTeamNames = teams.stream()
                .map(team -> team.get("uniform_name").asText())
                .collect(Collectors.toSet());
        Set<String> allowedSuffixes = GroupSuffixFilter.allowedSuffixes(allTeamNames, groupNames);

        return teams.stream()
                .map(team -> {
                    String teamUniformName = team.get("uniform_name").asText();
                    ArrayNode teamGroupsArrayNode = team.putArray("groups");
                    groupNames.stream()
                            .filter(group -> group.startsWith(teamUniformName))
                            .map(group -> group.substring(teamUniformName.length() + 1))
                            .filter(allowedSuffixes::contains)
                            .forEach(teamGroupsArrayNode::add);
                    return team;
                })
                .filter(team -> !excludeTeamsWithoutGroups || !team.get("groups").isEmpty())
                .toList();
    }

    Optional<Pattern> groupIncludeFilter(ProtocolMapperModel model) {
        // TODO: Validate somehow that the regex is valid?
        // If the user does not provide a valid regex, filtering is likely to fail spectacularly.
        return Optional.ofNullable(getConfigString(model, ConfigPropertyKey.GROUP_SUFFIX_INCLUDE_REGEX))
                .map(suffixRegex -> Pattern.compile(".*-(" + suffixRegex + ")"));
    }
}
