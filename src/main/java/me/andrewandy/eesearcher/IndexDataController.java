package me.andrewandy.eesearcher;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.zaxxer.hikari.pool.HikariPool;
import me.andrewandy.eesearcher.data.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

public class IndexDataController {

    public static final int MAX_QUERY_CACHE_SIZE = 20;

    private final Cache<IndexData, Essay> indexDataCache = CacheBuilder.newBuilder()
            .concurrencyLevel(2)
            .weakValues()
            .<IndexData, Essay>removalListener(listener -> {
                final Essay essay = listener.getValue();
                try {
                    essay.close();
                } catch (IOException ex) {
                    // Should never happen!
                    ex.printStackTrace();
                }
            }).build();

    private final Cache<QueryParameters, Set<SearchResult>> queryCache = CacheBuilder.newBuilder()
            .concurrencyLevel(2)
            .maximumSize(MAX_QUERY_CACHE_SIZE)
            .build();

    private final Map<QueryParameters, CompletableFuture<Set<SearchResult>>> pendingQueries = new ConcurrentHashMap<>();

    @Inject
    private DataUtil dataUtil;
    @Inject
    @Named("internal-pool")
    private HikariPool connectionPool;
    @Inject
    private ScheduledExecutorService executorService;

    private synchronized CompletableFuture<Set<SearchResult>> performQueryAsync(@NotNull QueryParameters queryParameters) {
        final CompletableFuture<Set<SearchResult>> pending = pendingQueries.get(queryParameters);
        if (pending != null) {
            return pending;
        }
        final CompletableFuture<Set<SearchResult>> completableFuture = new CompletableFuture<>();
        pendingQueries.put(queryParameters, completableFuture);
        executorService.submit(() -> {
            completableFuture.complete(performQuerySync(queryParameters));
            pendingQueries.remove(queryParameters);
        });
        return completableFuture;
    }

    private Set<SearchResult> performQuerySync(QueryParameters queryParameters) {
        final CompletableFuture<Set<SearchResult>> completableFuture = new CompletableFuture<>();
        pendingQueries.put(queryParameters, completableFuture);
        final Set<SearchResult> results = new HashSet<>();
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement query = dataUtil.newSearch(connection, queryParameters, -1);
             ResultSet resultSet = query.executeQuery()) {
            while (resultSet.next()) {
                try {
                    final Essay essay = dataUtil.extractEssay(resultSet, this::getCachedEssay);
                    indexDataCache.put(essay.getIndexData(), essay);
                    results.add(new SearchResult(essay, Collections.emptyList()));
                } catch (IOException ex) {

                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            completableFuture.completeExceptionally(ex);
        }
        queryCache.put(queryParameters, results);
        pendingQueries.remove(queryParameters);
        if (!completableFuture.isCompletedExceptionally()) {
            completableFuture.complete(results);
        }
        return results;
    }

    public @NotNull Optional<@NotNull Essay> getCachedEssay(@NotNull IndexData indexData) {
        return Optional.ofNullable(indexDataCache.getIfPresent(indexData));
    }

    public synchronized @NotNull CompletableFuture<@NotNull Set<@NotNull SearchResult>> performQuery(@NotNull QueryParameters queryParameters) {
        final Optional<Set<SearchResult>> optionalSearchResult = getCachedResult(queryParameters);
        return optionalSearchResult.map(CompletableFuture::completedFuture).orElseGet(() -> performQueryAsync(queryParameters));
    }

    public @NotNull Optional<@NotNull Set<@NotNull SearchResult>> getCachedResult(@NotNull QueryParameters searchQueryParameters) {
        return Optional.ofNullable(queryCache.getIfPresent(searchQueryParameters));
    }


}
