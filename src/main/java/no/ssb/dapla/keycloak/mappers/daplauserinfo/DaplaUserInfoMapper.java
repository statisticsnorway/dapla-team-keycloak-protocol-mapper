package no.ssb.dapla.keycloak.mappers.daplauserinfo;

import com.google.auto.service.AutoService;
import no.ssb.dapla.keycloak.DaplaKeycloakException;
import no.ssb.dapla.keycloak.mappers.AbstractTokenMapper;
import no.ssb.dapla.keycloak.mappers.ConfigPropertyType;
import no.ssb.dapla.keycloak.services.teamapi.DaplaTeamApiService;
import no.ssb.dapla.keycloak.services.teamapi.DefaultDaplaTeamApiService;
import no.ssb.dapla.keycloak.services.teamapi.DefaultDaplaTeamApiService.Config;
import no.ssb.dapla.keycloak.services.teamapi.DummyDaplaTeamApiService;
import no.ssb.dapla.keycloak.utils.Env;
import no.ssb.dapla.keycloak.utils.Json;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.representations.IDToken;

import java.net.URI;

@AutoService(ProtocolMapper.class)
public class DaplaUserInfoMapper extends AbstractTokenMapper {
    public static final String PROVIDER_ID = "oidc-dapla-userinfo-mapper";

    public static class ConfigPropertyKey {
        public static final String API_URL = "dapla.team-api-url";
        public static final String API_IMPL = "dapla.team-api-impl";
        public static final String NESTED_TEAMS = "dapla.userinfo.nested";
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
                                Specify the root URL for the Dapla Team API.
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
                        .name(ConfigPropertyKey.DAPLA_USER_PROPS)
                        .label("Dapla user properties to include")
                        .helpText("""
                                List of Dapla user properties to include on root level of the claim""")
                        .type(ConfigPropertyType.MULTIVALUED_LIST)
                        .options("azure_ad_id", "division_code", "division_name", "section_code", "section_name")
                        .defaultValue("section_code")
                        .build(),

                configProperty()
                        .name(ConfigPropertyKey.DAPLA_TEAM_PROPS)
                        .label("Dapla team properties to include")
                        .helpText("""
                                List of Dapla team properties to include in the claim's team objects""")
                        .type(ConfigPropertyType.MULTIVALUED_LIST)
                        .options("autonomy_level", "display_name", "section_code", "source_data_classification", "statistical_products")
                        .defaultValue("section_code")
                        .build()
                );

        // Allow the mapper to configure the target value to be "JSON" (required)
        OIDCAttributeMapperHelper.addJsonTypeConfig(configProperties);
    }

    @Override
    protected String helpText() {
        return "Adds a 'teams' claim, retrieved from Dapla Team API";
    }

    @Override
    protected Object mapToClaim(IDToken token, ProtocolMapperModel model, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
        debugLog(model,"Retrieve Dapla teams");
        DaplaTeamApiService teamApiService = teamApiService(model);
        return Json.from(teamApiService.getDaplaUserInfo(userSession.getUser().getEmail()));
    }

    DaplaTeamApiService teamApiService(ProtocolMapperModel model) {
        String apiImpl = getConfigString(model, ConfigPropertyKey.API_IMPL);
        debugLog(model, "Use " + apiImpl + " Dapla Team API implementation");

        if (DefaultDaplaTeamApiService.NAME.equals(apiImpl)) {
            Config config = Config.builder()
                    .teamApiUrl(URI.create(getConfigString(model, ConfigPropertyKey.API_URL)))
                    .keycloakUrl(URI.create(Env.requiredVar("KEYCLOAK_URL")))
                    .daplaUserProps(getConfigStringSet(model, ConfigPropertyKey.DAPLA_USER_PROPS))
                    .daplaTeamProps(getConfigStringSet(model, ConfigPropertyKey.DAPLA_TEAM_PROPS))
                    .nestedTeams(getConfigBoolean(model, ConfigPropertyKey.NESTED_TEAMS))
                    .build();

            debugLog(model, "Dapla Team API Service params: " + config);
            return new DefaultDaplaTeamApiService(config);
        }
        else if (DummyDaplaTeamApiService.NAME.equals(apiImpl)) {
            return new DummyDaplaTeamApiService();
        }
        else {
            throw new DaplaKeycloakException("Unsupported Team API implementation: " + apiImpl);
        }
    }

}
