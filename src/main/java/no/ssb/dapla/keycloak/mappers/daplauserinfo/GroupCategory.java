package no.ssb.dapla.keycloak.mappers.daplauserinfo;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;

public enum GroupCategory {
    MANAGERS("managers"), // Will be removed after migration to new api
    DEVELOPERS("developers"),
    DATA_ADMINS("data-admins");

    public final String value;
    GroupCategory(String value) {
        this.value = value;
    }


    public static final String ALLOWED_GROUP_CATEGORIES_PIPE_SEPARATED = Arrays.stream(values())
            .map(category -> category.value)
            .collect(joining("|"));

}