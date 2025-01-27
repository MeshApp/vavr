/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2017 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package javaslang.control;

import javaslang.AbstractValueTest;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.control.Either.RightProjection;
import org.junit.Test;

import java.util.*;

public class EitherRightProjectionTest extends AbstractValueTest {

    // -- AbstractValueTest

    @Override
    protected <T> RightProjection<?, T> empty() {
        return Either.<T, T> left(null).right();
    }

    @Override
    protected <T> RightProjection<?, T> of(T element) {
        return Either.<T, T> right(element).right();
    }

    @SafeVarargs
    @Override
    protected final <T> RightProjection<?, T> of(T... elements) {
        return of(elements[0]);
    }

    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return true;
    }

    @Override
    protected int getPeekNonNilPerformingAnAction() {
        return 1;
    }

    // -- RightProjection

    // get

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowOnGetOnRightProjectionOfLeft() {
        Either.left(1).right().get();
    }

    @Test
    public void shouldGetOnRightProjectionOfRight() {
        assertThat(Either.right(1).right().get()).isEqualTo(1);
    }

    // orElse

    @Test
    public void shouldRightProjectionOrElseRightProjection() {
        assertThat(Either.right(1).right().orElse(Either.right(2).right()).get()).isEqualTo(1);
        assertThat(Either.<Integer, Integer> left(1).right().orElse(Either.<Integer, Integer> right(2).right()).get()).isEqualTo(2);
    }

    // getOrElse

    @Test
    public void shouldReturnRightWhenOrElseOnRightProjectionOfRight() {
        final Integer actual = Either.<String, Integer> right(1).right().getOrElse(2);
        assertThat(actual).isEqualTo(1);
    }

    @Test
    public void shouldReturnOtherWhenOrElseOnRightProjectionOfLeft() {
        final Integer actual = Either.<String, Integer> left("1").right().getOrElse(2);
        assertThat(actual).isEqualTo(2);
    }

    // getOrElse(Function)

    @Test
    public void shouldReturnRightWhenOrElseGetGivenFunctionOnRightProjectionOfRight() {
        final Integer actual = Either.<String, Integer> right(1).right().getOrElseGet(l -> 2);
        assertThat(actual).isEqualTo(1);
    }

    @Test
    public void shouldReturnOtherWhenOrElseGetGivenFunctionOnRightProjectionOfLeft() {
        final Integer actual = Either.<String, Integer> left("1").right().getOrElseGet(l -> 2);
        assertThat(actual).isEqualTo(2);
    }

    // orElseRun

    @Test
    public void shouldReturnRightWhenOrElseRunOnRightProjectionOfRight() {
        final boolean[] actual = new boolean[] { true };
        Either.<String, Integer> right(1).right().orElseRun(s -> {
            actual[0] = false;
        });
        assertThat(actual[0]).isTrue();
    }

    @Test
    public void shouldReturnOtherWhenOrElseRunOnRightProjectionOfLeft() {
        final boolean[] actual = new boolean[] { false };
        Either.<String, Integer> left("1").right().orElseRun(s -> {
            actual[0] = true;
        });
        assertThat(actual[0]).isTrue();
    }

    // getOrElseThrow(Function)

    @Test
    public void shouldReturnRightWhenOrElseThrowWithFunctionOnRightProjectionOfRight() {
        final Integer actual = Either.<String, Integer> right(1).right().getOrElseThrow(s -> new RuntimeException(s));
        assertThat(actual).isEqualTo(1);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowWhenOrElseThrowWithFunctionOnRightProjectionOfLeft() {
        Either.<String, Integer> left("1").right().getOrElseThrow(i -> new RuntimeException(String.valueOf(i)));
    }

    // toOption

    @Test
    public void shouldConvertRightProjectionOfLeftToNone() {
        assertThat(Either.left(0).right().toOption()).isEqualTo(Option.none());
    }

    @Test
    public void shouldConvertRightProjectionOfRightToSome() {
        assertThat(Either.<Integer, String> right("1").right().toOption()).isEqualTo(Option.of("1"));
    }

    // toEither

    @Test
    public void shouldConvertRightProjectionOfLeftToEither() {
        final Either<Integer, String> self = Either.left(1);
        assertThat(self.right().toEither()).isEqualTo(self);
    }

    @Test
    public void shouldConvertRightProjectionOfRightToEither() {
        final Either<Integer, String> self = Either.right("1");
        assertThat(self.right().toEither()).isEqualTo(self);
    }

    // toJavaOptional

    @Test
    public void shouldConvertRightProjectionOfLeftToJavaOptional() {
        assertThat(Either.left(0).right().toJavaOptional()).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldConvertRightProjectionOfRightToJavaOptional() {
        assertThat(Either.<Integer, String> right("1").right().toJavaOptional()).isEqualTo(Optional.of("1"));
    }

    // filter

    @Test
    public void shouldFilterSomeOnRightProjectionOfRightIfPredicateMatches() {
        final boolean actual = Either.<String, Integer> right(1).right().filter(i -> true).toOption().isDefined();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldFilterNoneOnRightProjectionOfRightIfPredicateNotMatches() {
        assertThat(Either.<String, Integer> right(1).right().filter(i -> false)).isEqualTo(Option.none());
    }

    @Test
    public void shouldFilterSomeOnRightProjectionOfLeftIfPredicateMatches() {
        final boolean actual = Either.<String, Integer> left("1").right().filter(i -> true).isDefined();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldFilterNoneOnRightProjectionOfLeftIfPredicateNotMatches() {
        final boolean actual = Either.<String, Integer> left("1").right().filter(i -> false).isDefined();
        assertThat(actual).isTrue();
    }

    // flatMap

    @Test
    public void shouldFlatMapOnRightProjectionOfRight() {
        final Either<String, Integer> actual = Either.<String, Integer> right(1).right().flatMap(i -> Either.<String, Integer> right(i + 1).right()).toEither();
        assertThat(actual).isEqualTo(Either.right(2));
    }

    @Test
    public void shouldFlatMapOnRightProjectionOfLeft() {
        final Either<String, Integer> actual = Either.<String, Integer> left("1").right().flatMap(i -> Either.<String, Integer> right(i + 1).right()).toEither();
        assertThat(actual).isEqualTo(Either.left("1"));
    }

    @Test
    public void shouldFlatMapRightProjectionOfLeftOnRightProjectionOfRight() {
        final Either<String, String> good = Either.right("good");
        final Either<String, String> bad = Either.left("bad");
        final RightProjection<String, Tuple2<String, String>> actual = good.right().flatMap(g -> bad.right().map(b -> Tuple.of(g, b)));
        assertThat(actual.toEither()).isEqualTo(Either.left("bad"));
    }

    // -- exists

    @Test
    public void shouldBeAwareOfPropertyThatHoldsExistsOfRightProjectionOfRight() {
        assertThat(Either.right(1).right().exists(i -> i == 1)).isTrue();
    }

    @Test
    public void shouldBeAwareOfPropertyThatNotHoldsExistsOfRightProjectionOfRight() {
        assertThat(Either.right(1).right().exists(i -> i == 2)).isFalse();
    }

    @Test
    public void shouldNotHoldPropertyExistsOfRightProjectionOfLeft() {
        assertThat(Either.right(1).left().exists(e -> true)).isFalse();
    }

    // -- forall

    @Test
    public void shouldBeAwareOfPropertyThatHoldsForAllOfRightProjectionOfRight() {
        assertThat(Either.right(1).right().forAll(i -> i == 1)).isTrue();
    }

    @Test
    public void shouldBeAwareOfPropertyThatNotHoldsForAllOfRightProjectionOfRight() {
        assertThat(Either.right(1).right().forAll(i -> i == 2)).isFalse();
    }

    @Test // a property holds for all elements of no elements
    public void shouldNotHoldPropertyForAllOfRightProjectionOfLeft() {
        assertThat(Either.right(1).left().forAll(e -> true)).isTrue();
    }

    // forEach

    @Test
    public void shouldForEachOnRightProjectionOfRight() {
        final List<Integer> actual = new ArrayList<>();
        Either.<String, Integer> right(1).right().forEach(actual::add);
        assertThat(actual).isEqualTo(Collections.singletonList(1));
    }

    @Test
    public void shouldForEachOnRightProjectionOfLeft() {
        final List<Integer> actual = new ArrayList<>();
        Either.<String, Integer> left("1").right().forEach(actual::add);
        assertThat(actual.isEmpty()).isTrue();
    }

    // peek

    @Test
    public void shouldPeekOnRightProjectionOfRight() {
        final List<Integer> actual = new ArrayList<>();
        final Either<String, Integer> testee = Either.<String, Integer> right(1).right().peek(actual::add).toEither();
        assertThat(actual).isEqualTo(Collections.singletonList(1));
        assertThat(testee).isEqualTo(Either.right(1));
    }

    @Test
    public void shouldPeekOnRightProjectionOfLeft() {
        final List<Integer> actual = new ArrayList<>();
        final Either<String, Integer> testee = Either.<String, Integer> left("1").right().peek(actual::add).toEither();
        assertThat(actual.isEmpty()).isTrue();
        assertThat(testee).isEqualTo(Either.<String, Integer> left("1"));
    }

    // map

    @Test
    public void shouldMapOnRightProjectionOfRight() {
        final Either<String, Integer> actual = Either.<String, Integer> right(1).right().map(i -> i + 1).toEither();
        assertThat(actual).isEqualTo(Either.right(2));
    }

    @Test
    public void shouldMapOnRightProjectionOfLeft() {
        final Either<String, Integer> actual = Either.<String, Integer> left("1").right().map(i -> i + 1).toEither();
        assertThat(actual).isEqualTo(Either.left("1"));
    }

    // iterator

    @Test
    public void shouldReturnIteratorOfRightOfRightProjection() {
        assertThat((Iterator<Integer>) Either.right(1).right().iterator()).isNotNull();
    }

    @Test
    public void shouldReturnIteratorOfLeftOfRightProjection() {
        assertThat((Iterator<Object>) Either.left(1).right().iterator()).isNotNull();
    }

    // equals

    @Test
    public void shouldEqualRightProjectionOfRightIfObjectIsSame() {
        final RightProjection<?, ?> r = Either.right(1).right();
        assertThat(r.equals(r)).isTrue();
    }

    @Test
    public void shouldEqualRightProjectionOfLeftIfObjectIsSame() {
        final RightProjection<?, ?> r = Either.left(1).right();
        assertThat(r.equals(r)).isTrue();
    }

    @Test
    public void shouldNotEqualRightProjectionOfRightIfObjectIsNull() {
        assertThat(Either.right(1).right().equals(null)).isFalse();
    }

    @Test
    public void shouldNotEqualRightProjectionOfLeftIfObjectIsNull() {
        assertThat(Either.left(1).right().equals(null)).isFalse();
    }

    @Test
    public void shouldNotEqualRightProjectionOfRightIfObjectIsOfDifferentType() {
        assertThat(Either.right(1).right().equals(new Object())).isFalse();
    }

    @Test
    public void shouldNotEqualRightProjectionOfLeftIfObjectIsOfDifferentType() {
        assertThat(Either.left(1).right().equals(new Object())).isFalse();
    }

    @Test
    public void shouldEqualRightProjectionOfRight() {
        assertThat(Either.right(1).right()).isEqualTo(Either.right(1).right());
    }

    @Test
    public void shouldEqualRightProjectionOfLeft() {
        assertThat(Either.left(1).right()).isEqualTo(Either.left(1).right());
    }

    // hashCode

    @Test
    public void shouldHashRightProjectionOfRight() {
        assertThat(Either.right(1).right().hashCode()).isEqualTo(Objects.hashCode(Either.right(1)));
    }

    @Test
    public void shouldHashRightProjectionOfLeft() {
        assertThat(Either.left(1).right().hashCode()).isEqualTo(Objects.hashCode(Either.left(1)));
    }

    // toString

    @Test
    public void shouldConvertRightProjectionOfLeftToString() {
        assertThat(Either.left(1).right().toString()).isEqualTo("RightProjection(Left(1))");
    }

    @Test
    public void shouldConvertRightProjectionOfRightToString() {
        assertThat(Either.right(1).right().toString()).isEqualTo("RightProjection(Right(1))");
    }

}
