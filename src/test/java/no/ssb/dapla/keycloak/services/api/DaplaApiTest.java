package no.ssb.dapla.keycloak.services.api;

import com.sun.net.httpserver.HttpServer;
import no.ssb.dapla.keycloak.services.model.DaplaGroup;
import no.ssb.dapla.keycloak.services.model.DaplaTeam;
import no.ssb.dapla.keycloak.services.model.DaplaUserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class DaplaApiTest {

    private static final String TEST_TOKEN = "test-token";
    public static final String USER_EMAIL = "mis@ssb.no";

    private HttpServer server;
    private int port;
    private String lastRequestBody;
    private String lastAuthorization;
    private String lastContentType;
    private String lastMethod;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/graphql", exchange -> {
            lastMethod = exchange.getRequestMethod();
            lastContentType = exchange.getRequestHeaders().getFirst("Content-Type");
            lastAuthorization = exchange.getRequestHeaders().getFirst("Authorization");
            lastRequestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            byte[] response = graphQlResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        port = server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void requestIsAsExpected() {
        DaplaApi api = new DaplaApi(new DaplaApi.Config("http://localhost:" + port + "/graphql", TEST_TOKEN));

        api.getDaplaUserInfo(USER_EMAIL, null);

        assertThat(lastMethod).isEqualTo("POST");
        assertThat(lastContentType).isEqualTo("application/json; charset=utf-8");
        assertThat(lastAuthorization).isEqualTo("Bearer " + TEST_TOKEN);
        assertThat(lastRequestBody).isEqualTo(DaplaApi.queryBody.formatted(USER_EMAIL));
    }

    @Test
    void getDaplaUserInfo_parsesUserAndTeams() {
        DaplaApi api = new DaplaApi(new DaplaApi.Config("http://localhost:" + port + "/graphql", TEST_TOKEN));

        DaplaUserInfo info = api.getDaplaUserInfo(USER_EMAIL, null);

        assertThat(info.user().name()).isEqualTo("Mus, Mikke");
        assertThat(info.user().email()).isEqualTo(USER_EMAIL);
        assertThat(info.user().sectionCode()).isEqualTo("724");
        assertThat(info.user().sectionName()).isEqualTo("Seksjon for dataplattform");
        assertThat(info.user().isSectionManager()).isFalse();

        assertThat(info.teams()).hasSize(11);

        DaplaTeam daplaFelles = findTeam(info.teams(), "dapla-felles");
        assertThat(daplaFelles.displayName()).isEqualTo("Dapla Felles");
        assertThat(daplaFelles.sectionCode()).isEqualTo("724");
        assertThat(daplaFelles.sectionName()).isEqualTo("Seksjon for dataplattform");
        assertThat(daplaFelles.autonomyLevel()).isEqualTo("MANAGED");
        assertThat(groupNames(daplaFelles)).containsExactly(
                "dapla-felles-data-admins",
                "dapla-felles-developers"
        );

        DaplaTeam daplaLab = findTeam(info.teams(), "dapla-lab");
        assertThat(groupNames(daplaLab)).containsExactly("dapla-lab-developers");
    }

    @Test
    void getDaplaUserInfo_filtersGroupsByPattern() {
        DaplaApi api = new DaplaApi(new DaplaApi.Config("http://localhost:" + port + "/graphql", TEST_TOKEN));

        DaplaUserInfo info = api.getDaplaUserInfo(USER_EMAIL, Pattern.compile(".*-data-admins"));

        DaplaTeam daplaFelles = findTeam(info.teams(), "dapla-felles");
        assertThat(groupNames(daplaFelles)).containsExactly("dapla-felles-data-admins");

        DaplaTeam daplaLab = findTeam(info.teams(), "dapla-lab");
        assertThat(groupNames(daplaLab)).isEmpty();

        DaplaTeam playFoeniks = findTeam(info.teams(), "play-foeniks-a");
        assertThat(groupNames(playFoeniks)).containsExactly("play-foeniks-a-data-admins");
    }

    private static DaplaTeam findTeam(List<DaplaTeam> teams, String uniformName) {
        return teams.stream()
                .filter(team -> team.uniformName().equals(uniformName))
                .findFirst()
                .orElseThrow();
    }

    private static List<String> groupNames(DaplaTeam team) {
        return team.groups().stream().map(DaplaGroup::name).toList();
    }

    String graphQlResponse = """
            {
                "data": {
                    "user": {
                        "name": "Mus, Mikke",
                        "email": "mis@ssb.no",
                        "section": {
                            "name": "Seksjon for dataplattform",
                            "code": "724"
                        },
                        "isSectionManager": false,
                        "teams": {
                            "nodes": [
                                {
                                    "team": {
                                        "slug": "dapla-felles",
                                        "displayName": "Dapla Felles",
                                        "isManaged": true,
                                        "section": {
                                            "name": "Seksjon for dataplattform",
                                            "code": "724"
                                        }
                                    },
                                    "groups": [
                                        {
                                            "name": "dapla-felles-data-admins"
                                        },
                                        {
                                            "name": "dapla-felles-developers"
                                        }
                                    ]
                                },
                                {
                                    "team": {
                                        "slug": "dapla-lab",
                                        "displayName": "dapla-lab",
                                        "isManaged": true,
                                        "section": {
                                            "name": "Seksjon for dataplattform",
                                            "code": "724"
                                        }
                                    },
                                    "groups": [
                                        {
                                            "name": "dapla-lab-developers"
                                        }
                                    ]
                                },
                                {
                                    "team": {
                                        "slug": "dapla-onprem-vpn",
                                        "displayName": "Dapla onprem vpn",
                                        "isManaged": true,
                                        "section": {
                                            "name": "Seksjon for drift og infrastruktur",
                                            "code": "782"
                                        }
                                    },
                                    "groups": [
                                        {
                                            "name": "dapla-onprem-vpn-developers"
                                        }
                                    ]
                                },
                                {
                                    "team": {
                                        "slug": "dapla-platform",
                                        "displayName": "Dapla Platform",
                                        "isManaged": true,
                                        "section": {
                                            "name": "Seksjon for dataplattform",
                                            "code": "724"
                                        }
                                    },
                                    "groups": [
                                        {
                                            "name": "dapla-platform-developers"
                                        }
                                    ]
                                },
                                {
                                    "team": {
                                        "slug": "dapla-skyinfra",
                                        "displayName": "Dapla Skyinfra",
                                        "isManaged": true,
                                        "section": {
                                            "name": "Seksjon for dataplattform",
                                            "code": "724"
                                        }
                                    },
                                    "groups": [
                                        {
                                            "name": "dapla-skyinfra-developers"
                                        }
                                    ]
                                },
                                {
                                    "team": {
                                        "slug": "keycloak",
                                        "displayName": "keycloak",
                                        "isManaged": true,
                                        "section": {
                                            "name": "Seksjon for dataplattform",
                                            "code": "724"
                                        }
                                    },
                                    "groups": [
                                        {
                                            "name": "keycloak-developers"
                                        }
                                    ]
                                },
                                {
                                    "team": {
                                        "slug": "play-enhjoern-a",
                                        "displayName": "Play Enhjørn A",
                                        "isManaged": true,
                                        "section": {
                                            "name": "Seksjon for dataplattform",
                                            "code": "724"
                                        }
                                    },
                                    "groups": [
                                        {
                                            "name": "play-enhjoern-a-data-admins"
                                        },
                                        {
                                            "name": "play-enhjoern-a-developers"
                                        }
                                    ]
                                },
                                {
                                    "team": {
                                        "slug": "play-foeniks-a",
                                        "displayName": "Play Føniks A",
                                        "isManaged": true,
                                        "section": {
                                            "name": "Seksjon for dataplattform",
                                            "code": "724"
                                        }
                                    },
                                    "groups": [
                                        {
                                            "name": "play-foeniks-a-data-admins"
                                        }
                                    ]
                                },
                                {
                                    "team": {
                                        "slug": "play-obr",
                                        "displayName": "Play obr",
                                        "isManaged": true,
                                        "section": {
                                            "name": "Seksjon for dataplattform",
                                            "code": "724"
                                        }
                                    },
                                    "groups": [
                                        {
                                            "name": "play-obr-data-admins"
                                        },
                                        {
                                            "name": "play-obr-developers"
                                        }
                                    ]
                                },
                                {
                                    "team": {
                                        "slug": "play-obr-b",
                                        "displayName": "play-obr-b",
                                        "isManaged": true,
                                        "section": {
                                            "name": "Seksjon for dataplattform",
                                            "code": "724"
                                        }
                                    },
                                    "groups": [
                                        {
                                            "name": "play-obr-b-data-admins"
                                        },
                                        {
                                            "name": "play-obr-b-developers"
                                        }
                                    ]
                                },
                                {
                                    "team": {
                                        "slug": "play-skyinfra-a",
                                        "displayName": "Play Skyinfra A",
                                        "isManaged": true,
                                        "section": {
                                            "name": "Seksjon for dataplattform",
                                            "code": "724"
                                        }
                                    },
                                    "groups": [
                                        {
                                            "name": "play-skyinfra-a-developers"
                                        }
                                    ]
                                }
                            ]
                        }
                    }
                }
            }
            """;
}
