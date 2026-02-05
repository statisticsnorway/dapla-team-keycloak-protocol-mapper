package no.ssb.dapla.keycloak.mappers.daplauserinfo;

import com.google.auto.service.AutoService;
import no.ssb.dapla.keycloak.DaplaKeycloakException;
import no.ssb.dapla.keycloak.mappers.AbstractTokenMapper;
import no.ssb.dapla.keycloak.mappers.ConfigPropertyType;
import no.ssb.dapla.keycloak.services.model.DaplaGroup;
import no.ssb.dapla.keycloak.services.model.DaplaTeam;
import no.ssb.dapla.keycloak.services.model.DaplaUserInfo;
import no.ssb.dapla.keycloak.services.api.*;
import no.ssb.dapla.keycloak.utils.Json;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.representations.IDToken;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
                                Default: Online Dapla Team API (old, REST)
                                Dapla-api: Online Dapla API (new, graphql)
                                Dummy: Offline, dummy replacement instead of a real API invocation.""")
                        .type(ConfigPropertyType.LIST)
                        .options(DaplaTeamApi.NAME, DaplaApi.NAME, DummyApi.NAME)
                        .defaultValue(DaplaTeamApi.NAME)
                        .build(),

                configProperty()
                        .name(ConfigPropertyKey.API_URL)
                        .label("Dapla Team API URL")
                        .helpText("""
                                Root URL for the API.
                                This is not relevant if 'Dapla Team API Impl' is Dummy.""")
                        .type(ConfigPropertyType.STRING)
                        .defaultValue("http://dapla-team-api.dapla-platform")
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
                                Filter group names by their category (developers, data-admin, etc). Only groups with categories matching the regex will be included.
                                For example, to include only ‘developers’ and ‘data-admins’ groups, use the regex ‘developers|data-admins’.
                                If not specified, all groups are included.
                                Suffix is the old name for category - and should not be confused with how we use 'suffix' in the new dapla api.
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
                                Such as: section_code, section_name""")
                        .type(ConfigPropertyType.STRING) // TODO: Until https://github.com/keycloak/keycloak/issues/26794 is fixed, we have to use a (comma-spearated) String here
                        .build(),

                configProperty()
                        .name(ConfigPropertyKey.DAPLA_TEAM_PROPS)
                        .label("Dapla team properties to include")
                        .helpText("""
                                Comma-separated list of Dapla team properties to include in the claim's team objects.
                                Only applicable if teams info are nested.
                                Such as: autonomy_level, display_name, section_code""")
                        .type(ConfigPropertyType.STRING) // TODO: Until https://github.com/keycloak/keycloak/issues/26794 is fixed, we have to use a (comma-spearated) String here
                        .build()
        );

        // Allow the mapper to configure the target value to be "JSON" (required)
        OIDCAttributeMapperHelper.addJsonTypeConfig(configProperties);
    }

    @Override
    protected String helpText() {
        return "Adds a 'dapla' user info claim, with selected data retrieved from Dapla Api or Dapla Team Api";
    }

    @Override
    protected Object mapToClaim(IDToken token, ProtocolMapperModel model, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
        debugLog(model, "Retrieve Dapla userinfo");
        ApiService teamApiService = teamApiService(model);

        DaplaUserInfo daplaUserInfo = teamApiService.getDaplaUserInfo(userPrincipalName(userSession), groupIncludeFilter(model));

        return createClaim(daplaUserInfo, model);
    }

    private String createClaim(DaplaUserInfo daplaUserInfo, ProtocolMapperModel model) {


        boolean excludeTeamsWithoutGroups = getConfigBoolean(model, DaplaUserInfoMapper.ConfigPropertyKey.EXCLUDE_TEAMS_WITHOUT_GROUPS);
        Stream<DaplaTeam> teamStream = daplaUserInfo.teams()
                .stream()
                .filter(team -> !(excludeTeamsWithoutGroups && team.groups().isEmpty()));


        Map<String, Object> claim = new HashMap<>();

        Map<String, String> userMap = Json.toGenericMap(daplaUserInfo.user());
        for (String userPropToInclude : getConfigStringList(model, ConfigPropertyKey.DAPLA_USER_PROPS)) {
            if (userMap.containsKey(userPropToInclude)) {
                claim.put(userPropToInclude, userMap.get(userPropToInclude));
            }
        }


        boolean nestTeams = getConfigBoolean(model, DaplaUserInfoMapper.ConfigPropertyKey.NESTED_TEAMS);
        if (nestTeams) {
            claim.put("teams", teamStream
                    .map(team -> {

                        // We do the mapping on itself, since raw pojo to json mapping will leave us with a DaplaGroup object in the json, but we want it flat with just name
                        List<String> groupsCategory = team.groups().stream().map(DaplaGroup::name).map(name -> name.replace(team.uniformName() + "-", "")).toList();

                        //Avoid duplicate groups when team pojo -> json
                        team.groups().clear();
                        Map<String, Object> pojoMap = Json.toGenericMap(team);

                        Map<String, Object> teamProps = new HashMap<>();
                        for (String teamPropToInclude : getConfigStringList(model, ConfigPropertyKey.DAPLA_TEAM_PROPS)) {
                            if (pojoMap.containsKey(teamPropToInclude)) {
                                teamProps.put(teamPropToInclude, pojoMap.get(teamPropToInclude));
                            }
                        }

                        teamProps.put("uniform_name", team.uniformName());
                        teamProps.put("groups", groupsCategory);
                        return teamProps;
                    })
                    .toList());
        } else {
            var filteredTeams = teamStream.toList();
            claim.put("teams", filteredTeams.stream().map(DaplaTeam::uniformName).toList());
            claim.put("groups", filteredTeams.stream()
                    .flatMap((team -> team.groups().stream().map(DaplaGroup::name)))
                    .toList());
        }

        return Json.from(claim);
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
     * Instantiate Dapla API service based on the configuration.
     *
     * @return the implementation to use based on the user option
     */
    ApiService teamApiService(ProtocolMapperModel model) {
        String apiImpl = getConfigString(model, ConfigPropertyKey.API_IMPL);
        debugLog(model, "Use " + apiImpl + " Dapla Team API implementation");
        if (DaplaTeamApi.NAME.equals(apiImpl)) {
            return new DaplaTeamApi(DaplaTeamApi.Config.builder()
                    .teamApiUrl(URI.create(getConfigString(model, ConfigPropertyKey.API_URL)))
                    .build());
        }
        if (DaplaApi.NAME.equals(apiImpl)) {
            return new DaplaApi(DaplaApi.Config.builder()
                    .apiUrl(getConfigString(model, ConfigPropertyKey.API_URL))
                    .build());
        } else if (DummyApi.NAME.equals(apiImpl)) {
            return new DummyApi();
        } else {
            throw new DaplaKeycloakException("Unsupported Team API implementation: " + apiImpl);
        }
    }

    Pattern groupIncludeFilter(ProtocolMapperModel model) {
        // TODO: Validate somehow that the regex is valid?
        // If the user does not provide a valid regex, filtering is likely to fail spectacularly.
        return Optional.ofNullable(getConfigString(model, ConfigPropertyKey.GROUP_SUFFIX_INCLUDE_REGEX))
                .map(suffixRegex -> Pattern.compile(".*-(" + suffixRegex + ")"))
                .orElse(null);
    }
}
