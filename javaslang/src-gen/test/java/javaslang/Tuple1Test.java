/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2017 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package javaslang;

/*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*\
   G E N E R A T O R   C R A F T E D
\*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.Objects;
import javaslang.collection.List;
import javaslang.collection.Seq;
import org.junit.Test;

public class Tuple1Test {

    @Test
    public void shouldCreateTuple() {
        final Tuple1<Object> tuple = createTuple();
        assertThat(tuple).isNotNull();
    }

    @Test
    public void shouldGetArity() {
        final Tuple1<Object> tuple = createTuple();
        assertThat(tuple.arity()).isEqualTo(1);
    }

    @Test
    public void shouldReturnElements() {
        final Tuple1<Integer> tuple = createIntTuple(1);
        assertThat(tuple._1).isEqualTo(1);
    }

    @Test
    public void shouldConvertToSeq() {
        final Seq<?> actual = createIntTuple(1).toSeq();
        assertThat(actual).isEqualTo(List.of(1));
    }

    @Test
    public void shouldCompareEqual() {
        final Tuple1<Integer> t0 = createIntTuple(0);
        assertThat(t0.compareTo(t0)).isZero();
        assertThat(intTupleComparator.compare(t0, t0)).isZero();
    }

    @Test
    public void shouldCompare1stArg() {
        final Tuple1<Integer> t0 = createIntTuple(0);
        final Tuple1<Integer> t1 = createIntTuple(1);
        assertThat(t0.compareTo(t1)).isNegative();
        assertThat(t1.compareTo(t0)).isPositive();
        assertThat(intTupleComparator.compare(t0, t1)).isNegative();
        assertThat(intTupleComparator.compare(t1, t0)).isPositive();
    }

    @Test
    public void shouldMap() {
        final Tuple1<Object> tuple = createTuple();
        final Tuple1<Object> actual = tuple.map(o -> o);
        assertThat(actual).isEqualTo(tuple);
    }

    @Test
    public void shouldMapComponents() {
      final Tuple1<Object> tuple = createTuple();
      final Function1<Object, Object> f1 = Function1.identity();
      final Tuple1<Object> actual = tuple.map(f1);
      assertThat(actual).isEqualTo(tuple);
    }

    @Test
    public void shouldTransformTuple() {
        final Tuple1<Object> tuple = createTuple();
        final Tuple0 actual = tuple.transform(o1 -> Tuple0.instance());
        assertThat(actual).isEqualTo(Tuple0.instance());
    }

    @Test
    public void shouldRecognizeEquality() {
        final Tuple1<Object> tuple1 = createTuple();
        final Tuple1<Object> tuple2 = createTuple();
        assertThat((Object) tuple1).isEqualTo(tuple2);
    }

    @Test
    public void shouldRecognizeNonEquality() {
        final Tuple1<Object> tuple = createTuple();
        final Object other = new Object();
        assertThat(tuple).isNotEqualTo(other);
    }

    @Test
    public void shouldRecognizeNonEqualityPerComponent() {
        final Tuple1<String> tuple = Tuple.of("1");
        assertThat(tuple.equals(Tuple.of("X"))).isFalse();
    }

    @Test
    public void shouldComputeCorrectHashCode() {
        final int actual = createTuple().hashCode();
        final int expected = Objects.hash(new Object[] { null });
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldImplementToString() {
        final String actual = createTuple().toString();
        final String expected = "(null)";
        assertThat(actual).isEqualTo(expected);
    }

    private Comparator<Tuple1<Integer>> intTupleComparator = Tuple1.comparator(Integer::compare);

    private Tuple1<Object> createTuple() {
        return new Tuple1<>(null);
    }

    private Tuple1<Integer> createIntTuple(Integer i1) {
        return new Tuple1<>(i1);
    }
}