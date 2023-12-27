package ch.usi.si.seart.collection;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Extends the functionality of a {@link LinkedHashMap}
 * to add least recently used (LRU) Entry eviction.
 * Instances are created and maintained at a specific capacity,
 * with oldest-inserted entries being removed once the limit is exceeded.
 *
 * @author Ozren Dabić
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class LRUHashMap<K, V> extends LinkedHashMap<K, V> {

    int maxCapacity;

    public LRUHashMap(int maxCapacity) {
        super(maxCapacity);
        this.maxCapacity = maxCapacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxCapacity;
    }
}
