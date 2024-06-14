package no.ssb.dapla.keycloak.mappers.daplauserinfo;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

public class GroupSuffixFilter {

    /**
     * Get allowed suffixes for a list of teams and groups.
     * <p>
     * Group by group, check all supplied teams and find the longest prefixing teamname + a dash (-).
     * The longest prefix is removed from the start of the group name, and the resulting suffix is preserved.
     * </p>
     *
     * @param teams
     * @param groups
     * @return
     */
    public static Set<String> allowedSuffixes(Collection<String> teams, Collection<String> groups) {
        Set<String> suffixes = new LinkedHashSet<>();

        for (String group : groups) {
            String longestPrefix = teams.stream()
                    .filter(group::startsWith)
                    .max(Comparator.comparingInt(String::length))
                    .orElse("");

            if (!longestPrefix.isEmpty()) {
                String suffix = group.substring(longestPrefix.length() + 1);
                suffixes.add(suffix);
            }
        }

        return suffixes;
    }

}