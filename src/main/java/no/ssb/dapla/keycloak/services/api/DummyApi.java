package no.ssb.dapla.keycloak.services.api;

import lombok.RequiredArgsConstructor;
import no.ssb.dapla.keycloak.services.model.DaplaGroup;
import no.ssb.dapla.keycloak.services.model.DaplaTeam;
import no.ssb.dapla.keycloak.services.model.DaplaUser;
import no.ssb.dapla.keycloak.services.model.DaplaUserInfo;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class DummyApi implements ApiService {
    private static final Logger log = Logger.getLogger(DummyApi.class);
    public static final String NAME = "Dummy";

    @Override
    public DaplaUserInfo getDaplaUserInfo(String userPrincipalName, Pattern groupCategoriesToInclude) {
        var user = new DaplaUser("Mus, Mikke", userPrincipalName, "399", "399 Nærings- og miljøstatistikk", false);

        return new DaplaUserInfo(
                user,
                List.of(
                        new DaplaTeam(
                                "dapla-felles",
                                "Dapla Felles",
                                "724",
                                "Dataplattform (724)",
                                "MANAGED", withGroups(groupCategoriesToInclude)),
                        new DaplaTeam("mu",
                                "Team Mu",
                                "399",
                                "Nærings- og miljøstatistikk (399)",
                                "MANAGED", withGroups(groupCategoriesToInclude,
                                new DaplaGroup("mu-developers")
                        )),
                        new DaplaTeam("mus",
                                "Team Mus",
                                "399",
                                "Nærings- og miljøstatistikk (399)",
                                "MANAGED",
                                withGroups(groupCategoriesToInclude,
                                        new DaplaGroup("mus-developers"),
                                        new DaplaGroup("mus-data-admins")
                                )),
                        new DaplaTeam("mus-ost",
                                "Team Ost",
                                "399",
                                "Nærings- og miljøstatistikk (399)",
                                "SELF_MANAGED", withGroups(groupCategoriesToInclude,
                                new DaplaGroup("mus-ost-developers"),
                                new DaplaGroup("mus-ost-developers-mysuffix")
                        )),
                        new DaplaTeam("play-foeniks-a",
                                "Play Føniks A",
                                "724",
                                "Dataplattform (724)",
                                "SELF_MANAGED", withGroups(groupCategoriesToInclude,
                                new DaplaGroup("play-foeniks-a-developers"),
                                new DaplaGroup("play-foeniks-a-data-admins")
                        ))
                )
        );
    }

    private ArrayList<DaplaGroup> withGroups(Pattern groupCategoriesToInclude, DaplaGroup... groups) {
        return new ArrayList<>(Arrays.stream(groups).filter(group -> groupCategoriesToInclude == null || groupCategoriesToInclude.matcher(group.name()).matches()).toList());
    }

}
