package me.andrewandy.eesearcher;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Singleton;
import me.andrewandy.eesearcher.data.EEIndexData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Singleton
public class SearchController {

    public static final int MAX_CACHE_SIZE = 20;

    private final Cache<SearchQuery, SearchResult> queryCache = CacheBuilder.newBuilder().concurrencyLevel(2).maximumSize(MAX_CACHE_SIZE).build();
    private final Map<SearchQuery, CompletableFuture<SearchResult>> pendingQueries = new ConcurrentHashMap<>();

    private CompletableFuture<SearchResult> performQuery0(@NotNull SearchQuery query) {
        final CompletableFuture<SearchResult> completableFuture = new CompletableFuture<>();
        pendingQueries.put(query, completableFuture);
        // FIXME
        final Subject dummySubject = new Subject((byte) 3, "ITGS", true);
        final SearchResult result = new SearchResult(new HashMap<>(), new EEIndexData("test", dummySubject, "is A = B?", ExamSession.of("M21")), Collections.emptyList());
        completableFuture.complete(result);
        pendingQueries.remove(query);
        queryCache.put(query, result);
        return completableFuture;
    }

    public synchronized @NotNull CompletableFuture<@NotNull SearchResult> performQuery(@NotNull SearchQuery query) {
        final Optional<SearchResult> optionalSearchResult = getCachedResult(query);
        return optionalSearchResult.map(CompletableFuture::completedFuture).orElseGet(() -> performQuery0(query));
    }

    public @NotNull Optional<SearchResult> getCachedResult(@NotNull SearchQuery searchQuery) {
        return Optional.ofNullable(queryCache.getIfPresent(searchQuery));
    }

    public enum SearchAttribute {
        TEXT,
        SUBJECT,
        EXAM_SESSION,
    }

    public static class SearchQuery {

        private final EnumMap<SearchAttribute, Pattern> attributes;

        public SearchQuery(final Map<SearchAttribute, Pattern> attributes) {
            this.attributes = new EnumMap<>(attributes);
        }

        public SearchQuery(final SearchAttribute attribute, final Pattern pattern) {
            this.attributes = new EnumMap<>(SearchAttribute.class);
            this.attributes.put(attribute, pattern);
        }

        public EnumMap<SearchAttribute, Pattern> getAttributes() {
            return attributes;
        }
    }

    public static class SearchResult {
        private final EnumMap<SearchAttribute, String> attributes = new EnumMap<>(SearchAttribute.class);
        private final EEIndexData indexData;
        private final List<String> matchingText;

        SearchResult(@NotNull final Map<SearchAttribute, String> attributes, @NotNull final EEIndexData indexData, @NotNull final List<String> matchingText) {
            this.attributes.putAll(attributes);
            this.matchingText = new ArrayList<>(matchingText);
            this.indexData = indexData;
        }

        public @NotNull EEIndexData getIndexData() {
            return indexData;
        }

        public Map<SearchAttribute, String> getAttributes() {
            return attributes;
        }

        public List<String> getMatchingText() {
            return matchingText;
        }
    }


}
