package uk.ac.dundee.ac41004.team9.util;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import lombok.experimental.UtilityClass;

/**
 * Misc utilities for manipulating collection types.
 */
@UtilityClass
public class CollectionUtils {

    /**
     * Creates a map of A to B from an assembly of objects.
     *
     * @param a The first key.
     * @param b The first value.
     * @param objs A series of A/B key-value pairs.
     * @param <A> Key type.
     * @param <B> Value type.
     * @return A map of values.
     */
    @SuppressWarnings("unchecked")
    public static <A, B> Map<A, B> mapOf(A a, B b, Object... objs) {
        Map<A, B> map = new HashMap<>();
        map.put(a, b);
        int i = 0;
        while (i < objs.length - 1) {
            map.put((A)objs[i], (B)objs[i + 1]);
            i += 2;
        }
        return map;
    }

    /**
     * Immutable variant of @link{mapOf}.
     */
    public static <A, B> ImmutableMap<A, B> immutableMapOf(A a, B b, Object... objs) {
        return ImmutableMap.copyOf(mapOf(a, b, objs));
    }

}
