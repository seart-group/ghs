package ch.usi.si.seart.collection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe map that associates read-write locks with keys.
 * This map allows multiple threads to acquire
 * different locks for different keys concurrently.
 *
 * @author Ozren Dabić
 * @param <K> the type of keys stored in the map.
 */
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConcurrentReadWriteLockMap<K> {

    ConcurrentReferenceHashMap<K, ReadWriteLock> map = new ConcurrentReferenceHashMap<>();

    private ReadWriteLock get(K key) {
        return map.compute(key, (k, v) -> v == null ? new ReentrantReadWriteLock() : v);
    }

    /**
     * Returns the write lock associated with the specified key.
     * If a lock is not already associated with the key,
     * a new {@link ReentrantReadWriteLock} instance is created.
     *
     * @param key the key used to identify the read-write lock.
     * @return the associated write lock.
     */
    public Lock getWriteLock(K key) {
        return get(key).writeLock();
    }

    /**
     * Returns the read lock associated with the specified key.
     * If a lock is not already associated with the key,
     * a new {@link ReentrantReadWriteLock} instance is created.
     *
     * @param key the key used to identify the read-write lock.
     * @return the associated read lock.
     */
    public Lock getReadLock(K key) {
        return get(key).readLock();
    }
}
