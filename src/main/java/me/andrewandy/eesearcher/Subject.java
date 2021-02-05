package me.andrewandy.eesearcher;

import java.util.*;
import java.util.stream.Collectors;

public final class Subject {

    private final boolean isActive;
    private final byte group;
    private final String displayName;
    private final Set<String> aliases;
    private long hash = Long.MIN_VALUE;

    public Subject(final byte group, final String displayName, boolean isActive, final String... aliases) {
        switch (group) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                this.group = group;
                break;
            default:
                throw new IllegalArgumentException("Invalid Group!");
        }
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

    public long getHash() {
        if (this.hash == Long.MIN_VALUE) {
            this.hash = Objects.hash(aliases.toArray(new Object[]{displayName}));
        }
        return this.hash;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subject subject = (Subject) o;

        if (isActive != subject.isActive) return false;
        if (group != subject.group) return false;
        if (hash != subject.hash) return false;
        if (!Objects.equals(displayName, subject.displayName)) return false;
        return Objects.equals(aliases, subject.aliases);
    }

    @Override
    public int hashCode() {
        int result = (isActive ? 1 : 0);
        result = 31 * result + (int) group;
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (aliases != null ? aliases.hashCode() : 0);
        result = 31 * result + (int) (hash ^ (hash >>> 32));
        return result;
    }
}
