package no.ssb.dapla.keycloak.services.model;

import java.util.List;

public record DaplaUserInfo(DaplaUser user, List<DaplaTeam> teams) {
}
