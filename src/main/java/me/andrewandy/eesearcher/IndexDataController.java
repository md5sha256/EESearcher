package me.andrewandy.eesearcher;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.zaxxer.hikari.pool.HikariPool;
import me.andrewandy.eesearcher.data.*;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;

public class IndexDataController {

    public static final int MAX_QUERY_CACHE_SIZE = 20;
    private final Cache<QueryParameters, Set<SearchResult>> queryCache = CacheBuilder.newBuilder()
            .concurrencyLevel(2)
            .maximumSize(MAX_QUERY_CACHE_SIZE)
            .build();
    private final Map<QueryParameters, CompletableFuture<Set<SearchResult>>> pendingQueries = new ConcurrentHashMap<>();
    @Inject
    private Parser parser;
    @Inject
    private DataUtil dataUtil;
    @Inject
    @Named("internal-pool")
    private HikariPool connectionPool;
    private final Cache<IndexData, Essay> indexDataCache = CacheBuilder.newBuilder()
            .concurrencyLevel(2)
            .weakKeys()
            .weakValues()
            .<IndexData, Essay>removalListener(listener -> {
                final Essay essay = listener.getValue();
                if (essay == null) {
                    return;
                }
                try (Connection connection = connectionPool.getConnection();
                     PreparedStatement statement = dataUtil.newEntry(connection, essay, true)) {
                    statement.execute();
                    essay.close();
                } catch (SQLException | IOException ex) {
                    // Should never happen!
                    ex.printStackTrace();
                }
            }).build();
    @Inject
    private ScheduledExecutorService executorService;

    private CompletableFuture<Set<SearchResult>> performQueryAsync(@NotNull QueryParameters queryParameters) {
        final CompletableFuture<Set<SearchResult>> pending = pendingQueries.get(queryParameters);
        if (pending != null) {
            return pending;
        }
        return CompletableFuture.completedFuture(performQuerySync(queryParameters));
    }

    private Set<SearchResult> performQuerySync(QueryParameters queryParameters) {
        final CompletableFuture<Set<SearchResult>> completableFuture = new CompletableFuture<>();
        pendingQueries.put(queryParameters, completableFuture);
        final Set<SearchResult> results = new HashSet<>();
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement query = dataUtil.newSearch(connection, queryParameters, -1);
             ResultSet resultSet = query.executeQuery()) {
            while (resultSet.next()) {
                Essay essay;
                try {
                    essay = dataUtil.extractEssay(resultSet, this::getCachedEssay);
                    indexDataCache.put(essay.getIndexData(), essay);
                    results.add(new SearchResult(essay, Collections.emptyList()));
                } catch (IOException ex) {
                    ex.printStackTrace();
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

    public Collection<Essay> getCachedEssays() {
        return new HashSet<>(this.indexDataCache.asMap().values());
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

    public @NotNull CompletableFuture<Void> performIndexing(@NotNull Collection<File> files, BiConsumer<File, Boolean> onCompletion) {
        final Collection<CompletableFuture<Void>> futures = Collections.synchronizedList(new LinkedList<>());
        for (File file : files) {
            final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            futures.add(completableFuture);
            executorService.execute(() -> {
                Exception exception = null;
                try {
                    final PDFParser pdfParser = new PDFParser(new RandomAccessBufferedFileInputStream(file));
                    pdfParser.parse();
                    Essay essay = parser.parseDocument(pdfParser);
                    indexDataCache.put(essay.getIndexData(), essay);
                    dataUtil.newEntry(connectionPool.getConnection(), essay, true).executeUpdate();
                } catch (Exception ex) {
                    exception = new RuntimeException(String.format("Error parsing %s", file), ex);
                } finally {
                    onCompletion.accept(file, exception == null);
                    if (exception != null) {
                        completableFuture.completeExceptionally(exception);
                    } else {
                        completableFuture.complete(null);
                    }
                }
            });
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }


}
