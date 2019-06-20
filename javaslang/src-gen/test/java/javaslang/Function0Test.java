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

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class Function0Test {

    @Test
    public void shouldCreateFromMethodReference() {
        class Type {
            Object methodReference() {
                return null;
            }
        }
        final Type type = new Type();
        assertThat(Function0.of(type::methodReference)).isNotNull();
    }

    @Test
    public void shouldLiftPartialFunction() {
        assertThat(Function0.lift(() -> { while(true); })).isNotNull();
    }

    @Test
    public void shouldGetValue() {
        final String s = "test";
        final Function0<String> supplier = () -> s;
        assertThat(supplier.get()).isEqualTo(s);
    }

    @Test
    public void shouldGetArity() {
        final Function0<Object> f = () -> null;
        assertThat(f.arity()).isEqualTo(0);
    }

    @Test
    public void shouldCurry() {
        final Function0<Object> f = () -> null;
        final Function0<Object> curried = f.curried();
        assertThat(curried).isNotNull();
    }

    @Test
    public void shouldTuple() {
        final Function0<Object> f = () -> null;
        final Function1<Tuple0, Object> tupled = f.tupled();
        assertThat(tupled).isNotNull();
    }

    @Test
    public void shouldReverse() {
        final Function0<Object> f = () -> null;
        assertThat(f.reversed()).isNotNull();
    }

    @Test
    public void shouldMemoize() {
        final AtomicInteger integer = new AtomicInteger();
        final Function0<Integer> f = () -> integer.getAndIncrement();
        final Function0<Integer> memo = f.memoized();
        // should apply f on first apply()
        final int expected = memo.apply();
        // should return memoized value of second apply()
        assertThat(memo.apply()).isEqualTo(expected);

    }

    @Test
    public void shouldNotMemoizeAlreadyMemoizedFunction() {
        final Function0<Integer> f = () -> null;
        final Function0<Integer> memo = f.memoized();
        assertThat(memo.memoized() == memo).isTrue();
    }

    @Test
    public void shouldRecognizeMemoizedFunctions() {
        final Function0<Integer> f = () -> null;
        final Function0<Integer> memo = f.memoized();
        assertThat(f.isMemoized()).isFalse();
        assertThat(memo.isMemoized()).isTrue();
    }

    private static final Function0<Integer> recurrent1 = () -> 11;

    @Test
    public void shouldCalculatedRecursively() {
        assertThat(recurrent1.apply()).isEqualTo(11);

    }

    @Test
    public void shouldComposeWithAndThen() {
        final Function0<Object> f = () -> null;
        final Function1<Object, Object> after = o -> null;
        final Function0<Object> composed = f.andThen(after);
        assertThat(composed).isNotNull();
    }

}