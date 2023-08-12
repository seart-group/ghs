package ch.usi.si.seart.collection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * An infinite cycle over an {@code Iterable} collection.
 * Useful in cases when one needs to continuously loop over
 * an iterable collection, one element at a time.
 * The backing iterable data structure used to construct
 * an instance <em>can</em> be empty, although an empty cycle
 * is not terribly useful.
 *
 * @see Iterables#cycle(Object[]) Iterables.cycle
 * @author Ozren Dabić
 * @param <T> the type of elements in the cycle.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Cycle<T> implements Iterator<T> {

    int size;

    Iterable<T> iterable;

    Iterator<T> iterator;

    public Cycle(Iterable<T> iterable) {
        Objects.requireNonNull(iterable, "Backing iterable must not be null!");
        this.size = Iterables.size(iterable);
        this.iterable = Iterables.cycle(iterable);
        this.iterator = this.iterable.iterator();
    }

    @SafeVarargs
    public Cycle(T... args) {
        this(Arrays.asList(args));
    }

    public int size() {
        return this.size;
    }

    /**
     * Given that the collection is repeated infinitely,
     * any cycle constructed from a non-empty collection
     * will always have a next element at its disposal.
     * The only exception to this is when the cycle was
     * constructed from an empty {@code Iterable}.
     *
     * @return true if there is a next element in the cycle, false otherwise.
     */
    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    /**
     * Similarly to {@link #hasNext()},
     * this method will always return a value if
     * the iterable used to make the cycle was non-empty.
     * Calling this method from the last element
     * restarts the iteration from the beginning.
     *
     * @return the next element in the cycle.
     * @throws java.util.NoSuchElementException if the cycle is empty.
     */
    @Override
    public T next() {
        return this.iterator.next();
    }

    public Stream<T> stream() {
        return Stream.generate(() -> this.iterable).flatMap(Streams::stream);
    }

    public List<T> toList() {
        Iterator<T> iterator = Iterators.limit(this.iterable.iterator(), this.size);
        return ImmutableList.copyOf(iterator);
    }

    public Set<T> toSet() {
        Iterator<T> iterator = Iterators.limit(this.iterable.iterator(), this.size);
        return ImmutableSet.copyOf(iterator);
    }

    @Override
    public String toString() {
        return toList().toString();
    }
}
