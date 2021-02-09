package me.andrewandy.eesearcher.data;

import com.google.inject.Inject;

public final class Subjects {


    // Group 1 (Language)

    // Group 2 (Language Acquisition)
    public final Subject ECONOMICS;
    public final Subject BUSINESS_MANAGEMENT;
    public final Subject ITGS;
    public final Subject GEOGRAPHY;
    public final Subject PHILOSPHY;
    public final Subject GLOBAL_POLITICS;
    public final Subject PSYCHOLOGY;
    public final Subject ANTHROPOLOGY;
    public final Subject WORLD_RELIGION;

    // Group 3 (Individuals & Societies)
    public final Subject HISTORY;

    // Group 4 (Sciences)
    public final Subject PHYSICS;
    public final Subject CHEMISTRY;
    public final Subject BIOLOGY;
    public final Subject COMPUTER_SCIENCE;
    public final Subject SPORT_EXERCISE_HEALTH_SCIENCE;
    public final Subject DESIGN_TECH;
    // Group 5 (Mathematics)
    public final Subject MATH_AA_2020;
    public final Subject MATH_AI_2020;
    public final Subject MATH_STUDIES_2020;

    private final SubjectDatabase registryInstance;

    // Group 6 (Arts)

    @Inject
    public Subjects(SubjectDatabase database) {
        this.registryInstance = database;
        HISTORY = group3("History", true);
        ECONOMICS = group3("Economics", true, "Econ");
        BUSINESS_MANAGEMENT = group3("Business Management", true, "BM");
        ITGS = group3("ITGS", true, "Information Technology in Global Societies");
        GEOGRAPHY = group3("Geography", true, "Geo");
        PHILOSPHY = group3("Philosophy", true);
        GLOBAL_POLITICS = group3("Global Politics", true, "Politics");
        PSYCHOLOGY = group3("Psychology", true, "Psych");
        ANTHROPOLOGY = group3("Anthropology", true, "Social and Cultural Anthropology");
        WORLD_RELIGION = group3("World Religion", true, "Religion");

        PHYSICS = group4("Physics", true);
        CHEMISTRY = group4("Chemistry", true);
        BIOLOGY = group4("Biology", true);
        COMPUTER_SCIENCE = group4("Computer Science", true, "CS");
        SPORT_EXERCISE_HEALTH_SCIENCE = group4("Sport, Exercise and Health Science", true);
        DESIGN_TECH = group4("Design Technology", true, "DT", "Design Tech");

        MATH_AA_2020 = group5("Mathematics: Analysis and Approaches", true, "Math AA");
        MATH_AI_2020 = group5("Mathematics: Applications and Interpretation", true, "Math AI");
        MATH_STUDIES_2020 = group5("Mathematics", true,
                "Math Studies",
                "Mathematics: Analysis and Approaches",
                "Math AA",
                "Mathematics: Applications and Interpretation",
                "Math AI",
                "Maths");

    }

    private Subject group1(final String displayName, final boolean active, final String... aliases) {
        final Subject s = new Subject((byte) 1, displayName, active, aliases);
        registryInstance.registerSubject(s);
        return s;
    }

    private Subject group2(final String displayName, final boolean active, final String... aliases) {
        final Subject s = new Subject((byte) 2, displayName, active, aliases);
        registryInstance.registerSubject(s);
        return s;
    }

    private Subject group3(final String displayName, final boolean active, final String... aliases) {
        final Subject s = new Subject((byte) 3, displayName, active, aliases);
        registryInstance.registerSubject(s);
        return s;
    }

    private Subject group4(final String displayName, final boolean active, final String... aliases) {
        final Subject s = new Subject((byte) 4, displayName, active, aliases);
        registryInstance.registerSubject(s);
        return s;
    }

    private Subject group5(final String displayName, final boolean active, final String... aliases) {
        final Subject s = new Subject((byte) 5, displayName, active, aliases);
        registryInstance.registerSubject(s);
        return s;
    }

    private Subject group6(final String displayName, final boolean active, final String... aliases) {
        final Subject s = new Subject((byte) 6, displayName, active, aliases);
        registryInstance.registerSubject(s);
        return s;
    }


}
