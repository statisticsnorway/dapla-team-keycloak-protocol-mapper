package no.ssb.dapla.keycloak.services.model;


import com.fasterxml.jackson.annotation.JsonProperty;

public record DaplaUser(
        String name,
        String email,
        @JsonProperty("section_code")
        String sectionCode,
        @JsonProperty("section_name")
        String sectionName,
        @JsonProperty("is_section_manager")
        Boolean isSectionManager
) {
}
