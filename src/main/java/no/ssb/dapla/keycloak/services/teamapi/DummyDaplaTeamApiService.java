package no.ssb.dapla.keycloak.services.teamapi;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import no.ssb.dapla.keycloak.utils.Json;
import org.jboss.logging.Logger;

@RequiredArgsConstructor
public class DummyDaplaTeamApiService implements DaplaTeamApiService {
    private static final Logger log = Logger.getLogger(DummyDaplaTeamApiService.class);
    public static final String NAME = "Dummy";

    @Override
    public JsonNode getDaplaUserInfo(String userPrincipalName) {
         return Json.toJsonNode("""
                {
                  "principal_name": "%s",
                  "azure_ad_id": "12345678-9abc-def0-1234-56789abcdef0",
                  "display_name": "Mus, Mikke",
                  "first_name": "Mikke",
                  "last_name": "Mus",
                  "email": "Mikke.Mus@ssb.no",
                  "phone": "99999999",
                  "job_title": "Testbruker",
                  "division_name": "300 Musestatistikk",
                  "division_code": "300",
                  "section_name": "399 Nærings- og miljøstatistikk",
                  "section_code": "399",
                  "_embedded": {
                    "teams": [
                      {
                        "uniform_name": "dapla-felles",
                        "display_name": "Dapla Felles",
                        "division_name": "IT (Avdeling 700)",
                        "section_name": "Dataplattform (724)",
                        "section_code": "724",
                        "autonomy_level": "SELF_MANAGED",
                        "source_data_classification": [],
                        "statistical_products": [],
                        "dpia_links": []
                      },
                      {
                        "uniform_name": "mus-ost",
                        "display_name": "Team Ost",
                        "division_name": "Musestatistikk (Avdeling 300)",
                        "section_name": "Nærings- og miljøstatistikk (399)",
                        "section_code": "399",
                        "autonomy_level": "SELF_MANAGED",
                        "source_data_classification": ["PII","CONSENT_BASED"],
                        "statistical_products": ["eiendom_kostra"],
                        "dpia_links": ["https://ssb.no/pii-agreement-1"]
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
                        "dpia_links": []
                      }
                    ],
                    "groups": [
                      {
                        "uniform_name": "mus-ost-developers"
                      },
                      {
                        "uniform_name": "play-foeniks-a-developers"
                      },
                      {
                        "uniform_name": "play-foeniks-a-data-admins"
                      },
                      {
                        "uniform_name": "play-foeniks-a-consumers"
                      },
                      {
                        "uniform_name": "play-foeniks-a-editors"
                      }
                    ]
                  }
                }
                """.formatted(userPrincipalName)
         );
    }

}
