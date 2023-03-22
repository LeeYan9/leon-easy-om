package com.lyon.easy.common.utils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lyon
 */
public class CollUtils {

    public static <E, K> List<K> toList(Collection<E> coll, Function<E, K> func) {
        return isEmpty(coll) ? null : coll.stream().map(func).collect(Collectors.toList());
    }

    public static <E, K> List<K> toNonEmptyList(Collection<E> coll, Function<E, K> func) {
        assert !isEmpty(coll);
        final List<K> collect = coll.stream().map(func).collect(Collectors.toList());
        assert !isEmpty(collect);
        return collect;
    }

    public static <E, K> List<K> filterToList(Collection<E> coll, Predicate<E> predicate, Function<E, K> func) {
        return isEmpty(coll) ? null : coll.stream().filter(predicate).map(func).collect(Collectors.toList());
    }

    public static <E> E filterFirst(Collection<E> coll, Predicate<E> predicate) {
        return isEmpty(coll) ? null : coll.stream().filter(predicate).findFirst().orElse(null);
    }

    public static <E> boolean isEmpty(Collection<E> coll) {
        return coll == null || coll.isEmpty();
    }


}
