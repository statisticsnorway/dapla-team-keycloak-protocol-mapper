package no.ssb.dapla.keycloak.services.model;

import java.util.ArrayList;

public record DaplaTeam(

        String uniformName,
        String displayName,
        String sectionCode,
        String sectionName,
        String autonomyLevel,
        ArrayList<DaplaGroup> groups// We want it to be array list since we modify the names if the groups shall be nested
) {
        public DaplaTeam {
                // To avoid NPE when we try to access groups after deserialization
                groups = new ArrayList<>();
        }

        public DaplaTeam(String uniformName, String displayName, String sectionCode, String sectionName, String autonomyLevel) {
                this(uniformName, displayName, sectionCode, sectionName, autonomyLevel, new ArrayList<>());
        }
}
