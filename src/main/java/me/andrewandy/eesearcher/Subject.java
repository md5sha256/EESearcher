package me.andrewandy.eesearcher;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class Subject implements Serializable {

    private final boolean isActive;
    private final byte group;
    private final String displayName;
    private final Set<String> aliases;

    public Subject(final byte group, final String displayName, boolean isActive, final String... aliases) {
        this.group = switch (group) {
            case 1, 2, 3, 4, 5, 6:
                yield group;
            default:
                throw new IllegalArgumentException("Invalid Group!");
        };
        this.isActive = isActive;
        this.displayName = displayName.toLowerCase();
        if (aliases != null) {
            this.aliases = Arrays.stream(aliases)
                    .filter(Objects::nonNull)
                    .map(String::toLowerCase)
                    .collect(Collectors.toUnmodifiableSet());
        } else {
            this.aliases = Collections.emptySet();
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public byte getGroup() {
        return group;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public boolean isSubject(final String name) {
        if (name == null) {
            return false;
        }
        return this.displayName.equalsIgnoreCase(name) || this.aliases.contains(name.toLowerCase());
    }

}
