package no.ssb.dapla.keycloak.mappers.daplauserinfo;

import no.ssb.dapla.keycloak.mappers.ConfigPropertyKey;
import no.ssb.dapla.keycloak.services.api.DaplaApi;
import no.ssb.dapla.keycloak.services.api.ApiService;
import no.ssb.dapla.keycloak.services.model.DaplaUserInfo;
import no.ssb.dapla.keycloak.services.api.DaplaTeamApi;
import no.ssb.dapla.keycloak.utils.Json;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.keycloak.models.*;
import org.keycloak.representations.IDToken;
import org.mockito.Mockito;

import java.net.URI;
import java.util.Map;

import static no.ssb.dapla.keycloak.Env.Var.*;
import static no.ssb.dapla.keycloak.Env.requiredEnv;

@Tag("integration")
@Disabled
public class DaplaUseInfoMapperIT {
    private static final Logger log = Logger.getLogger(DaplaUserInfoMapperTest.class);
    private ApiService service;

    private ProtocolMapperModel protocolMapperModel;
    private UserSessionModel userSessionModel;
    private UserModel userModel;
    private IDToken idToken;
    private KeycloakSession keycloakSession;
    private ClientSessionContext clientSessionContext;
    private DaplaUserInfoMapper mapper;

    @BeforeEach
    void setUp() {
        service = new DaplaTeamApi(DaplaTeamApi.Config.builder()
                .teamApiUrl(URI.create("https://dapla-team-api.intern.test.ssb.no"))
                .build());

        protocolMapperModel = new ProtocolMapperModel();
        userSessionModel = Mockito.mock(UserSessionModel.class);
        userModel = Mockito.mock(UserModel.class);
        Mockito.when(userSessionModel.getUser()).thenReturn(userModel);
        idToken = new IDToken();
        keycloakSession = Mockito.mock(KeycloakSession.class);
        clientSessionContext = Mockito.mock(ClientSessionContext.class);
        mapper = new DaplaUserInfoMapper();
    }


    @BeforeEach
    public void setup() {
    }

    @Test
    public void testRequiredEnvVarsSet() {
        requiredEnv(DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_AUTH_URL);
        requiredEnv(DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_ID);
        requiredEnv(DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_SECRET);
        requiredEnv(TEST_USER_PRINCIPAL_NAME);
    }

    @Test
    public void testGetDaplaUserInfo() {
        String userPrincipalName = requiredEnv(TEST_USER_PRINCIPAL_NAME);
        DaplaUserInfo daplaInfoJson = service.getDaplaUserInfo(userPrincipalName, null);
        log.info(Json.prettyFrom(daplaInfoJson));
    }

    @Test
    void testMapToClaimUsingDefaultDaplaTeamApiService() {
        protocolMapperModel.setConfig(Map.of(
                ConfigPropertyKey.VERBOSE_LOGGING, Boolean.TRUE.toString(),
                DaplaUserInfoMapper.ConfigPropertyKey.API_URL, "http://dapla-team-api.dapla-platform",
                DaplaUserInfoMapper.ConfigPropertyKey.API_IMPL, DaplaTeamApi.NAME,
                DaplaUserInfoMapper.ConfigPropertyKey.NESTED_TEAMS, Boolean.TRUE.toString(),
                DaplaUserInfoMapper.ConfigPropertyKey.EXCLUDE_TEAMS_WITHOUT_GROUPS, Boolean.FALSE.toString(),
                DaplaUserInfoMapper.ConfigPropertyKey.DAPLA_USER_PROPS, "division_code",
                DaplaUserInfoMapper.ConfigPropertyKey.DAPLA_TEAM_PROPS, "section_code, autonomy_level"
        ));

        String claimJson = (String) mapper.mapToClaim(idToken, protocolMapperModel, userSessionModel, keycloakSession, clientSessionContext);
        log.info(Json.prettyFrom(claimJson));
    }
    @Test
    void testMapToClaimUsingDaplaApiService() {
        protocolMapperModel.setConfig(Map.of(
                ConfigPropertyKey.VERBOSE_LOGGING, Boolean.TRUE.toString(),
                DaplaUserInfoMapper.ConfigPropertyKey.API_URL, "http://dapla-api.dapla-platform",
                DaplaUserInfoMapper.ConfigPropertyKey.API_IMPL, DaplaApi.NAME,
                DaplaUserInfoMapper.ConfigPropertyKey.NESTED_TEAMS, Boolean.TRUE.toString(),
                DaplaUserInfoMapper.ConfigPropertyKey.EXCLUDE_TEAMS_WITHOUT_GROUPS, Boolean.FALSE.toString(),
                DaplaUserInfoMapper.ConfigPropertyKey.DAPLA_USER_PROPS, "division_code",
                DaplaUserInfoMapper.ConfigPropertyKey.DAPLA_TEAM_PROPS, "section_code, autonomy_level"
        ));

        String claimJson = (String) mapper.mapToClaim(idToken, protocolMapperModel, userSessionModel, keycloakSession, clientSessionContext);
        log.info(Json.prettyFrom(claimJson));
    }

}
