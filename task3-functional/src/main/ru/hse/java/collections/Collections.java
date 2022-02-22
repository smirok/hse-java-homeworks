package ru.hse.java.collections;

import org.jetbrains.annotations.NotNull;
import ru.hse.java.function.Function1;
import ru.hse.java.function.Function2;
import ru.hse.java.function.Predicate;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class Collections {
    public static <E, V> List<V> map(@NotNull Function1<? super E, ? extends V> func,
                                     Iterable<? extends E> iterable) {
        LinkedList<V> resultList = new LinkedList<>();
        iterable.forEach(arg -> resultList.add(func.apply(arg)));
        return resultList;
    }

    public static <E> List<E> filter(@NotNull Predicate<? super E> pred,
                                     Iterable<? extends E> iterable) {
        LinkedList<E> resultList = new LinkedList<>();
        iterable.forEach(arg -> {
            if (pred.apply(arg)) {
                resultList.add(arg);
            }
        });
        return resultList;
    }

    public static <E> List<E> takeWhile(@NotNull Predicate<? super E> pred,
                                        Iterable<? extends E> iterable) {
        LinkedList<E> resultList = new LinkedList<>();
        for (E element : iterable) {
            if (!pred.apply(element)) {
                break;
            }

            resultList.add(element);
        }
        return resultList;
    }

    public static <E> List<E> takeUnless(@NotNull Predicate<? super E> pred,
                                         Iterable<? extends E> iterable) {
        return takeWhile(pred.not(), iterable);
    }

    public static <E, T> T foldl(@NotNull Function2<? super T, ? super E, ? extends T> function2,
                                 T initializer,
                                 Iterable<? extends E> iterable) {
        Iterator<? extends E> iterator = iterable.iterator();
        T result = initializer;
        while (iterator.hasNext()) {
            result = function2.apply(result, iterator.next());
        }
        return result;
    }

    public static <E, T> T foldr(@NotNull Function2<? super E, ? super T, ? extends T> function2,
                                 T initializer, Iterable<? extends E> iterable) {
        List<E> list = new LinkedList<>();
        iterable.forEach(list::add);

        ListIterator<? extends E> iterator = list.listIterator(list.size());
        T result = initializer;
        while (iterator.hasPrevious()) {
            result = function2.apply(iterator.previous(), result);
        }
        return result;
    }
}
