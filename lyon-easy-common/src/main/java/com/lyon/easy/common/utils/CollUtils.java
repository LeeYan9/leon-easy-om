package com.lyon.easy.common.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
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

    public static <T,K,V>  Map<K,V> convertMap(Collection<T> from , Function<T,K> keyFunc ,
                                               Function<T,V> valueFunc) {
        return convertMap(from,keyFunc,valueFunc,(v, v2) -> v,HashMap::new);
    }

    public static <T,K,V> Map<K,V> convertMap(Collection<T> from ,
                                              Function<T,K> keyFunc ,
                                              Function<T,V> valFunc ,
                                              BinaryOperator<V> mergeFunc , Supplier<? extends Map<K,V>> supplier){
        if (isEmpty(from)) {
            return supplier.get();
        }
        return from.stream().collect(Collectors.toMap(keyFunc,valFunc,mergeFunc,supplier));
    }

    public static <E> boolean isEmpty(Collection<E> coll) {
        return coll == null || coll.isEmpty();
    }


}
