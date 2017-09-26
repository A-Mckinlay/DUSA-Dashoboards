package uk.ac.dundee.ac41004.team9.util;

import lombok.Data;
import lombok.Getter;

/**
 * A pair of objects. No, really, that's it.
 *
 * @param <A> Type of 'first' object.
 * @param <B> Type of 'second' object.
 */
@Data
public class Pair<A, B> {

    @Getter public final A first;
    @Getter public final B second;

}
