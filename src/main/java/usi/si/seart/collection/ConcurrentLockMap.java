package usi.si.seart.collection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A thread-safe map that associates locks with keys.
 * This map allows multiple threads to acquire
 * locks for different keys concurrently.
 *
 * @author Ozren Dabić
 * @param <K> the type of keys stored in the map.
 */
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConcurrentLockMap<K> {

    ConcurrentReferenceHashMap<K, Lock> map = new ConcurrentReferenceHashMap<>();

    /**
     * Returns the lock associated with the specified key.
     * If a lock is not already associated with the key,
     * a new {@link ReentrantLock} instance is created.
     *
     * @param key the key used to identify the lock.
     * @return the associated lock.
     */
    public Lock get(K key) {
        return map.compute(key, (k, v) -> v == null ? new ReentrantLock() : v);
    }
}
