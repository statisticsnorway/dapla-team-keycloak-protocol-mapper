# Local Keycloak Setup

Start a local Keycloak instance using Docker Compose:

```shell
docker-compose up --force-recreate
```

Inspect your local Keycloak container:

```shell
docker exec -it keycloak bash
```

To use the latest version of `dapla-team-keycloak-protocol-mapper` in your local Keycloak instance,
sync the latest built JAR file:

```shell
cp ../target/dapla-team-keycloak-protocol-mapper-*.jar ./custom-providers/dapla-team-keycloak-protocol-mapper.jar
```

The `localstack/custom-providers` directory is mounted as a volume in the Keycloak container, and the JAR file is
automatically deployed when restarting Keycloak. You don't need to rebuild the Docker image.

You can access the Keycloak admin console at [http://localhost:18080](http://localhost:18080) with
username `admin` and password `admin`.

## Development

Code, deploy, restart, test, repeat - all the things ♻️

```shell
mvn clean install -Dmaven.test.skip=true -f ../pom.xml && \
cp ../target/dapla-team-keycloak-protocol-mapper-*.jar ./custom-providers/dapla-team-keycloak-protocol-mapper.jar && \
docker-compose up --force-recreate
```
(while in the `localstack` directory)


#### Other tricks:

* Set the `KEYCLOAK_LOG_LEVEL` environment variable in the docker-compose.yml file to `DEBUG` to get more detailed logs.
* Scripts within in the `localstack/init-scripts` directory are run on Keycloak startup. This can be handy if e.g. you need to
download other providers or manipulate container internal files.