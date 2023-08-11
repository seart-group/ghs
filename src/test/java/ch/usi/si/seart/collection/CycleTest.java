package ch.usi.si.seart.collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

class CycleTest {

    @Test
    void cycleTest() {
        Cycle<Integer> cycle = new Cycle<>(1, 2, 3, 4);
        Assertions.assertEquals(4, cycle.size());
        Assertions.assertTrue(cycle.hasNext());
        Assertions.assertEquals(1, cycle.next());
        Assertions.assertTrue(cycle.hasNext());
        Assertions.assertEquals(2, cycle.next());
        Assertions.assertTrue(cycle.hasNext());
        Assertions.assertEquals(3, cycle.next());
        Assertions.assertTrue(cycle.hasNext());
        Assertions.assertEquals(4, cycle.next());
        Assertions.assertTrue(cycle.hasNext());
        Assertions.assertEquals(1, cycle.next());
        Assertions.assertTrue(cycle.hasNext());
        Assertions.assertEquals(2, cycle.next());
        Assertions.assertTrue(cycle.hasNext());
        Assertions.assertEquals(3, cycle.next());
        Assertions.assertTrue(cycle.hasNext());
        Assertions.assertEquals(4, cycle.next());
        Assertions.assertTrue(cycle.hasNext());
    }

    @Test
    void singletonCycleTest() {
        Cycle<Integer> cycle = new Cycle<>(1);
        Assertions.assertEquals(1, cycle.size());
        Assertions.assertTrue(cycle.hasNext());
        Assertions.assertEquals(1, cycle.next());
        Assertions.assertTrue(cycle.hasNext());
        Assertions.assertEquals(1, cycle.next());
        Assertions.assertTrue(cycle.hasNext());
    }

    @Test
    void emptyCycleTest() {
        Cycle<Integer> cycle = new Cycle<>();
        Assertions.assertEquals(0, cycle.size());
        Assertions.assertFalse(cycle.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, cycle::next);
    }

    @Test
    void streamTest() {
        Cycle<Integer> cycle = new Cycle<>(1, 2, 3, 4);
        List<Integer> expected = List.of(1, 2, 3, 4, 1, 2, 3, 4, 1, 2);
        List<Integer> actual;
        // Un-queried cycle
        actual = cycle.stream()
                .limit(expected.size())
                .toList();
        Assertions.assertEquals(expected, actual);
        // Queried cycle
        cycle.next();
        actual = cycle.stream()
                .limit(expected.size())
                .toList();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void toListTest() {
        Cycle<Integer> cycle = new Cycle<>(1, 2, 3, 4);
        List<Integer> expected = List.of(1, 2, 3, 4);
        List<Integer> actual;
        // Un-queried cycle
        actual = cycle.toList();
        Assertions.assertEquals(expected, actual);
        // Queried cycle
        cycle.next();
        actual = cycle.toList();
        Assertions.assertEquals(expected, actual);
        // Empty cycle
        Assertions.assertEquals(List.of(), new Cycle<>().toList());
    }

    @Test
    void toSetTest() {
        Cycle<Integer> cycle = new Cycle<>(1, 2, 3, 4);
        Set<Integer> expected = Set.of(1, 2, 3, 4);
        Set<Integer> actual;
        // Un-queried cycle
        actual = cycle.toSet();
        Assertions.assertEquals(expected, actual);
        // Queried cycle
        cycle.next();
        actual = cycle.toSet();
        Assertions.assertEquals(expected, actual);
        // Empty cycle
        Assertions.assertEquals(Set.of(), new Cycle<>().toSet());
    }

    @Test
    void toStringTest() {
        Cycle<Integer> cycle = new Cycle<>(1, 2, 3, 4);
        String expected = "[1, 2, 3, 4]";
        String actual;
        // Un-queried cycle
        actual = cycle.toString();
        Assertions.assertEquals(expected, actual);
        // Queried cycle
        cycle.next();
        actual = cycle.toString();
        Assertions.assertEquals(expected, actual);
        // Empty cycle
        Assertions.assertEquals("[]", new Cycle<>().toString());
    }
}