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
                // groups can be null after deserialization if no groups are present. To avoid NPE we set it to a empty list
                if(groups == null) {
                        groups = new ArrayList<>();
                }
        }

        public DaplaTeam(String uniformName, String displayName, String sectionCode, String sectionName, String autonomyLevel) {
                this(uniformName, displayName, sectionCode, sectionName, autonomyLevel, new ArrayList<>());
        }
}
