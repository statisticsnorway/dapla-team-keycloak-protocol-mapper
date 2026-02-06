package no.ssb.dapla.keycloak.services.api;

import org.junit.jupiter.api.Test;

class DaplaApiTest {

    @Test
    void getDaplaUserInfo() {
    }

    String graphQlResponse = """
            {
                "data": {
                    "user": {
                        "name": "Svergja, John Kasper",
                        "email": "hnk@ssb.no",
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