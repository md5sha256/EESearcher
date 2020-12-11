package me.andrewandy.eesearcher;

import java.util.Collection;
import java.util.regex.Pattern;

@FunctionalInterface
public interface Subject {

    Collection<Pattern> getPatterns();

}
