/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2017 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package javaslang;

import javaslang.collection.*;
import javaslang.collection.HashMap;
import javaslang.collection.HashSet;
import javaslang.collection.Iterator;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Queue;
import javaslang.collection.Set;
import javaslang.collection.Stack;
import javaslang.collection.Vector;
import javaslang.concurrent.Future;
import javaslang.control.Either;
import javaslang.control.Option;
import javaslang.control.Try;
import javaslang.control.Validation;
import org.assertj.core.api.*;
import org.junit.Test;

import java.io.Serializable;
import java.util.*;
import java.util.Collections;
import java.util.function.Supplier;

import static javaslang.API.*;
import static javaslang.Predicates.anyOf;
import static javaslang.Predicates.instanceOf;
import static javaslang.Serializables.deserialize;
import static javaslang.Serializables.serialize;

public abstract class AbstractValueTest {

    protected <T> IterableAssert<T> assertThat(Iterable<T> actual) {
        return new IterableAssert<T>(actual) {
        };
    }

    protected <T> ObjectAssert<T> assertThat(T actual) {
        return new ObjectAssert<T>(actual) {
        };
    }

    protected <T> ObjectArrayAssert<T> assertThat(T[] actual) {
        return new ObjectArrayAssert<T>(actual) {};
    }

    protected BooleanAssert assertThat(Boolean actual) {
        return new BooleanAssert(actual) {};
    }

    protected DoubleAssert assertThat(Double actual) {
        return new DoubleAssert(actual) {
        };
    }

    protected IntegerAssert assertThat(Integer actual) {
        return new IntegerAssert(actual) {
        };
    }

    protected LongAssert assertThat(Long actual) {
        return new LongAssert(actual) {
        };
    }

    protected StringAssert assertThat(String actual) {
        return new StringAssert(actual) {
        };
    }

    abstract protected <T> Value<T> empty();

    abstract protected <T> Value<T> of(T element);

    @SuppressWarnings("unchecked")
    abstract protected <T> Value<T> of(T... elements);

    abstract protected boolean useIsEqualToInsteadOfIsSameAs();

    // returns the peek result of the specific Traversable implementation
    abstract protected int getPeekNonNilPerformingAnAction();

    // -- get()

    @Test(expected = NoSuchElementException.class)
    public void shouldGetEmpty() {
        empty().get();
    }

    @Test
    public void shouldGetNonEmpty() {
        assertThat(of(1).get()).isEqualTo(1);
    }

    // -- getOption()

    @Test
    public void shouldGetOptionEmpty() {
        assertThat(empty().getOption()).isEqualTo(Option.none());
    }

    @Test
    public void shouldGetOptionNonEmpty() {
        assertThat(of(1).getOption()).isEqualTo(Option.of(1));
    }

    // -- getOrElse(T)

    @Test
    public void shouldCalculateGetOrElse() {
        assertThat(empty().getOrElse(1)).isEqualTo(1);
        assertThat(of(1).getOrElse(2)).isEqualTo(1);
    }

    // -- getOrElse(Supplier)

    @Test(expected = NullPointerException.class)
    public void shouldThrowOnGetOrElseWithNullSupplier() {
        final Supplier<?> supplier = null;
        empty().getOrElse(supplier);
    }

    @Test
    public void shouldCalculateGetOrElseSupplier() {
        assertThat(empty().getOrElse(() -> 1)).isEqualTo(1);
        assertThat(of(1).getOrElse(() -> 2)).isEqualTo(1);
    }

    // -- getOrElseThrow

    @Test(expected = ArithmeticException.class)
    public void shouldThrowOnGetOrElseThrowIfEmpty() {
        empty().getOrElseThrow(ArithmeticException::new);
    }

    @Test
    public void shouldNotThrowOnGetOrElseThrowIfNonEmpty() {
        assertThat(of(1).getOrElseThrow(ArithmeticException::new)).isEqualTo(1);
    }

    // -- getOrElseTry

    @Test
    public void shouldReturnUnderlyingValueWhenCallingGetOrElseTryOnNonEmptyValue() {
        assertThat(of(1).getOrElseTry(() -> 2)).isEqualTo(1);
    }

    @Test
    public void shouldReturnAlternateValueWhenCallingGetOrElseTryOnEmptyValue() {
        assertThat(empty().getOrElseTry(() -> 2)).isEqualTo(2);
    }

    @Test(expected = Try.NonFatalException.class)
    public void shouldThrowWhenCallingGetOrElseTryOnEmptyValueAndTryIsAFailure() {
        empty().getOrElseTry(() -> {
            throw new Error();
        });
    }

    // -- forEach

    @Test
    public void shouldPerformsActionOnEachElement() {
        final int[] consumer = new int[1];
        final Value<Integer> value = of(1, 2, 3);
        value.forEach(i -> consumer[0] += i);
        assertThat(consumer[0]).isEqualTo(value.isSingleValued() ? 1 : 6);
    }

    // -- isEmpty

    @Test
    public void shouldCalculateIsEmpty() {
        assertThat(empty().isEmpty()).isTrue();
        assertThat(of(1).isEmpty()).isFalse();
    }

    // -- peek

    @Test
    public void shouldPeekNil() {
        assertThat(empty().peek(t -> {})).isEqualTo(empty());
    }

    @Test
    public void shouldPeekNonNilPerformingNoAction() {
        assertThat(of(1).peek(t -> {})).isEqualTo(of(1));
    }

    @Test
    public void shouldPeekSingleValuePerformingAnAction() {
        final int[] effect = { 0 };
        final Value<Integer> actual = of(1).peek(i -> effect[0] = i);
        assertThat(actual).isEqualTo(of(1));
        assertThat(effect[0]).isEqualTo(1);
    }

    @Test
    public void shouldPeekNonNilPerformingAnAction() {
        final int[] effect = { 0 };
        final Value<Integer> actual = of(1, 2, 3).peek(i -> effect[0] = i);
        assertThat(actual).isEqualTo(of(1, 2, 3)); // traverses all elements in the lazy case
        assertThat(effect[0]).isEqualTo(getPeekNonNilPerformingAnAction());
    }

    // -- Conversions toXxx()

    @Test
    public void shouldConvertToArray() {
        final Value<Integer> value = of(1, 2, 3);
        final Array<Integer> array = value.toArray();
        if (value.isSingleValued()) {
            assertThat(array).isEqualTo(Array.of(1));
        } else {
            assertThat(array).isEqualTo(Array.of(1, 2, 3));
        }
    }

    @Test
    public void shouldConvertToCharSeq() {
        final Value<Integer> value = of(1, 2, 3);
        final CharSeq charSeq = value.toCharSeq();
        final CharSeq expected = CharSeq.of(of(1, 2, 3).iterator().mkString());
        assertThat(charSeq).isEqualTo(expected);
    }

    @Test
    public void shouldConvertToList() {
        final Value<Integer> value = of(1, 2, 3);
        final List<Integer> list = value.toList();
        if (value.isSingleValued()) {
            assertThat(list).isEqualTo(List.of(1));
        } else {
            assertThat(list).isEqualTo(List.of(1, 2, 3));
        }
    }

    @Test
    public void shouldConvertToMap() {
        final Value<Integer> value = of(1, 2, 3);
        final Map<Integer, Integer> map = value.toMap(v -> Tuple.of(v, v));
        if (value.isSingleValued()) {
            assertThat(map).isEqualTo(HashMap.of(1, 1));
        } else {
            assertThat(map).isEqualTo(HashMap.empty().put(1, 1).put(2, 2).put(3, 3));
        }
    }

    @Test
    public void shouldConvertToOption() {
        assertThat(empty().toOption()).isSameAs(Option.none());
        assertThat(of(1).toOption()).isEqualTo(Option.of(1));
    }

    @Test
    public void shouldConvertToQueue() {
        final Value<Integer> value = of(1, 2, 3);
        final Queue<Integer> queue = value.toQueue();
        if (value.isSingleValued()) {
            assertThat(queue).isEqualTo(Queue.of(1));
        } else {
            assertThat(queue).isEqualTo(Queue.of(1, 2, 3));
        }
    }

    @Test
    public void shouldConvertToSet() {
        final Value<Integer> value = of(1, 2, 3);
        final Set<Integer> set = value.toSet();
        if (value.isSingleValued()) {
            assertThat(set).isEqualTo(HashSet.of(1));
        } else {
            assertThat(set).isEqualTo(HashSet.of(1, 2, 3));
        }
    }

    @Test
    public void shouldConvertToStack() {
        final Value<Integer> value = of(1, 2, 3);
        final Stack<Integer> stack = value.toStack();
        if (value.isSingleValued()) {
            assertThat(stack).isEqualTo(List.of(1));
        } else {
            assertThat(stack).isEqualTo(List.of(1, 2, 3));
        }
    }

    @Test
    public void shouldConvertToStream() {
        final Value<Integer> value = of(1, 2, 3);
        final Stream<Integer> stream = value.toStream();
        if (value.isSingleValued()) {
            assertThat(stream).isEqualTo(Stream.of(1));
        } else {
            assertThat(stream).isEqualTo(Stream.of(1, 2, 3));
        }
    }

    @Test
    public void shouldConvertNonEmptyToTry() {
        assertThat(of(1, 2, 3).toTry()).isEqualTo(Try.of(() -> 1));
    }

    @Test
    public void shouldConvertEmptyToTry() {
        final Try<?> actual = empty().toTry();
        assertThat(actual.isFailure()).isTrue();
        assertThat(actual.getCause()).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldConvertNonEmptyToTryUsingExceptionSupplier() {
        final Exception x = new Exception("test");
        assertThat(of(1, 2, 3).toTry(() -> x)).isEqualTo(Try.of(() -> 1));
    }

    @Test
    public void shouldConvertEmptyToTryUsingExceptionSupplier() {
        final Exception x = new Exception("test");
        assertThat(empty().toTry(() -> x)).isEqualTo(Try.failure(x));
    }

    @Test
    public void shouldConvertToVector() {
        final Value<Integer> value = of(1, 2, 3);
        final Vector<Integer> vector = value.toVector();
        if (value.isSingleValued()) {
            assertThat(vector).isEqualTo(Vector.of(1));
        } else {
            assertThat(vector).isEqualTo(Vector.of(1, 2, 3));
        }
    }

    @Test
    public void shouldConvertToJavaArray() {
        final Value<Integer> value = of(1, 2, 3);
        final Integer[] ints = value.toJavaArray(Integer.class);
        if (value.isSingleValued()) {
            assertThat(ints).containsOnly(1);
        } else {
            assertThat(ints).containsOnly(1, 2, 3);
        }
    }

    @Test
    public void shouldConvertToJavaArrayWithTypeHintPrimitiveBoolean() {
        final Value<Boolean> value = of(true, false);
        final Boolean[] array = value.toJavaArray(boolean.class);
        if (value.isSingleValued()) {
            assertThat(array).containsOnly(true);
        } else {
            assertThat(array).containsOnly(true, false);
        }
    }

    @Test
    public void shouldConvertToJavaArrayWithTypeHintPrimitiveByte() {
        final Value<Byte> value = of((byte) 1, (byte) 2);
        final Byte[] array = value.toJavaArray(byte.class);
        if (value.isSingleValued()) {
            assertThat(array).containsOnly((byte) 1);
        } else {
            assertThat(array).containsOnly((byte) 1, (byte) 2);
        }
    }

    @Test
    public void shouldConvertToJavaArrayWithTypeHintPrimitiveChar() {
        final Value<Character> value = of('a', 'b');
        final Character[] array = value.toJavaArray(char.class);
        if (value.isSingleValued()) {
            assertThat(array).containsOnly('a');
        } else {
            assertThat(array).containsOnly('a', 'b');
        }
    }

    @Test
    public void shouldConvertToJavaArrayWithTypeHintPrimitiveDouble() {
        final Value<Double> value = of(.1, .2);
        final Double[] array = value.toJavaArray(double.class);
        if (value.isSingleValued()) {
            assertThat(array).containsOnly(.1);
        } else {
            assertThat(array).containsOnly(.1, .2);
        }
    }

    @Test
    public void shouldConvertToJavaArrayWithTypeHintPrimitiveFloat() {
        final Value<Float> value = of(.1f, .2f);
        final Float[] array = value.toJavaArray(float.class);
        if (value.isSingleValued()) {
            assertThat(array).containsOnly(.1f);
        } else {
            assertThat(array).containsOnly(.1f, .2f);
        }
    }

    @Test
    public void shouldConvertToJavaArrayWithTypeHintPrimitiveInt() {
        final Value<Integer> value = of(1, 2);
        final Integer[] array = value.toJavaArray(int.class);
        if (value.isSingleValued()) {
            assertThat(array).containsOnly(1);
        } else {
            assertThat(array).containsOnly(1, 2);
        }
    }

    @Test
    public void shouldConvertToJavaArrayWithTypeHintPrimitiveLong() {
        final Value<Long> value = of(1L, 2L);
        final Long[] array = value.toJavaArray(long.class);
        if (value.isSingleValued()) {
            assertThat(array).containsOnly(1L);
        } else {
            assertThat(array).containsOnly(1L, 2L);
        }
    }

    @Test
    public void shouldConvertToJavaArrayWithTypeHintPrimitiveShort() {
        final Value<Short> value = of((short) 1, (short) 2);
        final Short[] array = value.toJavaArray(short.class);
        if (value.isSingleValued()) {
            assertThat(array).containsOnly((short) 1);
        } else {
            assertThat(array).containsOnly((short) 1, (short) 2);
        }
    }

    @Test
    public void shouldConvertToJavaArrayWithTypeHintPrimitiveVoid() {
        final Value<Void> value = of((Void) null);
        final Void[] array = value.toJavaArray(void.class);
        assertThat(array).containsOnly((Void) null);
    }

    @Test
    public void shouldConvertToJavaCollectionUsingSupplier() {
        final Value<Integer> value = of(1, 2, 3);
        final java.util.List<Integer> ints = value.toJavaCollection(ArrayList::new);
        if (value.isSingleValued()) {
            assertThat(ints).isEqualTo(Collections.singletonList(1));
        } else {
            assertThat(ints).isEqualTo(Arrays.asList(1, 2, 3));
        }
    }

    @Test
    public void shouldConvertToJavaList() {
        final Value<Integer> value = of(1, 2, 3);
        final java.util.List<Integer> list = value.toJavaList();
        if (value.isSingleValued()) {
            assertThat(list).isEqualTo(Arrays.asList(1));
        } else {
            assertThat(list).isEqualTo(Arrays.asList(1, 2, 3));
        }
    }

    @Test
    public void shouldConvertToJavaListUsingSupplier() {
        final Value<Integer> value = of(1, 2, 3);
        final java.util.List<Integer> ints = value.toJavaList(ArrayList::new);
        if (value.isSingleValued()) {
            assertThat(ints).isEqualTo(Collections.singletonList(1));
        } else {
            assertThat(ints).isEqualTo(Arrays.asList(1, 2, 3));
        }
    }

    @Test
    public void shouldConvertToJavaMapUsingFunction() {
        final Value<Integer> value = of(1, 2, 3);
        final java.util.Map<Integer, Integer> map = value.toJavaMap(v -> Tuple.of(v, v));
        if (value.isSingleValued()) {
            assertThat(map).isEqualTo(JavaCollections.javaMap(1, 1));
        } else {
            assertThat(map).isEqualTo(JavaCollections.javaMap(1, 1, 2, 2, 3, 3));
        }
    }

    @Test
    public void shouldConvertToJavaMapUsingSupplierAndFunction() {
        final Value<Integer> value = of(1, 2, 3);
        final java.util.Map<Integer, Integer> map = value.toJavaMap(java.util.HashMap::new, i -> Tuple.of(i, i));
        if (value.isSingleValued()) {
            assertThat(map).isEqualTo(JavaCollections.javaMap(1, 1));
        } else {
            assertThat(map).isEqualTo(JavaCollections.javaMap(1, 1, 2, 2, 3, 3));
        }
    }

    @Test
    public void shouldConvertToJavaOptional() {
        assertThat(of(1, 2, 3).toJavaOptional()).isEqualTo(Optional.of(1));
    }

    @Test
    public void shouldConvertToJavaSet() {
        final Value<Integer> value = of(1, 2, 3);
        final java.util.Set<Integer> set = value.toJavaSet();
        if (value.isSingleValued()) {
            assertThat(set).isEqualTo(JavaCollections.javaSet(1));
        } else {
            assertThat(set).isEqualTo(JavaCollections.javaSet(1, 2, 3));
        }
    }

    @Test
    public void shouldConvertToJavaSetUsingSupplier() {
        final Value<Integer> value = of(1, 2, 3);
        final java.util.Set<Integer> set = value.toJavaSet(java.util.HashSet::new);
        if (value.isSingleValued()) {
            assertThat(set).isEqualTo(JavaCollections.javaSet(1));
        } else {
            assertThat(set).isEqualTo(JavaCollections.javaSet(1, 2, 3));
        }
    }

    @Test
    public void shouldConvertToJavaStream() {
        final Value<Integer> value = of(1, 2, 3);
        final java.util.stream.Stream<Integer> s1 = value.toJavaStream();
        if (value.isSingleValued()) {
            final java.util.stream.Stream<Integer> s2 = java.util.stream.Stream.of(1);
            assertThat(List.ofAll(s1::iterator)).isEqualTo(List.ofAll(s2::iterator));
        } else {
            final java.util.stream.Stream<Integer> s2 = java.util.stream.Stream.of(1, 2, 3);
            assertThat(List.ofAll(s1::iterator)).isEqualTo(List.ofAll(s2::iterator));
        }
    }

    @Test
    public void shouldConvertToEitherLeftFromValueSupplier() {
        Either<Integer, String> either = of(0).toLeft(() -> "fallback");
        assertThat(either.isLeft()).isTrue();
        assertThat(either.getLeft()).isEqualTo(0);

        Either<Object, String> either2 = empty().toLeft(() -> "fallback");
        assertThat(either2.isRight()).isTrue();
        assertThat(either2.get()).isEqualTo("fallback");
    }

    @Test
    public void shouldConvertToEitherLeftFromValue() {
        Either<Integer, String> either = of(0).toLeft("fallback");
        assertThat(either.isLeft()).isTrue();
        assertThat(either.getLeft()).isEqualTo(0);

        Either<Object, String> either2 = empty().toLeft("fallback");
        assertThat(either2.isRight()).isTrue();
        assertThat(either2.get()).isEqualTo("fallback");
    }

    @Test
    public void shouldConvertToEitherRightFromValueSupplier() {
        Either<String, Integer> either = of(0).toRight(() -> "fallback");
        assertThat(either.isRight()).isTrue();
        assertThat(either.get()).isEqualTo(0);

        Either<String, Object> either2 = empty().toRight(() -> "fallback");
        assertThat(either2.isLeft()).isTrue();
        assertThat(either2.getLeft()).isEqualTo("fallback");
    }

    @Test
    public void shouldConvertToEitherRightFromValue() {
        Either<String, Integer> either = of(0).toRight("fallback");
        assertThat(either.isRight()).isTrue();
        assertThat(either.get()).isEqualTo(0);

        Either<String, Object> either2 = empty().toRight("fallback");
        assertThat(either2.isLeft()).isTrue();
        assertThat(either2.getLeft()).isEqualTo("fallback");
    }

    // -- exists

    @Test
    public void shouldBeAwareOfExistingElement() {
        final Value<Integer> value = of(1, 2);
        if (value.isSingleValued()) {
            assertThat(value.exists(i -> i == 1)).isTrue();
        } else {
            assertThat(value.exists(i -> i == 2)).isTrue();
        }

    }

    @Test
    public void shouldBeAwareOfNonExistingElement() {
        assertThat(this.<Integer> empty().exists(i -> i == 1)).isFalse();
    }

    // -- forAll

    @Test
    public void shouldBeAwareOfPropertyThatHoldsForAll() {
        assertThat(of(2, 4).forAll(i -> i % 2 == 0)).isTrue();
    }

    @Test
    public void shouldBeAwareOfPropertyThatNotHoldsForAll() {
        assertThat(of(1, 2).forAll(i -> i % 2 == 0)).isFalse();
    }

    // ### ValueModule.Iterable ###

    // -- corresponds

    @Test
    public void shouldntCorrespondsNilNil() {
        assertThat(empty().corresponds(empty(), (o1, o2) -> true)).isTrue();
    }

    @Test
    public void shouldntCorrespondsNilNonNil() {
        assertThat(empty().corresponds(of(1), (o1, i2) -> true)).isFalse();
    }

    @Test
    public void shouldntCorrespondsNonNilNil() {
        assertThat(of(1).corresponds(empty(), (i1, o2) -> true)).isFalse();
    }

    @Test
    public void shouldntCorrespondsDifferentLengths() {
        if (!empty().isSingleValued()) {
            assertThat(of(1, 2, 3).corresponds(of(1, 2), (i1, i2) -> true)).isFalse();
            assertThat(of(1, 2).corresponds(of(1, 2, 3), (i1, i2) -> true)).isFalse();
        }
    }

    @Test
    public void shouldCorresponds() {
        assertThat(of(1, 2, 3).corresponds(of(3, 4, 5), (i1, i2) -> i1 == i2 - 2)).isTrue();
        assertThat(of(1, 2, 3).corresponds(of(1, 2, 3), (i1, i2) -> i1 == i2 + 1)).isFalse();
    }

    // -- Serialization

    /**
     * States whether the specific Value implementation is Serializable.
     * <p>
     * Test classes override this method to return false if needed.
     *
     * @return true (by default), if the Value is Serializable, false otherwise
     */
    private boolean isSerializable() {
        if (empty() instanceof Serializable != of(1) instanceof Serializable) {
            throw new Error("empty and non-empty do not consistently implement Serializable");
        }
        final boolean actual = empty() instanceof Serializable;
        final boolean expected = Match(empty()).of(
                Case(anyOf(
                        instanceOf(Either.LeftProjection.class),
                        instanceOf(Either.RightProjection.class),
                        instanceOf(Future.class),
                        instanceOf(Iterator.class)
                ), false),
                Case(anyOf(
                        instanceOf(Either.class),
                        instanceOf(Option.class),
                        instanceOf(Try.class),
                        instanceOf(Traversable.class),
                        instanceOf(Validation.class)
                ), true)
        );
        assertThat(actual).isEqualTo(expected);
        return actual;
    }

    @Test
    public void shouldSerializeDeserializeEmpty() {
        if (isSerializable()) {
            final Value<?> testee = empty();
            final Value<?> actual = deserialize(serialize(testee));
            assertThat(actual).isEqualTo(testee);
        }
    }

    @Test
    public void shouldSerializeDeserializeSingleValued() {
        if (isSerializable()) {
            final Value<?> testee = of(1);
            final Value<?> actual = deserialize(serialize(testee));
            assertThat(actual).isEqualTo(testee);
        }
    }

    @Test
    public void shouldSerializeDeserializeMultiValued() {
        if (isSerializable()) {
            final Value<?> testee = of(1, 2, 3);
            final Value<?> actual = deserialize(serialize(testee));
            assertThat(actual).isEqualTo(testee);
        }
    }

    @Test
    public void shouldPreserveSingletonInstanceOnDeserialization() {
        if (isSerializable() && !useIsEqualToInsteadOfIsSameAs()) {
            final Value<?> empty = empty();
            final Value<?> actual = deserialize(serialize(empty));
            assertThat(actual).isSameAs(empty);
        }
    }

}
