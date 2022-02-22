package ru.hse.java.util;

import java.util.Map;
import java.util.Objects;

public class Cell<K, V> implements Map.Entry<K, V> {
    private enum CellState {
        EMPTY,
        DELETED,
        EXIST
    }

    public K key;
    public V value;
    private CellState state;

    public Cell() {
        this.key = null;
        this.value = null;
        this.state = CellState.EMPTY;
    }

    public Cell(K key, V value) {
        this.key = key;
        this.value = value;
        this.state = CellState.EXIST;
    }

    public boolean isEmpty() {
        return this.state == CellState.EMPTY;
    }

    public boolean isExists() {
        return this.state == CellState.EXIST;
    }

    public boolean isDeleted() {
        return this.state == CellState.DELETED;
    }

    public void setStateDeleted() {
        this.state = CellState.DELETED;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V v) {
        V oldValue = value;
        value = v;
        return oldValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell<?, ?> cell = (Cell<?, ?>) o;
        return Objects.equals(key, cell.key) && Objects.equals(value, cell.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
