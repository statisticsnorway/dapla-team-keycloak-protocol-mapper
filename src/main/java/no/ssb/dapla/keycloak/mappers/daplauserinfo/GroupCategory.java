package no.ssb.dapla.keycloak.mappers.daplauserinfo;

public class GroupCategory {
    public static final String MANAGERS = "managers"; // Will be removed after migration to new api
    public static final String DEVELOPERS = "developers";
    public static final String DATA_ADMINS = "data-admins";

    public static final String ALLOWED_GROUP_CATEGORIES_PIPE_SEPARATED = MANAGERS + "|" + DEVELOPERS + "|" + DATA_ADMINS;

}