package uk.ac.dundee.ac41004.team9.util;

import lombok.Data;
import lombok.Getter;

@Data
public class Pair<A, B> {

    @Getter public final A first;
    @Getter public final B second;

}
