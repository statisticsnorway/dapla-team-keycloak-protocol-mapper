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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class DaplaTeamApiTest {

    public static final String USER_EMAIL = "mis@ssb.no";

    private HttpServer server;
    private String lastAuthorization;
    private String lastMethod;
    private DaplaTeamApi.Config config;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/token", exchange -> {
            byte[] response = "{\"access_token\": \"my_test_access_token\"}".getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        server.createContext("/users", exchange -> {
            lastMethod = exchange.getRequestMethod();
            lastAuthorization = exchange.getRequestHeaders().getFirst("Authorization");

            byte[] response = apiResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        int port = server.getAddress().getPort();

        config = new DaplaTeamApi.Config(URI.create("http://localhost:" + port), "http://localhost:" + port + "/token", "clientId", "secret");

    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void requestIsAsExpected() {
        DaplaTeamApi api = new DaplaTeamApi(config);

        api.getDaplaUserInfo(USER_EMAIL, null);

        assertThat(lastMethod).isEqualTo("GET");
        assertThat(lastAuthorization).isEqualTo("Bearer " + "my_test_access_token");
    }

    @Test
    void getDaplaUserInfo_parsesUserAndTeams() {
        DaplaTeamApi api = new DaplaTeamApi(config);

        DaplaUserInfo info = api.getDaplaUserInfo(USER_EMAIL, null);

        assertThat(info.user().name()).isEqualTo("Mus, Mikke");
        assertThat(info.user().email()).isEqualTo(USER_EMAIL);
        assertThat(info.user().sectionCode()).isEqualTo("724");
        assertThat(info.user().sectionName()).isEqualTo("O 724 Seksjon for dataplattform");
        assertThat(info.user().isSectionManager()).isNull();

        assertThat(info.teams()).hasSize(11);

        DaplaTeam daplaFelles = findTeam(info.teams(), "dapla-felles");
        assertThat(daplaFelles.displayName()).isEqualTo("Dapla Felles");
        assertThat(daplaFelles.sectionCode()).isEqualTo("724");
        assertThat(daplaFelles.sectionName()).isEqualTo("Dataplattform (724)");
        assertThat(daplaFelles.autonomyLevel()).isEqualTo("MANAGED");
        assertThat(groupNames(daplaFelles)).containsExactly(
                "dapla-felles-developers",
                "dapla-felles-data-admins"
        );

        DaplaTeam daplaLab = findTeam(info.teams(), "dapla-lab");
        assertThat(groupNames(daplaLab)).containsExactly("dapla-lab-developers");
    }

    @Test
    void getDaplaUserInfo_filtersGroupsByPattern() {
        DaplaTeamApi api = new DaplaTeamApi(config);

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

    String apiResponse = """
            {
               "principal_name": "mis@ssb.no",
               "azure_ad_id": "abcdef01-1234-1234-1234-abcdef012345",
               "display_name": "Mus, Mikke",
               "first_name": "Mikke",
               "last_name": "mus",
               "email": "mikke.mus@ssb.no",
               "phone": "12345678",
               "job_title": "Rådgiver",
               "division_name": "Avdeling for IT 700",
               "division_code": "700",
               "section_name": "O 724 Seksjon for dataplattform",
               "section_code": "724",
               "_links": {
                 "self": {
                   "href": "http://localhost/users/mis%40ssb.no"
                 }
               },
               "_embedded": {
                 "teams": [
                   {
                     "uniform_name": "play-obr-b",
                     "display_name": "play-obr-b",
                     "division_name": "IT (Avdeling 700)",
                     "section_name": "Dataplattform (724)",
                     "section_code": "724",
                     "autonomy_level": "SELF_MANAGED",
                     "source_data_classification": [],
                     "statistical_products": [],
                     "dpia_links": [],
                     "_links": {
                       "iac_repo": {
                         "href": "http://github.com/statisticsnorway/play-obr-b-iac"
                       },
                       "self": {
                         "href": "http://localhost/teams/play-obr-b"
                       },
                       "groups": {
                         "href": "http://localhost/teams/play-obr-b/groups"
                       },
                       "users": {
                         "href": "http://localhost/teams/play-obr-b/users"
                       },
                       "section_manager": {
                         "href": "http://localhost/users/stf%40ssb.no"
                       }
                     }
                   },
                   {
                     "uniform_name": "dapla-felles",
                     "display_name": "Dapla Felles",
                     "division_name": "IT (Avdeling 700)",
                     "section_name": "Dataplattform (724)",
                     "section_code": "724",
                     "autonomy_level": "MANAGED",
                     "source_data_classification": [],
                     "statistical_products": [],
                     "dpia_links": [],
                     "_links": {
                       "iac_repo": {
                         "href": "http://github.com/statisticsnorway/dapla-felles-iac"
                       },
                       "self": {
                         "href": "http://localhost/teams/dapla-felles"
                       },
                       "groups": {
                         "href": "http://localhost/teams/dapla-felles/groups"
                       },
                       "users": {
                         "href": "http://localhost/teams/dapla-felles/users"
                       },
                       "section_manager": {
                         "href": "http://localhost/users/stf%40ssb.no"
                       }
                     }
                   },
                   {
                     "uniform_name": "play-obr",
                     "display_name": "Play obr",
                     "division_name": "IT (Avdeling 700)",
                     "section_name": "Dataplattform (724)",
                     "section_code": "724",
                     "autonomy_level": "SELF_MANAGED",
                     "source_data_classification": [],
                     "statistical_products": [],
                     "dpia_links": [],
                     "_links": {
                       "iac_repo": {
                         "href": "http://github.com/statisticsnorway/play-obr-iac"
                       },
                       "self": {
                         "href": "http://localhost/teams/play-obr"
                       },
                       "groups": {
                         "href": "http://localhost/teams/play-obr/groups"
                       },
                       "users": {
                         "href": "http://localhost/teams/play-obr/users"
                       },
                       "section_manager": {
                         "href": "http://localhost/users/stf%40ssb.no"
                       }
                     }
                   },
                   {
                     "uniform_name": "dapla-lab",
                     "display_name": "dapla-lab",
                     "division_name": "IT (Avdeling 700)",
                     "section_name": "Dataplattform (724)",
                     "section_code": "724",
                     "autonomy_level": "SELF_MANAGED",
                     "source_data_classification": [],
                     "statistical_products": [],
                     "dpia_links": [],
                     "_links": {
                       "iac_repo": {
                         "href": "http://github.com/statisticsnorway/dapla-lab-iac"
                       },
                       "self": {
                         "href": "http://localhost/teams/dapla-lab"
                       },
                       "groups": {
                         "href": "http://localhost/teams/dapla-lab/groups"
                       },
                       "users": {
                         "href": "http://localhost/teams/dapla-lab/users"
                       },
                       "section_manager": {
                         "href": "http://localhost/users/stf%40ssb.no"
                       }
                     }
                   },
                   {
                     "uniform_name": "play-foeniks-a",
                     "display_name": "Play Føniks A",
                     "division_name": "IT (Avdeling 700)",
                     "section_name": "Dataplattform (724)",
                     "section_code": "724",
                     "autonomy_level": "SELF_MANAGED",
                     "source_data_classification": [],
                     "statistical_products": [],
                     "dpia_links": [],
                     "_links": {
                       "iac_repo": {
                         "href": "http://github.com/statisticsnorway/play-foeniks-a-iac"
                       },
                       "self": {
                         "href": "http://localhost/teams/play-foeniks-a"
                       },
                       "groups": {
                         "href": "http://localhost/teams/play-foeniks-a/groups"
                       },
                       "users": {
                         "href": "http://localhost/teams/play-foeniks-a/users"
                       },
                       "section_manager": {
                         "href": "http://localhost/users/stf%40ssb.no"
                       }
                     }
                   },
                   {
                     "uniform_name": "dapla-skyinfra",
                     "display_name": "Dapla Skyinfra",
                     "division_name": "IT (Avdeling 700)",
                     "section_name": "Dataplattform (724)",
                     "section_code": "724",
                     "autonomy_level": "SELF_MANAGED",
                     "source_data_classification": [],
                     "statistical_products": [],
                     "dpia_links": [],
                     "_links": {
                       "iac_repo": {
                         "href": "http://github.com/statisticsnorway/dapla-skyinfra-iac"
                       },
                       "self": {
                         "href": "http://localhost/teams/dapla-skyinfra"
                       },
                       "groups": {
                         "href": "http://localhost/teams/dapla-skyinfra/groups"
                       },
                       "users": {
                         "href": "http://localhost/teams/dapla-skyinfra/users"
                       },
                       "section_manager": {
                         "href": "http://localhost/users/stf%40ssb.no"
                       }
                     }
                   },
                   {
                     "uniform_name": "dapla-platform",
                     "display_name": "Dapla Platform",
                     "division_name": "IT (Avdeling 700)",
                     "section_name": "Dataplattform (724)",
                     "section_code": "724",
                     "autonomy_level": "SELF_MANAGED",
                     "source_data_classification": [],
                     "statistical_products": [],
                     "dpia_links": [],
                     "_links": {
                       "iac_repo": {
                         "href": "http://github.com/statisticsnorway/dapla-platform-iac"
                       },
                       "self": {
                         "href": "http://localhost/teams/dapla-platform"
                       },
                       "groups": {
                         "href": "http://localhost/teams/dapla-platform/groups"
                       },
                       "users": {
                         "href": "http://localhost/teams/dapla-platform/users"
                       },
                       "section_manager": {
                         "href": "http://localhost/users/stf%40ssb.no"
                       }
                     }
                   },
                   {
                     "uniform_name": "play-enhjoern-a",
                     "display_name": "Play Enhjørn A",
                     "division_name": "IT (Avdeling 700)",
                     "section_name": "Dataplattform (724)",
                     "section_code": "724",
                     "autonomy_level": "SELF_MANAGED",
                     "source_data_classification": [],
                     "statistical_products": [],
                     "dpia_links": [],
                     "_links": {
                       "iac_repo": {
                         "href": "http://github.com/statisticsnorway/play-enhjoern-a-iac"
                       },
                       "self": {
                         "href": "http://localhost/teams/play-enhjoern-a"
                       },
                       "groups": {
                         "href": "http://localhost/teams/play-enhjoern-a/groups"
                       },
                       "users": {
                         "href": "http://localhost/teams/play-enhjoern-a/users"
                       },
                       "section_manager": {
                         "href": "http://localhost/users/stf%40ssb.no"
                       }
                     }
                   },
                   {
                     "uniform_name": "keycloak",
                     "display_name": "keycloak",
                     "division_name": "IT (Avdeling 700)",
                     "section_name": "Dataplattform (724)",
                     "section_code": "724",
                     "autonomy_level": "SELF_MANAGED",
                     "source_data_classification": [],
                     "statistical_products": [],
                     "dpia_links": [],
                     "_links": {
                       "iac_repo": {
                         "href": "http://github.com/statisticsnorway/keycloak-iac"
                       },
                       "self": {
                         "href": "http://localhost/teams/keycloak"
                       },
                       "groups": {
                         "href": "http://localhost/teams/keycloak/groups"
                       },
                       "users": {
                         "href": "http://localhost/teams/keycloak/users"
                       },
                       "section_manager": {
                         "href": "http://localhost/users/stf%40ssb.no"
                       }
                     }
                   },
                   {
                     "uniform_name": "play-skyinfra-a",
                     "display_name": "Play Skyinfra A",
                     "division_name": "IT (Avdeling 700)",
                     "section_name": "Dataplattform (724)",
                     "section_code": "724",
                     "autonomy_level": "SELF_MANAGED",
                     "source_data_classification": [],
                     "statistical_products": [],
                     "dpia_links": [],
                     "_links": {
                       "iac_repo": {
                         "href": "http://github.com/statisticsnorway/play-skyinfra-a-iac"
                       },
                       "self": {
                         "href": "http://localhost/teams/play-skyinfra-a"
                       },
                       "groups": {
                         "href": "http://localhost/teams/play-skyinfra-a/groups"
                       },
                       "users": {
                         "href": "http://localhost/teams/play-skyinfra-a/users"
                       },
                       "section_manager": {
                         "href": "http://localhost/users/stf%40ssb.no"
                       }
                     }
                   },
                   {
                     "uniform_name": "dapla-onprem-vpn",
                     "display_name": "Dapla onprem vpn",
                     "division_name": "IT (Avdeling 700)",
                     "section_name": "Drift og infrastruktur (782)",
                     "section_code": "782",
                     "autonomy_level": "SELF_MANAGED",
                     "source_data_classification": [],
                     "statistical_products": [],
                     "dpia_links": [],
                     "_links": {
                       "iac_repo": {
                         "href": "http://github.com/statisticsnorway/dapla-onprem-vpn-iac"
                       },
                       "self": {
                         "href": "http://localhost/teams/dapla-onprem-vpn"
                       },
                       "groups": {
                         "href": "http://localhost/teams/dapla-onprem-vpn/groups"
                       },
                       "users": {
                         "href": "http://localhost/teams/dapla-onprem-vpn/users"
                       },
                       "section_manager": {
                         "href": "http://localhost/users/ibe%40ssb.no"
                       }
                     }
                   }
                 ],
                 "groups": [
                   {
                     "uniform_name": "dapla-skyinfra-developers",
                     "_links": {
                       "self": {
                         "href": "http://localhost/groups/dapla-skyinfra-developers"
                       }
                     }
                   },
                   {
                     "uniform_name": "dapla-lab-developers",
                     "_links": {
                       "self": {
                         "href": "http://localhost/groups/dapla-lab-developers"
                       }
                     }
                   },
                   {
                     "uniform_name": "play-obr-data-admins",
                     "_links": {
                       "self": {
                         "href": "http://localhost/groups/play-obr-data-admins"
                       }
                     }
                   },
                   {
                     "uniform_name": "keycloak-developers",
                     "_links": {
                       "self": {
                         "href": "http://localhost/groups/keycloak-developers"
                       }
                     }
                   },
                   {
                     "uniform_name": "play-obr-b-data-admins",
                     "_links": {
                       "self": {
                         "href": "http://localhost/groups/play-obr-b-data-admins"
                       }
                     }
                   },
                   {
                     "uniform_name": "play-skyinfra-a-developers",
                     "_links": {
                       "self": {
                         "href": "http://localhost/groups/play-skyinfra-a-developers"
                       }
                     }
                   },
                   {
                     "uniform_name": "dapla-felles-data-admins",
                     "_links": {
                       "self": {
                         "href": "http://localhost/groups/dapla-felles-data-admins"
                       }
                     }
                   },
                   {
                     "uniform_name": "dapla-felles-developers",
                     "_links": {
                       "self": {
                         "href": "http://localhost/groups/dapla-felles-developers"
                       }
                     }
                   },
                   {
                     "uniform_name": "play-foeniks-a-data-admins",
                     "_links": {
                       "self": {
                         "href": "http://localhost/groups/play-foeniks-a-data-admins"
                       }
                     }
                   },
                   {
                     "uniform_name": "play-enhjoern-a-developers",
                     "_links": {
                       "self": {
                         "href": "http://localhost/groups/play-enhjoern-a-developers"
                       }
                     }
                   },
                   {
                     "uniform_name": "play-enhjoern-a-data-admins",
                     "_links": {
                       "self": {
                         "href": "http://localhost/groups/play-enhjoern-a-data-admins"
                       }
                     }
                   },
                   {
                     "uniform_name": "dapla-platform-developers",
                     "_links": {
                       "self": {
                         "href": "http://localhost/groups/dapla-platform-developers"
                       }
                     }
                   },
                   {
                     "uniform_name": "play-obr-developers",
                     "_links": {
                       "self": {
                         "href": "http://localhost/groups/play-obr-developers"
                       }
                     }
                   },
                   {
                     "uniform_name": "play-obr-b-developers",
                     "_links": {
                       "self": {
                         "href": "http://localhost/groups/play-obr-b-developers"
                       }
                     }
                   },
                   {
                     "uniform_name": "dapla-onprem-vpn-developers",
                     "_links": {
                       "self": {
                         "href": "http://localhost/groups/dapla-onprem-vpn-developers"
                       }
                     }
                   }
                 ]
               }
             }
            """;
}
