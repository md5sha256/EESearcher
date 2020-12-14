package me.andrewandy.eesearcher;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KeywordFinder {

    private static final Collection<String> exclusions = new HashSet<>();
    static {
        final List<String> list = Arrays.asList("they",
                "that", "with", "from", "which",
                "because", "very", "this", "when");
        exclusions.addAll(list);
    }

    private Map<String, Integer> results;
    private List<String> processed;

    public void count(final String text, boolean reset) {
        if (reset) {
            results = null;
        }
        Map<String, Integer> map = new HashMap<>();
        final Pattern pattern = Pattern.compile("(\\w+)");
        final Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            final String s = matcher.group(1).toLowerCase();
            if (s.length() < 4) {
                continue;
            }
            map.compute(s, (key, value) -> {
                if (value == null) {
                    return 1;
                }
                return value + 1;
            });
        }
        map.keySet().removeAll(exclusions);
        this.results = map;
        this.processed = processResults(map);
    }

    public static List<String> processResults(Map<String, Integer> map) {
        return map.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Comparator.comparingInt(Map.Entry::getValue)))
                .map(Map.Entry::getKey)
                .limit(30)
                .collect(Collectors.toList());
    }

    public List<String> getProcessedResults() {
        return this.processed == null ? Collections.emptyList() : new ArrayList<>(this.processed);
    }

    public Map<String, Integer> getResults() {
        return this.results == null ? Collections.emptyMap() : new HashMap<>(this.results);
    }

}
