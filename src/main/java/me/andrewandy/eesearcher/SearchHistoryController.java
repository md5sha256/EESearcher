package me.andrewandy.eesearcher;

import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

/**
 * Thread-Safe implementation of the SearchHistory.
 * All methods can be executed from multiple threads.
 */
@Singleton
public class SearchHistoryController {

    private final LinkedList<String> history = new LinkedList<>();

    private transient final Set<String> cache = new HashSet<>();
    public volatile transient int HISTORY_MAX_SIZE = 30;

    public synchronized void save(final File file) throws IOException {
        final LinkedList<String> copy;
        synchronized (this.history) {
            copy = new LinkedList<>(this.history);
        }
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream outputStream = new ObjectOutputStream(fos)) {
            outputStream.writeObject(copy);
        }
    }

    public synchronized void loadData(final File file, boolean mergeData) throws IOException {
        final LinkedList<String> serial;
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream inputStream = new ObjectInputStream(fis)) {
            final Object obj = inputStream.readObject();
            if (!(obj instanceof LinkedList)) {
                // FIXME log invalid object.
                return;
            }
            final LinkedList<?> linkedList = (LinkedList<?>) obj;
            final Iterator<?> iterator = linkedList.iterator();
            while (iterator.hasNext()) {
                final Object o = iterator.next();
                if (o == null) {
                    // Gracefully handle null entries from deserialization
                    iterator.remove();
                    continue;
                }
                if (!(o instanceof String)) {
                    // FIXME log invalid list
                    return;
                }
            }
            // Potential performance improvement: pre-calculate how many elements should be added / will be truncated
            synchronized (this.history) {
                if (!mergeData) {
                    this.history.clear();
                    this.cache.clear();
                }
                this.history.addAll((LinkedList<String>) linkedList);
                this.cache.addAll(this.history);
            }
            truncateHistory();
        } catch (ClassNotFoundException ex) {
            // Should never happen since LinkedList is a part of stdlib; indicates data is corrupted
            // FIXME log CNFE
        }
    }

    public @NotNull List<@NotNull String> getHistory() {
        return new ArrayList<>(this.history);
    }

    public void offerHistory(final @NotNull List<@NotNull String> history) {
        synchronized (this.history) {
            this.history.clear();
            for (String s : history) {
                // Forcefully validate objects aren't null
                if (s == null) {
                    continue;
                }
                this.cache.add(s);
                this.history.add(s);
            }
        }
    }

    public void addEntry(final String entry) {
        synchronized (this.history) {
            final String added = Objects.requireNonNull(entry).toLowerCase(Locale.ROOT);
            this.history.add(added);
            this.cache.add(added);
        }
    }

    public @NotNull Optional<@NotNull String> removeEntry() {
        synchronized (this.history) {
            if (this.history.isEmpty()) {
                return Optional.empty();
            }
            final String removed = this.history.remove();
            this.cache.remove(removed);
            return Optional.of(this.history.remove());
        }
    }

    public void removeEntry(@NotNull String entry) {
        synchronized (this.history) {
            if (this.cache.remove(entry)) {
                this.history.remove(entry);
            }
        }
    }

    public @NotNull Optional<@NotNull String> lastEntry() {
        synchronized (this.history) {
            if (this.history.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(this.history.getLast());
        }
    }

    public boolean containsEntry(@NotNull String entry) {
        synchronized (this.history) {
            // Cache should be locked in parallel with history
            return this.cache.contains(entry.toLowerCase(Locale.ROOT));
        }
    }

    public void truncateHistory() {
        synchronized (this.history) {
            // Cache the max size as it may change, even if it probably won't
            final int maxSize = HISTORY_MAX_SIZE;
            if (this.history.size() < maxSize) {
                return;
            }
            final Iterator<String> descendingIterator = this.history.descendingIterator();
            for (int toRemove = this.history.size() - maxSize; toRemove > 0; toRemove--) {
                this.cache.remove(descendingIterator.next());
                descendingIterator.remove();
            }
        }
    }

}
