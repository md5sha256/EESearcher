package me.andrewandy.eesearcher;

public final class Subjects {

    private static final SubjectRegistry registryInstance = new SubjectRegistry();

    // Group 1 (Language)

    // Group 2 (Language Acquisition)

    // Group 3 (Individuals & Societies)

    // Group 4 (Sciences)
    public static final Subject PHYSICS;
    public static final Subject CHEMISTRY;
    public static final Subject BIOLOGY;
    public static final Subject COMPUTER_SCIENCE;
    public static final Subject SPORT_EXERCISE_HEALTH_SCIENCE;
    public static final Subject DESIGN_TECH;

    // Group 5 (Mathematics)
    public static final Subject MATH_AA_2020;
    public static final Subject MATH_AI_2020;
    public static final Subject MATH_STUDIES_2020;

    // Group 6 (Arts)


    static {
        PHYSICS = group4("Physics", true);
        CHEMISTRY = group4("Chemistry", true);
        BIOLOGY = group4("Biology", true);
        COMPUTER_SCIENCE = group4("Computer Science", true, "CS");
        SPORT_EXERCISE_HEALTH_SCIENCE = group4("Sport, Exercise and Health Science", true);
        DESIGN_TECH = group4("Design Technology", true, "DT", "Design Tech");

        MATH_AA_2020 = group5("Mathematics: Analysis and Approaches", true, "Math AA");
        MATH_AI_2020 = group5("Mathematics: Applications and Interpretation", true, "Math AI");
        MATH_STUDIES_2020 = group5("Mathematical Studies", true, "Math Studies");

    }

    public static SubjectRegistry getSubjectRegistry() {
        return registryInstance;
    }

    private Subjects() {

    }

    private static Subject group1(final String displayName, final boolean active, final String... aliases) {
        final Subject s = new Subject((byte) 1, displayName, active, aliases);
        registryInstance.registerSubject(s);
        return s;
    }

    private static Subject group2(final String displayName, final boolean active, final String... aliases) {
        final Subject s = new Subject((byte) 2, displayName, active, aliases);
        registryInstance.registerSubject(s);
        return s;
    }

    private static Subject group3(final String displayName, final boolean active, final String... aliases) {
        final Subject s = new Subject((byte) 3, displayName, active, aliases);
        registryInstance.registerSubject(s);
        return s;
    }

    private static Subject group4(final String displayName, final boolean active, final String... aliases) {
        final Subject s = new Subject((byte) 4, displayName, active, aliases);
        registryInstance.registerSubject(s);
        return s;
    }

    private static Subject group5(final String displayName, final boolean active, final String... aliases) {
        final Subject s = new Subject((byte) 5, displayName, active, aliases);
        registryInstance.registerSubject(s);
        return s;
    }

    private static Subject group6(final String displayName, final boolean active, final String... aliases) {
        final Subject s = new Subject((byte) 6, displayName, active, aliases);
        registryInstance.registerSubject(s);
        return s;
    }


}
