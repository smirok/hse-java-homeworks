package ru.hse.java.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class DictionaryImpl<K, V> implements Dictionary<K, V> {
    private static final int DEFAULT_CAPACITY = 20;
    private static final double DEFAULT_UP_FACTOR = 0.666;
    private static final double DEFAULT_DOWN_FACTOR = 0.166;

    private final double upFactor;
    private final double downFactor;
    private int size = 0;
    private int capacity;
    private ArrayList<Cell<K, V>> table;

    public DictionaryImpl() {
        this(DEFAULT_CAPACITY, DEFAULT_UP_FACTOR, DEFAULT_DOWN_FACTOR);
    }

    public DictionaryImpl(int capacity, double upFactor, double downFactor) {
        this.capacity = capacity;
        this.upFactor = upFactor;
        this.downFactor = downFactor;
        table = new ArrayList<>(Collections.nCopies(capacity, new Cell<>()));
    }

    private void resize(int capacity) {
        this.capacity = capacity;
        size = 0;
        ArrayList<Cell<K, V>> oldTable = table;

        table = new ArrayList<>(Collections.nCopies(capacity, new Cell<>()));

        for (Cell<K, V> element : oldTable) {
            if (element.isExists()) {
                put(element.getKey(), element.getValue());
            }
        }
    }

    private int getHash(Object key) {
        return key == null ? 0 : ((key.hashCode() % capacity) + capacity) % capacity;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        int hash = getHash(key);
        for (int i = hash; i < capacity + hash; i++) {
            int index = i < capacity ? i : i - capacity;
            if (table.get(index).isEmpty()) {
                return false;
            }
            if (Objects.equals(table.get(index).getKey(), key)
                    && !table.get(index).isDeleted()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object o) {
        for (int i = 0; i < capacity; i++) {
            if (table.get(i).isExists() &&
                    Objects.equals(table.get(i).getValue(), o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        int hash = getHash(key);
        for (int i = hash; i < capacity + hash; i++) {
            int index = i < capacity ? i : i - capacity;
            if (table.get(index).isEmpty()) {
                return null;
            }
            if (Objects.equals(table.get(index).getKey(), key)
                    && !table.get(index).isDeleted()) {
                return table.get(index).getValue();
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        if (size > upFactor * capacity) {
            resize(capacity * 2);
        }
        int hash = getHash(key);
        for (int i = hash; i < capacity + hash; i++) {
            int index = i < capacity ? i : i - capacity;
            if (table.get(index).isExists() &&
                    !Objects.equals(table.get(index).getKey(), key)) {
                continue;
            }

            if (!table.get(index).isExists()) {
                size++;
            }

            Cell<K, V> oldCell = table.get(index);
            table.set(index, new Cell<>(key, value));
            return oldCell == null || oldCell.isDeleted()
                    ? null : oldCell.getValue();
        }
        return null;
    }

    @Override
    public V remove(Object key) {
        if (size < downFactor * capacity) {
            resize(capacity / 2);
        }
        int hash = getHash(key);
        for (int i = hash; i < capacity + hash; i++) {
            int index = i < capacity ? i : i - capacity;
            if (!table.get(index).isExists()) {
                return null;
            }
            if (Objects.equals(table.get(index).getKey(), key)) {
                size--;
                table.get(index).setStateDeleted();
                return table.get(index).getValue();
            }
        }
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        size = 0;
        capacity = DEFAULT_CAPACITY;
        table = new ArrayList<>(Collections.nCopies(capacity, new Cell<>()));
    }

    private class ViewIterator implements Iterator<Map.Entry<K, V>> {
        private int pos = 0;

        @Override
        public boolean hasNext() {
            for (int i = pos; i < capacity; i++) {
                if (table.get(i).isExists()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Map.Entry<K, V> next() {
            for (; pos < capacity; pos++) {
                if (table.get(pos).isExists()) {
                    pos++;
                    return table.get(pos - 1);
                }
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            table.get(pos).setStateDeleted();
            size--;
        }
    }

    private class KeySet extends AbstractImmutableCollection<K> implements Set<K> {

        @NotNull
        @Override
        public Iterator<K> iterator() {
            return IteratorFactory.create(new ViewIterator(), Entry::getKey);
        }

        @Override
        public int size() {
            return DictionaryImpl.this.size();
        }
    }

    @Override
    public @NotNull Set<K> keySet() {
        return new KeySet();
    }

    private class Values extends AbstractImmutableCollection<V> {

        @NotNull
        @Override
        public Iterator<V> iterator() {
            return IteratorFactory.create(new ViewIterator(), Entry::getValue);
        }

        @Override
        public int size() {
            return DictionaryImpl.this.size();
        }
    }

    @Override
    public @NotNull Collection<V> values() {
        return new Values();
    }

    private class EntrySet extends AbstractImmutableCollection<Entry<K, V>> implements Set<Entry<K, V>> {

        @NotNull
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return IteratorFactory.create(new ViewIterator(), Function.identity());
        }

        @Override
        public int size() {
            return DictionaryImpl.this.size();
        }
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }
}
