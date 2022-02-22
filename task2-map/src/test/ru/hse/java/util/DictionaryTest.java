package ru.hse.java.util;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class DictionaryTest {
    private static final int STRESS_TEST_SIZE = 10_000;

    private DictionaryImpl<Integer, Character> dictionary;
    private HashMap<Integer, Character> hashMap;
    private ArrayList<Cell<Integer, Character>> stressData;

    private void generateStressData(int size) {
        Random random = new Random();
        stressData = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stressData.add(new Cell<>(random.nextInt(10000), (char) (random.nextInt(26) + 'a')));
        }
    }

    @BeforeEach
    public void beforeEachTest() {
        dictionary = new DictionaryImpl<>();
        hashMap = new HashMap<>();
    }

    @Test
    public void testCreation() {
        assertEquals(dictionary.size(), 0);
        assertTrue(dictionary.isEmpty());
    }

    @Test
    public void testPutUniqueElements() {
        assertNull(dictionary.put(3, 'a'));
        assertFalse(dictionary.isEmpty());
        assertEquals(dictionary.size(), 1);
        assertNull(dictionary.put(2, 'b'));
        assertEquals(dictionary.size(), 2);
    }

    @Test
    public void testPutOnExistingKey() {
        assertNull(dictionary.put(3, 'a'));
        assertEquals(dictionary.put(3, 'b'), 'a');
        assertEquals(dictionary.size(), 1);
    }

    @Test
    public void testPutWithNullKey() {
        assertNull(dictionary.put(null, 'a'));
        assertEquals(dictionary.size(), 1);
        assertEquals(dictionary.put(null, null), 'a');
        assertEquals(dictionary.size(), 1);
        assertNull(dictionary.put(null, 'a'));
        assertEquals(dictionary.size(), 1);
    }

    @Test
    public void testGetExisting() {
        assertNull(dictionary.put(3, 'a'));
        assertEquals(dictionary.get(3), 'a');
    }

    @Test
    public void testGetNonExisting() {
        assertNull(dictionary.put(3, 'a'));
        assertNull(dictionary.get(2));
    }

    @Test
    public void testGetWithNullKey() {
        assertNull(dictionary.put(null, 'a'));
        assertEquals(dictionary.get(null), 'a');
    }

    @Test
    public void testGetDoesntChangeSize() {
        assertNull(dictionary.put(3, 'a'));
        assertNull(dictionary.put(2, 'b'));
        assertEquals(dictionary.get(3), 'a');
        assertEquals(dictionary.get(2), 'b');
        assertNull(dictionary.get(1));
        assertEquals(dictionary.size(), 2);
    }

    @Test
    public void testContainsKeyExisting() {
        assertNull(dictionary.put(3, 'a'));
        assertTrue(dictionary.containsKey(3));
    }

    @Test
    public void testContainsKeyNonExisting() {
        assertNull(dictionary.put(3, 'a'));
        assertFalse(dictionary.containsKey(2));
    }

    @Test
    public void testContainsKeyWithNullKey() {
        assertNull(dictionary.put(null, 'a'));
        assertTrue(dictionary.containsKey(null));
    }

    @Test
    public void testContainsKeyDoesntChangeSize() {
        assertNull(dictionary.put(3, 'a'));
        assertNull(dictionary.put(2, 'b'));
        assertTrue(dictionary.containsKey(3));
        assertTrue(dictionary.containsKey(2));
        assertFalse(dictionary.containsKey(1));
        assertEquals(dictionary.size(), 2);
    }

    @Test
    public void testContainsValueExisting() {
        assertNull(dictionary.put(3, 'a'));
        assertTrue(dictionary.containsValue('a'));
    }

    @Test
    public void testContainsValueNonExisting() {
        assertNull(dictionary.put(3, 'a'));
        assertFalse(dictionary.containsValue('b'));
    }

    @Test
    public void testContainsValueWithNullValue() {
        assertNull(dictionary.put(3, null));
        assertTrue(dictionary.containsValue(null));
    }

    @Test
    public void testContainsValueDoesntChangeSize() {
        assertNull(dictionary.put(3, 'a'));
        assertNull(dictionary.put(2, 'b'));
        assertTrue(dictionary.containsValue('b'));
        assertTrue(dictionary.containsValue('a'));
        assertFalse(dictionary.containsValue('c'));
        assertEquals(dictionary.size(), 2);
    }

    @Test
    public void testRemoveOneToEmpty() {
        assertNull(dictionary.put(3, 'a'));
        assertEquals(dictionary.remove(3), 'a');
        assertEquals(dictionary.size(), 0);
        assertTrue(dictionary.isEmpty());
    }

    @Test
    public void testRemoveNonExisting() {
        assertNull(dictionary.remove(3));
        assertNull(dictionary.put(3, 'a'));
        assertNull(dictionary.remove(2));
        assertEquals(dictionary.size(), 1);
    }

    @Test
    public void testRemoveDouble() {
        assertNull(dictionary.put(3, 'a'));
        assertEquals(dictionary.remove(3), 'a');
        assertNull(dictionary.remove(3));
        assertEquals(dictionary.size(), 0);
    }

    @Test
    public void testRemoveAndPutRepeatedly() {
        assertNull(dictionary.put(3, 'a'));
        assertEquals(dictionary.remove(3), 'a');
        assertNull(dictionary.put(3, 'a'));
        assertEquals(dictionary.remove(3), 'a');
        assertNull(dictionary.put(3, 'a'));
        assertEquals(dictionary.remove(3), 'a');
        assertEquals(dictionary.size(), 0);
    }

    @Test
    public void testRemoveNull() {
        assertNull(dictionary.put(null, 'a'));
        assertEquals(dictionary.remove(null), 'a');
    }

    @Test
    public void testPutAll() {
        assertNull(dictionary.put(3, 'c'));
        assertNull(dictionary.put(2, 'b'));
        assertNull(dictionary.put(1, 'a'));
        HashMap<Integer, Character> mapToMerge = new HashMap<>();
        mapToMerge.put(228, 'u');
        mapToMerge.put(1, 'z');
        dictionary.putAll(mapToMerge);
        assertEquals(dictionary.size(), 4);
        assertEquals(dictionary.get(1), 'z');
    }

    @Test
    public void testCreateKeySet() {
        assertNull(dictionary.put(3, 'c'));
        assertNull(dictionary.put(2, 'b'));
        assertNull(dictionary.put(1, 'a'));
        Set<Integer> set = dictionary.keySet();
        assertEquals(set.size(), 3);
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
        assertFalse(set.contains(4));
    }

    @Test
    public void testIterateOverKeySet() {
        assertNull(dictionary.put(3, 'c'));
        assertNull(dictionary.put(2, 'b'));
        assertNull(dictionary.put(1, 'a'));
        Set<Integer> set = dictionary.keySet();
        Iterator<Integer> i = set.iterator();
        assertTrue(i.hasNext());
        i.next();
        assertTrue(i.hasNext());
        i.next();
        assertTrue(i.hasNext());
        i.next();
        assertFalse(i.hasNext());
    }

    @Test
    public void testRemoveFromKeySetThroughIterator() {
        assertNull(dictionary.put(3, 'c'));
        assertNull(dictionary.put(2, 'b'));
        assertNull(dictionary.put(1, 'a'));
        Set<Integer> set = dictionary.keySet();
        Iterator<Integer> i = set.iterator();
        i.next();
        i.remove();
        assertEquals(set.size(), 2);
        assertEquals(dictionary.size(), 2);
    }

    @Test
    public void testRemoveAndAddFromKeySetThroughMethod() {
        assertNull(dictionary.put(3, 'c'));
        assertNull(dictionary.put(2, 'b'));
        assertNull(dictionary.put(1, 'a'));
        Set<Integer> set = dictionary.keySet();
        assertThrows(UnsupportedOperationException.class, () -> set.remove(3));
        assertEquals(set.size(), 3);
        assertEquals(dictionary.size(), 3);
        assertThrows(UnsupportedOperationException.class, () -> set.remove(4));
        assertEquals(set.size(), 3);
        assertEquals(dictionary.size(), 3);
    }

    @Test
    public void testCreateEntrySet() {
        assertNull(dictionary.put(3, 'c'));
        assertNull(dictionary.put(2, 'b'));
        assertNull(dictionary.put(1, 'a'));
        Set<Map.Entry<Integer, Character>> set = dictionary.entrySet();
        assertEquals(set.size(), 3);
        assertTrue(set.contains(new Cell<>(3, 'c')));
        assertTrue(set.contains(new Cell<>(2, 'b')));
        assertFalse(set.contains(new Cell<>(1, 'c')));
        assertTrue(set.contains(new Cell<>(1, 'a')));
    }

    @Test
    public void testIterateOverEntrySet() {
        assertNull(dictionary.put(3, 'c'));
        assertNull(dictionary.put(2, 'b'));
        assertNull(dictionary.put(1, 'a'));
        Set<Map.Entry<Integer, Character>> set = dictionary.entrySet();
        Iterator<Map.Entry<Integer, Character>> i = set.iterator();
        assertTrue(i.hasNext());
        i.next();
        assertTrue(i.hasNext());
        i.next();
        assertTrue(i.hasNext());
        i.next();
        assertFalse(i.hasNext());
    }

    @Test
    public void testRemoveFromEntrySetThroughIterator() {
        assertNull(dictionary.put(3, 'c'));
        assertNull(dictionary.put(2, 'b'));
        assertNull(dictionary.put(1, 'a'));
        Set<Map.Entry<Integer, Character>> set = dictionary.entrySet();
        Iterator<Map.Entry<Integer, Character>> i = set.iterator();
        i.next();
        i.remove();
        assertEquals(set.size(), 2);
        assertEquals(dictionary.size(), 2);
    }

    @Test
    public void testRemoveAndAddFromEntrySetThroughMethod() {
        assertNull(dictionary.put(3, 'c'));
        assertNull(dictionary.put(2, 'b'));
        assertNull(dictionary.put(1, 'a'));
        Set<Map.Entry<Integer, Character>> set = dictionary.entrySet();
        assertThrows(UnsupportedOperationException.class, () -> set.remove(new Cell<>(3, 'c')));
        assertEquals(set.size(), 3);
        assertEquals(dictionary.size(), 3);
        assertThrows(UnsupportedOperationException.class, () -> set.add(new Cell<>(228, 'f')));
        assertEquals(set.size(), 3);
        assertEquals(dictionary.size(), 3);
    }

    @Test
    public void testCreateValues() {
        assertNull(dictionary.put(3, 'c'));
        assertNull(dictionary.put(2, 'b'));
        assertNull(dictionary.put(1, 'a'));
        Collection<Character> values = dictionary.values();
        assertEquals(values.size(), 3);
        assertTrue(values.contains('a'));
        assertTrue(values.contains('b'));
        assertTrue(values.contains('c'));
        assertFalse(values.contains('d'));
    }

    @Test
    public void testIterateOverValues() {
        assertNull(dictionary.put(3, 'c'));
        assertNull(dictionary.put(2, 'b'));
        assertNull(dictionary.put(1, 'a'));
        Collection<Character> values = dictionary.values();
        Iterator<Character> i = values.iterator();
        assertTrue(i.hasNext());
        i.next();
        assertTrue(i.hasNext());
        i.next();
        assertTrue(i.hasNext());
        i.next();
        assertFalse(i.hasNext());
    }

    @Test
    public void testRemoveFromValuesThroughIterator() {
        assertNull(dictionary.put(3, 'c'));
        assertNull(dictionary.put(2, 'b'));
        assertNull(dictionary.put(1, 'a'));
        assertNull(dictionary.put(4, 'a'));
        Collection<Character> values = dictionary.values();
        Iterator<Character> i = values.iterator();
        i.next();
        i.remove();
        i.next();
        i.remove();
        assertEquals(values.size(), 2);
        assertEquals(dictionary.size(), 2);
    }

    @Test
    public void testRemoveAndAddFromValuesThroughMethod() {
        assertNull(dictionary.put(3, 'c'));
        assertNull(dictionary.put(2, 'b'));
        assertNull(dictionary.put(1, 'a'));
        assertNull(dictionary.put(4, 'a'));
        Collection<Character> values = dictionary.values();
        assertThrows(UnsupportedOperationException.class, () -> values.remove('d'));
        assertThrows(UnsupportedOperationException.class, () -> values.add('g'));
    }

    @Test
    public void testStressPut() {
        generateStressData(STRESS_TEST_SIZE);
        for (Cell<Integer, Character> elem : stressData) {
            assertEquals(dictionary.put(elem.getKey(), elem.getValue()),
                    hashMap.put(elem.getKey(), elem.getValue()));
            assertEquals(dictionary.size(), hashMap.size());
        }
    }

    @Test
    public void testStressPutAndGet() {
        generateStressData(STRESS_TEST_SIZE);
        for (Cell<Integer, Character> elem : stressData) {
            assertEquals(dictionary.put(elem.getKey(), elem.getValue()),
                    hashMap.put(elem.getKey(), elem.getValue()));
            assertEquals(dictionary.size(), hashMap.size());
        }
        generateStressData(STRESS_TEST_SIZE * 2);
        for (Cell<Integer, Character> elem : stressData) {
            assertEquals(dictionary.get(elem.getKey()),
                    hashMap.get(elem.getKey()));
            assertEquals(dictionary.size(), hashMap.size());
        }
    }

    @Test
    public void testStressPutAndRemove() {
        generateStressData(STRESS_TEST_SIZE);
        for (Cell<Integer, Character> elem : stressData) {
            assertEquals(dictionary.put(elem.getKey(), elem.getValue()),
                    hashMap.put(elem.getKey(), elem.getValue()));
            assertEquals(dictionary.size(), hashMap.size());
        }
        generateStressData(STRESS_TEST_SIZE);
        for (Cell<Integer, Character> elem : stressData) {
            assertEquals(dictionary.remove(elem.getKey()),
                    hashMap.remove(elem.getKey()));
            assertEquals(dictionary.size(), hashMap.size());
        }
    }

    @Test
    public void testStressPutAndClear() {
        generateStressData(STRESS_TEST_SIZE);
        for (int i = 0; i < STRESS_TEST_SIZE; i++) {
            Cell<Integer, Character> elem = stressData.get(i);
            assertEquals(dictionary.put(elem.getKey(), elem.getValue()),
                    hashMap.put(elem.getKey(), elem.getValue()));
            assertEquals(dictionary.size(), hashMap.size());
            if (i % (STRESS_TEST_SIZE / 100) == 0) {
                dictionary.clear();
                hashMap.clear();
            }
        }
    }
}
