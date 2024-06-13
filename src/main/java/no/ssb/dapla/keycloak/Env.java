package no.ssb.dapla.keycloak;

import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class Env {

    public enum Var {
        DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_AUTH_URL,
        DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_ID,
        DAPLA_TEAM_PROTOCOL_MAPPER_KEYCLOAK_CLIENT_SECRET,
        TEST_USER_PRINCIPAL_NAME
    }

    public static String requiredEnv(Var var) {
        return Optional.ofNullable(System.getenv(var.name()))
                .orElseThrow(() -> new RuntimeException("Missing environment variable: " + var));
    }

    public static String env(Var var, String defaultValue) {
        return Optional.ofNullable(System.getenv(var.name())).orElse(defaultValue);
    }

}
