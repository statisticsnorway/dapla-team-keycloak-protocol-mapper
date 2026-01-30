package no.ssb.dapla.keycloak.services.model;

public record DaplaUser(
        String name,
        String email,
        String sectionCode,
        String sectionName,
        Boolean isSectionManager
) {
}
