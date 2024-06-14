package no.ssb.dapla.keycloak.mappers.daplauserinfo;

import no.ssb.dapla.keycloak.mappers.ConfigPropertyKey;
import no.ssb.dapla.keycloak.services.teamapi.DummyDaplaTeamApiService;
import no.ssb.dapla.keycloak.utils.Json;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.models.*;
import org.keycloak.representations.IDToken;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DaplaUserInfoMapperTest {

    private static final Logger log = Logger.getLogger(DaplaUserInfoMapperTest.class);

    private ProtocolMapperModel protocolMapperModel;
    private UserSessionModel userSessionModel;
    private UserModel userModel;
    private IDToken idToken;
    private KeycloakSession keycloakSession;
    private ClientSessionContext clientSessionContext;
    private DaplaUserInfoMapper mapper;

    @BeforeEach
    void setUp() {
        protocolMapperModel = new ProtocolMapperModel();
        userSessionModel = Mockito.mock(UserSessionModel.class);
        userModel = Mockito.mock(UserModel.class);
        Mockito.when(userSessionModel.getUser()).thenReturn(userModel);
        Mockito.when(userModel.getEmail()).thenReturn("mik@55b.no");
        idToken = new IDToken();
        keycloakSession = Mockito.mock(KeycloakSession.class);
        clientSessionContext = Mockito.mock(ClientSessionContext.class);
        mapper = new DaplaUserInfoMapper();
    }

    @Test
    void mapToClaim_nested() throws Exception {
        protocolMapperModel.setConfig(Map.of(
                ConfigPropertyKey.VERBOSE_LOGGING, Boolean.TRUE.toString(),
                DaplaUserInfoMapper.ConfigPropertyKey.API_IMPL, DummyDaplaTeamApiService.NAME,
                DaplaUserInfoMapper.ConfigPropertyKey.NESTED_TEAMS, Boolean.TRUE.toString(),
                DaplaUserInfoMapper.ConfigPropertyKey.DAPLA_USER_PROPS, "section_code",
                DaplaUserInfoMapper.ConfigPropertyKey.DAPLA_TEAM_PROPS, "autonomy_level, source_data_classification, section_code",
                DaplaUserInfoMapper.ConfigPropertyKey.EXCLUDE_TEAMS_WITHOUT_GROUPS, Boolean.FALSE.toString()
        ));

        Object claim = mapper.mapToClaim(idToken, protocolMapperModel, userSessionModel, keycloakSession, clientSessionContext);
        assertThat(claim).isInstanceOf(String.class);
        String jsonClaim = (String) claim;
        log.info(Json.prettyFrom(jsonClaim));
        JSONAssert.assertEquals("""
               {
                 "teams" : [ {
                   "uniform_name" : "dapla-felles",
                   "section_code" : "724",
                   "autonomy_level" : "SELF_MANAGED",
                   "source_data_classification" : [ ],
                   "groups" : [ ]
                 }, {
                   "uniform_name" : "mu",
                   "section_code" : "399",
                   "autonomy_level" : "MANAGED",
                   "groups" : [ "developers" ]
                 }, {
                   "uniform_name" : "mus",
                   "section_code" : "399",
                   "autonomy_level" : "SEMI_MANAGED",
                   "groups" : [ "data-admins", "developers" ]
                 }, {
                   "uniform_name" : "mus-ost",
                   "section_code" : "399",
                   "autonomy_level" : "SELF_MANAGED",
                   "source_data_classification" : [ "PII", "CONSENT_BASED" ],
                   "groups" : [ "developers", "tech-admins" ]
                 }, {
                   "uniform_name" : "play-foeniks-a",
                   "section_code" : "724",
                   "autonomy_level" : "SELF_MANAGED",
                   "source_data_classification" : [ ],
                   "groups" : [ "consumers", "data-admins", "editors", "developers" ]
                 } ],
                 "section_code" : "399"
               }
               """, jsonClaim, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void mapToClaim_nested_withGroupRegex_excludingTeamsWithoutGroups() throws Exception {
        protocolMapperModel.setConfig(Map.of(
                ConfigPropertyKey.VERBOSE_LOGGING, Boolean.TRUE.toString(),
                DaplaUserInfoMapper.ConfigPropertyKey.API_IMPL, DummyDaplaTeamApiService.NAME,
                DaplaUserInfoMapper.ConfigPropertyKey.NESTED_TEAMS, Boolean.TRUE.toString(),
                DaplaUserInfoMapper.ConfigPropertyKey.GROUP_SUFFIX_INCLUDE_REGEX, "data-admins",
                DaplaUserInfoMapper.ConfigPropertyKey.EXCLUDE_TEAMS_WITHOUT_GROUPS, Boolean.TRUE.toString()
        ));

        String jsonClaim = (String) mapper.mapToClaim(idToken, protocolMapperModel, userSessionModel, keycloakSession, clientSessionContext);
        log.info(Json.prettyFrom(jsonClaim));
        JSONAssert.assertEquals("""
               {
                 "teams" : [ {
                   "uniform_name" : "mus",
                   "groups" : [ "data-admins" ]
                 }, {
                   "uniform_name" : "play-foeniks-a",
                   "groups" : [ "data-admins" ]
                 } ]
               }
               """, jsonClaim, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void mapToClaim_flat() throws Exception {
        protocolMapperModel.setConfig(Map.of(
                ConfigPropertyKey.VERBOSE_LOGGING, Boolean.TRUE.toString(),
                DaplaUserInfoMapper.ConfigPropertyKey.API_IMPL, DummyDaplaTeamApiService.NAME,
                DaplaUserInfoMapper.ConfigPropertyKey.NESTED_TEAMS, Boolean.FALSE.toString(),
                DaplaUserInfoMapper.ConfigPropertyKey.EXCLUDE_TEAMS_WITHOUT_GROUPS, Boolean.FALSE.toString()
        ));

        Object claim = mapper.mapToClaim(idToken, protocolMapperModel, userSessionModel, keycloakSession, clientSessionContext);
        assertThat(claim).isInstanceOf(String.class);
        String jsonClaim = (String) claim;
        System.out.println(Json.prettyFrom(jsonClaim));
        JSONAssert.assertEquals("""
                {
                   "teams": [
                     "dapla-felles",
                     "mu",
                     "mus",
                     "mus-ost",
                     "play-foeniks-a"
                   ],
                   "groups": [
                     "mu-developers",
                     "mus-developers",
                     "mus-data-admins",
                     "mus-ost-developers",
                     "mus-ost-tech-admins",
                     "play-foeniks-a-developers",
                     "play-foeniks-a-data-admins",
                     "play-foeniks-a-consumers",
                     "play-foeniks-a-editors"
                   ]
                 }
                """, jsonClaim, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void mapToClaim_flat_withGroupRegex_excludingTeamsWithoutGroups() throws Exception {
        protocolMapperModel.setConfig(Map.of(
                ConfigPropertyKey.VERBOSE_LOGGING, Boolean.TRUE.toString(),
                DaplaUserInfoMapper.ConfigPropertyKey.API_IMPL, DummyDaplaTeamApiService.NAME,
                DaplaUserInfoMapper.ConfigPropertyKey.NESTED_TEAMS, Boolean.FALSE.toString(),
                DaplaUserInfoMapper.ConfigPropertyKey.GROUP_SUFFIX_INCLUDE_REGEX, "data-admins",
                DaplaUserInfoMapper.ConfigPropertyKey.EXCLUDE_TEAMS_WITHOUT_GROUPS, Boolean.TRUE.toString()
        ));

        String jsonClaim = (String) mapper.mapToClaim(idToken, protocolMapperModel, userSessionModel, keycloakSession, clientSessionContext);
        log.info(Json.prettyFrom(jsonClaim));
        JSONAssert.assertEquals("""
                {
                  "teams": [
                    "mus",
                    "play-foeniks-a"
                  ],
                  "groups": [
                    "play-foeniks-a-data-admins",
                    "mus-data-admins"
                  ]
                }
               """, jsonClaim, JSONCompareMode.NON_EXTENSIBLE);
    }

}