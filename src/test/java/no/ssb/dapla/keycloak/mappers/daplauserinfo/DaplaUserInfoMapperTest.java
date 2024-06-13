package no.ssb.dapla.keycloak.mappers.daplauserinfo;

import no.ssb.dapla.keycloak.mappers.ConfigPropertyKey;
import no.ssb.dapla.keycloak.services.teamapi.DummyDaplaTeamApiService;
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
    void testMapToClaimUsingDummyDaplaTeamApiService() throws Exception {
        protocolMapperModel.setConfig(Map.of(
                ConfigPropertyKey.VERBOSE_LOGGING, Boolean.TRUE.toString(),
                DaplaUserInfoMapper.ConfigPropertyKey.API_IMPL, DummyDaplaTeamApiService.NAME,
                DaplaUserInfoMapper.ConfigPropertyKey.NESTED_TEAMS, Boolean.TRUE.toString()
        ));

        Object claim = mapper.mapToClaim(idToken, protocolMapperModel, userSessionModel, keycloakSession, clientSessionContext);
        assertThat(claim).isNotNull();
        assertThat(claim).isInstanceOf(String.class);
        String jsonClaim = (String) claim;
        JSONAssert.assertEquals("""
                {
                  "teams" : [ {
                    "uniform_name" : "dapla-felles",
                    "groups" : [ ]
                  }, {
                    "uniform_name" : "mus-ost",
                    "groups" : [ "developers" ]
                  }, {
                    "uniform_name" : "play-foeniks-a",
                    "groups" : [ "developers", "data-admins", "consumers", "editors" ]
                  } ]
                }
                """, jsonClaim, JSONCompareMode.LENIENT);
    }

}
