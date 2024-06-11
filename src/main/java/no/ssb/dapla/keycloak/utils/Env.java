package no.ssb.dapla.keycloak.utils;

import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class Env {

    public static String requiredVar(String name) {
        return Optional.ofNullable(System.getenv(name))
                .orElseThrow(() -> new RuntimeException("Missing environment variable: " + name));
    }

}
