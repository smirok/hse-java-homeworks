package ru.hse.java.collections;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


public class CollectionsTest {
    private static final int DATA_SIZE = 1000;
    private final ArrayList<Integer> data;

    {
        Random random = new Random();
        data = new ArrayList<>(DATA_SIZE);
        for (int i = 0; i < DATA_SIZE; i++) {
            data.add(random.nextInt(10000));
        }
    }

    @Test
    public void testMap() {
        List<Integer> customMappedList = Collections.map(arg -> arg + 3, data);
        List<Integer> streamMappedList = data.stream().map(arg -> arg + 3).collect(Collectors.toList());
        assertEquals(customMappedList, streamMappedList);
    }

    @Test
    public void testStressMap() {
        Random random = new Random();
        for (int i = 0; i < DATA_SIZE; i++) {
            int a = random.nextInt(1000);
            int b = random.nextInt(1000);
            List<Integer> customMappedList = Collections.map(arg -> arg * a + b, data);
            List<Integer> streamMappedList = data.stream().map(arg -> arg * a + b).collect(Collectors.toList());
            assertEquals(customMappedList, streamMappedList);
        }
    }

    @Test
    public void testFilter() {
        List<Integer> customFilteredList = Collections.filter(arg -> arg < 2000, data);
        List<Integer> streamFilteredList = data.stream().filter(arg -> arg < 2000).collect(Collectors.toList());
        assertEquals(customFilteredList, streamFilteredList);
    }

    @Test
    public void testStressFilter() {
        Random random = new Random();
        for (int i = 0; i < DATA_SIZE; i++) {
            int limit = random.nextInt(10000);
            List<Integer> customMappedList = Collections.filter(arg -> arg < limit, data);
            List<Integer> streamMappedList = data.stream().filter(arg -> arg < limit).collect(Collectors.toList());
            assertEquals(customMappedList, streamMappedList);
        }
    }

    @Test
    public void testTakeWhile() {
        List<Integer> customTakeWhileList = Collections.takeWhile(arg -> arg < 9000, data);
        List<Integer> streamTakeWhileList = data.stream().takeWhile(arg -> arg < 9000).collect(Collectors.toList());
        assertEquals(customTakeWhileList, streamTakeWhileList);
    }

    @Test
    public void testStressTakeWhile() {
        Random random = new Random();
        for (int i = 0; i < DATA_SIZE; i++) {
            int limit = random.nextInt(10000);
            List<Integer> customMappedList = Collections.takeWhile(arg -> arg < limit, data);
            List<Integer> streamMappedList = data.stream().takeWhile(arg -> arg < limit).collect(Collectors.toList());
            assertEquals(customMappedList, streamMappedList);
        }
    }

    @Test
    public void testTakeUnless() {
        List<Integer> customTakeUnlessList = Collections.takeUnless(arg -> arg < 1000, data);
        List<Integer> streamTakeUnlessList = data.stream().takeWhile(arg -> !(arg < 1000)).collect(Collectors.toList());
        assertEquals(customTakeUnlessList, streamTakeUnlessList);
    }

    @Test
    public void testStressTakeUnless() {
        Random random = new Random();
        for (int i = 0; i < DATA_SIZE; i++) {
            int limit = random.nextInt(10000);
            List<Integer> customMappedList = Collections.takeUnless(arg -> arg < limit, data);
            List<Integer> streamMappedList = data.stream().takeWhile(arg -> !(arg < limit)).collect(Collectors.toList());
            assertEquals(customMappedList, streamMappedList);
        }
    }

    @Test
    public void testFoldlSum() {
        Integer foldlResult = Collections.foldl(Integer::sum, 0, data);
        AtomicReference<Integer> tempSum = new AtomicReference<>(0);
        data.forEach(arg -> tempSum.updateAndGet(v -> v + arg));
        assertEquals(foldlResult, tempSum.get());
    }

    @Test
    public void testFoldlMinus() {
        Integer foldlResult = Collections.foldl((arg1, arg2) -> arg1 - arg2, 0, data);
        AtomicReference<Integer> tempFoldWithMinus = new AtomicReference<>(0);
        data.forEach(arg -> tempFoldWithMinus.updateAndGet(v -> v - arg));
        assertEquals(foldlResult, tempFoldWithMinus.get());
    }

    @Test
    public void testFoldlEmpty() {
        Integer foldlResult = Collections.foldl((arg1, arg2) -> arg1 - arg2, 0, new ArrayList<Integer>());
        assertEquals(foldlResult, 0);
    }

    @Test
    public void testFoldrSum() {
        Integer foldrResult = Collections.foldr(Integer::sum, 0, data);
        AtomicReference<Integer> tempSum = new AtomicReference<>(0);
        data.forEach(arg -> tempSum.updateAndGet(v -> v + arg));
        assertEquals(foldrResult, tempSum.get());
    }

    @Test
    public void testFoldrMinus() {
        Integer foldrResult = Collections.foldr((arg1, arg2) -> arg1 - arg2, 0, data);
        int tempFoldWithMinus = 0;
        for (int i = DATA_SIZE - 1; i >= 0; i--) {
            tempFoldWithMinus = data.get(i) - tempFoldWithMinus;
        }
        assertEquals(foldrResult, tempFoldWithMinus);
    }

    @Test
    public void testFoldrEmpty() {
        Integer foldrResult = Collections.foldr((arg1, arg2) -> arg1 - arg2, 0, new ArrayList<Integer>());
        assertEquals(foldrResult, 0);
    }

    @Test
    public void testSumEquality() {
        assertEquals(Collections.foldr(Integer::sum, 0, data), Collections.foldl(Integer::sum, 0, data));
    }
}
