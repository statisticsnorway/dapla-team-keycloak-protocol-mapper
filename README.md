# dapla-team-keycloak-protocol-mapper

Keycloak protocol mappers for Dapla team info.

Protocol Mappers in Keycloak offer a flexible way to manage, transform, and map user data between Keycloak and client
applications, ensuring that each application gets the necessary user attributes in the expected format.

This library provides an assortment of custom Keycloak protocol mappers that can add Dapla specific information to tokens.

## Installation

Place the dapla-team-keycloak-protocol-mapper.jar into Keycloak's providers directory (`/path/to/keycloak/providers`).
Restart and Keycloak will discover and install the protocol mappers automatically.

## Expected environment variables

The following environment variables are expected to be set:

| Env                                                 | Description                                                              | Example                                                                               |
|-----------------------------------------------------|--------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_AUTH_URL | Auth/token URL used by the Keycloak client with access to Dapla Team API | https://auth.external.prod.ssb.cloud.nais.io/realms/ssb/protocol/openid-connect/token |
| DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_ID       | ID of Keycloak client with access to Dapla Team API                      | dapla-team-protocol-mapper                                                            |
| DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_SECRET   | Client secret for Keycloak client with access to Dapla Team API          | <some secret>                                                                         |


## Terraform

You can use Terraform to configure a client to use the protocol mappers. The following examples
use [mrparkers Keycloak provider](https://registry.terraform.io/providers/mrparkers/keycloak).

Given a realm and client:

```terraform
resource "keycloak_realm" "realm" {
  realm   = "my-realm"
  enabled = true
}

resource "keycloak_openid_client" "some_client" {
  realm_id  = keycloak_realm.realm.id
  client_id = "some-client"
}
```

#### Dapla UserInfo Mapper

```terraform
resource "keycloak_generic_protocol_mapper" "dapla_userinfo_mapper" {
  realm_id        = keycloak_realm.realm.id
  client_id       = keycloak_openid_client.some_client.id
  name            = "dapla-teams"
  protocol        = "openid-connect"
  protocol_mapper = "oidc-dapla-userinfo-mapper"

  config = {
    "claim.name"                                  = "dapla"
    "jsonType.label"                              = "JSON"
    "id.token.claim"                              = true
    "access.token.claim"                          = true
    "userinfo.token.claim"                        = true
    "dapla-team-api.impl"                         = "Default"
    "dapla-team-api.url"                          = "https://dapla-team-api-v2.prod-bip-app.ssb.no"
    "dapla.userinfo.nested"                       = false
    "dapla.userinfo.group-suffix-include-regex"   = "developers|data-admins"
    "dapla.userinfo.exclude-teams-without-groups" = true
    "dapla.userinfo.user-props"                   = "section_code"
    "dapla.userinfo.team-props"                   = "section_code, autonomy_level, source_data_classification"
  }
}
```

## Versioning scheme

To ensure clarity and intuitiveness regarding this library's compatibility with Keycloak versions,
its major version mirrors that of Keycloak's major version. While the library *might* work with later
Keycloak versions, such compatibility hasn't been verified.


## Development

Use `make` to execute common tasks:
```
build              Build the project and install to your local maven repo
test               Run tests
release-dryrun     Simulate a release in order to detect any issues
release            Release a new version.
```

For local development, see [localstack](localstack/README.md) for instructions on how to run a local Keycloak instance.