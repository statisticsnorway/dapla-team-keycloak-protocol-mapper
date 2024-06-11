# dapla-team-keycloak-protocol-mapper

Keycloak protocol mappers for Dapla team info.

Protocol Mappers in Keycloak offer a flexible way to manage, transform, and map user data between Keycloak and client
applications, ensuring that each application gets the necessary user attributes in the expected format.

This library provides an assortment of custom Keycloak protocol mappers that can add Dapla specific information to tokens.

## Installation

Place the dapla-team-keycloak-protocol-mapper.jar into Keycloak's providers directory (`/path/to/keycloak/providers`).
Restart and Keycloak will discover and install the protocol mappers automatically.


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

#### Dapla Teams Mapper

```terraform
resource "keycloak_generic_protocol_mapper" "dapla_teams_mapper" {
  realm_id        = keycloak_realm.realm.id
  client_id       = keycloak_openid_client.some_client.id
  name            = "dapla-teams"
  protocol        = "openid-connect"
  protocol_mapper = "oidc-dapla-teams-mapper"

  config = {
    "claim.name"           = "teams"
    "jsonType.label"       = "JSON"
    "id.token.claim"       = true
    "access.token.claim"   = true
    "userinfo.token.claim" = true
    "dapla-team-api.impl"  = "Mocky"
    "dapla-team-api.url"   = "https://run.mocky.io"
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
