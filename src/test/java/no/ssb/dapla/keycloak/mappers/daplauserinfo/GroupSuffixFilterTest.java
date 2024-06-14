package no.ssb.dapla.keycloak.mappers.daplauserinfo;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GroupSuffixFilterTest {

    @Test
    void getAllowedSuffixes() {
        List<String> teams = List.of(
                "dapla-felles",
                "mus",
                "mus-ost",
                "play-foeniks-a"
        );
        List<String> groups = List.of(
                "mus-developers",
                "mus-data-admins",
                "mus-ost-developers",
                "mus-ost-tech-admins",
                "play-foeniks-a-developers",
                "play-foeniks-a-data-admins",
                "play-foeniks-a-consumers",
                "play-foeniks-a-editors"
        );

        Set<String> allowedSuffixes = GroupSuffixFilter.allowedSuffixes(teams, groups);
        assertThat(allowedSuffixes).containsExactlyInAnyOrder(
                "developers", "tech-admins", "data-admins", "consumers", "editors"
        );
    }
}