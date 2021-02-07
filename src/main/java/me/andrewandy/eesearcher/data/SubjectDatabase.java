package me.andrewandy.eesearcher.data;

import java.io.Serializable;
import java.util.*;

public final class SubjectDatabase implements Serializable {

    private final List<Subject> subjects = new ArrayList<>();
    private final List<Subject> activeSubjects = new ArrayList<>();
    private final List<Subject> inActiveSubjects = new ArrayList<>();
    private final Map<String, Subject> displayNameMap = new HashMap<>();
    private final Map<Byte, Set<Subject>> groupSubjectMap = new HashMap<>();

    SubjectDatabase() {
        for (int i = 0; i < 7; i++) {
            groupSubjectMap.put((byte) i, new HashSet<>());
        }
        unsafeRegisterSubject(Subject.WORLD_STUDIES);
    }

    private static void validateGroup(final byte group) {
        switch (group) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return;
            default:
                throw new IllegalArgumentException("Invalid Group: " + group);
        }
    }

    public Optional<Subject> getSubjectByName(final String name) {
        return Optional.ofNullable(displayNameMap.get(name)).or(() -> {
            for (Subject s : subjects) {
                if (s.isSubject(name)) {
                    return Optional.of(s);
                }
            }
            return Optional.empty();
        });
    }

    public Set<Subject> getSubjectsByName(final String name) {
        final Set<Subject> byName = new HashSet<>();
        for (Subject s : subjects) {
            if (s.isSubject(name)) {
                byName.add(s);
            }
        }
        return byName;
    }

    public Set<Subject> getSubjectsByGroup(final byte group) {
        validateGroup(group);
        return new HashSet<>(groupSubjectMap.get(group));
    }

    public List<Subject> getActiveSubjects() {
        return new ArrayList<>(activeSubjects);
    }

    public List<Subject> getInActiveSubjects() {
        return new ArrayList<>(inActiveSubjects);
    }

    public boolean isSubject(final String name) {
        return getSubjectByName(name).isPresent();
    }

    public void registerSubject(final Subject subject) throws IllegalArgumentException {
        checkSubjectValidity(subject);
        unsafeRegisterSubject(subject);
    }

    private void checkSubjectValidity(final Subject subject) throws IllegalArgumentException {
        if (isSubject(subject.getDisplayName())) {
            throw new IllegalArgumentException("Invalid Subject: " + subject.getDisplayName());
        }
    }

    private void unsafeRegisterSubject(final Subject subject) {
        subjects.add(subject);
        displayNameMap.put(subject.getDisplayName(), subject);
        groupSubjectMap.get(subject.getGroup()).add(subject);
        if (subject.isActive()) {
            activeSubjects.add(subject);
        } else {
            inActiveSubjects.remove(subject);
        }
    }


}
