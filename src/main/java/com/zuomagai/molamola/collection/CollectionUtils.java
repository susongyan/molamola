package com.zuomagai.molamola.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CollectionUtils {

    private CollectionUtils() {
        throw new AssertionError("No instances.");
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    public static int size(Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }

    public static boolean contains(Collection<?> collection, Object value) {
        return collection != null && collection.contains(value);
    }

    public static boolean containsAny(Collection<?> collection, Collection<?> candidates) {
        if (isEmpty(collection) || isEmpty(candidates)) {
            return false;
        }
        for (Object candidate : candidates) {
            if (collection.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAll(Collection<?> collection, Collection<?> candidates) {
        if (isEmpty(candidates)) {
            return true;
        }
        if (isEmpty(collection)) {
            return false;
        }
        return collection.containsAll(candidates);
    }

    public static <T> T firstOrNull(Iterable<T> iterable) {
        if (iterable == null) {
            return null;
        }
        Iterator<T> iterator = iterable.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    public static <T> T lastOrNull(List<T> list) {
        return isEmpty(list) ? null : list.get(list.size() - 1);
    }

    public static <T> List<T> emptyIfNull(List<T> list) {
        return list == null ? Collections.<T>emptyList() : list;
    }

    public static <T> Set<T> emptyIfNull(Set<T> set) {
        return set == null ? Collections.<T>emptySet() : set;
    }

    public static <K, V> Map<K, V> emptyIfNull(Map<K, V> map) {
        return map == null ? Collections.<K, V>emptyMap() : map;
    }

    public static <T> int addAll(Collection<T> target, Iterable<? extends T> items) {
        if (target == null || items == null) {
            return 0;
        }
        if (items instanceof Collection) {
            int before = target.size();
            target.addAll((Collection<? extends T>) items);
            return target.size() - before;
        }
        int added = 0;
        for (T item : items) {
            if (target.add(item)) {
                added++;
            }
        }
        return added;
    }
}
