package ru.hse.java.util;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

public class IteratorFactory {
    public static <T,K,V> Iterator<T> create(Iterator<Map.Entry<K,V>> viewIterator, Function<Map.Entry<K, V>, T> convertFunction) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return viewIterator.hasNext();
            }

            @Override
            public T next() {
                return convertFunction.apply(viewIterator.next());
            }

            @Override
            public void remove() {
                viewIterator.remove();
            }
        };
    }
}
