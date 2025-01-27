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

public class Tuple2Test {

    @Test
    public void shouldCreateTuple() {
        final Tuple2<Object, Object> tuple = createTuple();
        assertThat(tuple).isNotNull();
    }

    @Test
    public void shouldGetArity() {
        final Tuple2<Object, Object> tuple = createTuple();
        assertThat(tuple.arity()).isEqualTo(2);
    }

    @Test
    public void shouldReturnElements() {
        final Tuple2<Integer, Integer> tuple = createIntTuple(1, 2);
        assertThat(tuple._1).isEqualTo(1);
        assertThat(tuple._2).isEqualTo(2);
    }

    @Test
    public void shouldConvertToSeq() {
        final Seq<?> actual = createIntTuple(1, 0).toSeq();
        assertThat(actual).isEqualTo(List.of(1, 0));
    }

    @Test
    public void shouldCompareEqual() {
        final Tuple2<Integer, Integer> t0 = createIntTuple(0, 0);
        assertThat(t0.compareTo(t0)).isZero();
        assertThat(intTupleComparator.compare(t0, t0)).isZero();
    }

    @Test
    public void shouldCompare1stArg() {
        final Tuple2<Integer, Integer> t0 = createIntTuple(0, 0);
        final Tuple2<Integer, Integer> t1 = createIntTuple(1, 0);
        assertThat(t0.compareTo(t1)).isNegative();
        assertThat(t1.compareTo(t0)).isPositive();
        assertThat(intTupleComparator.compare(t0, t1)).isNegative();
        assertThat(intTupleComparator.compare(t1, t0)).isPositive();
    }

    @Test
    public void shouldCompare2ndArg() {
        final Tuple2<Integer, Integer> t0 = createIntTuple(0, 0);
        final Tuple2<Integer, Integer> t2 = createIntTuple(0, 1);
        assertThat(t0.compareTo(t2)).isNegative();
        assertThat(t2.compareTo(t0)).isPositive();
        assertThat(intTupleComparator.compare(t0, t2)).isNegative();
        assertThat(intTupleComparator.compare(t2, t0)).isPositive();
    }

    @Test
    public void shouldMap() {
        final Tuple2<Object, Object> tuple = createTuple();
        final Tuple2<Object, Object> actual = tuple.map((o1, o2) -> tuple);
        assertThat(actual).isEqualTo(tuple);
    }

    @Test
    public void shouldMapComponents() {
      final Tuple2<Object, Object> tuple = createTuple();
      final Function1<Object, Object> f1 = Function1.identity();
      final Function1<Object, Object> f2 = Function1.identity();
      final Tuple2<Object, Object> actual = tuple.map(f1, f2);
      assertThat(actual).isEqualTo(tuple);
    }

    @Test
    public void shouldMap1stComponent() {
      final Tuple2<String, Integer> actual = Tuple.of(1, 1).map1(i -> "X");
      final Tuple2<String, Integer> expected = Tuple.of("X", 1);
      assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldMap2ndComponent() {
      final Tuple2<Integer, String> actual = Tuple.of(1, 1).map2(i -> "X");
      final Tuple2<Integer, String> expected = Tuple.of(1, "X");
      assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldTransformTuple() {
        final Tuple2<Object, Object> tuple = createTuple();
        final Tuple0 actual = tuple.transform((o1, o2) -> Tuple0.instance());
        assertThat(actual).isEqualTo(Tuple0.instance());
    }

    @Test
    public void shouldRecognizeEquality() {
        final Tuple2<Object, Object> tuple1 = createTuple();
        final Tuple2<Object, Object> tuple2 = createTuple();
        assertThat((Object) tuple1).isEqualTo(tuple2);
    }

    @Test
    public void shouldRecognizeNonEquality() {
        final Tuple2<Object, Object> tuple = createTuple();
        final Object other = new Object();
        assertThat(tuple).isNotEqualTo(other);
    }

    @Test
    public void shouldRecognizeNonEqualityPerComponent() {
        final Tuple2<String, String> tuple = Tuple.of("1", "2");
        assertThat(tuple.equals(Tuple.of("X", "2"))).isFalse();
        assertThat(tuple.equals(Tuple.of("1", "X"))).isFalse();
    }

    @Test
    public void shouldComputeCorrectHashCode() {
        final int actual = createTuple().hashCode();
        final int expected = Objects.hash(null, null);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldImplementToString() {
        final String actual = createTuple().toString();
        final String expected = "(null, null)";
        assertThat(actual).isEqualTo(expected);
    }

    private Comparator<Tuple2<Integer, Integer>> intTupleComparator = Tuple2.comparator(Integer::compare, Integer::compare);

    private Tuple2<Object, Object> createTuple() {
        return new Tuple2<>(null, null);
    }

    private Tuple2<Integer, Integer> createIntTuple(Integer i1, Integer i2) {
        return new Tuple2<>(i1, i2);
    }
}