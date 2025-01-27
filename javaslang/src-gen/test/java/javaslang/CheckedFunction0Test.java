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

public class CheckedFunction0Test {

    @Test
    public void shouldCreateFromMethodReference() {
        class Type {
            Object methodReference() {
                return null;
            }
        }
        final Type type = new Type();
        assertThat(CheckedFunction0.of(type::methodReference)).isNotNull();
    }

    @Test
    public void shouldLiftPartialFunction() {
        assertThat(CheckedFunction0.lift(() -> { while(true); })).isNotNull();
    }

    @Test
    public void shouldGetArity() {
        final CheckedFunction0<Object> f = () -> null;
        assertThat(f.arity()).isEqualTo(0);
    }

    @Test
    public void shouldCurry() {
        final CheckedFunction0<Object> f = () -> null;
        final CheckedFunction0<Object> curried = f.curried();
        assertThat(curried).isNotNull();
    }

    @Test
    public void shouldTuple() {
        final CheckedFunction0<Object> f = () -> null;
        final CheckedFunction1<Tuple0, Object> tupled = f.tupled();
        assertThat(tupled).isNotNull();
    }

    @Test
    public void shouldReverse() {
        final CheckedFunction0<Object> f = () -> null;
        assertThat(f.reversed()).isNotNull();
    }

    @Test
    public void shouldMemoize() throws Throwable {
        final AtomicInteger integer = new AtomicInteger();
        final CheckedFunction0<Integer> f = () -> integer.getAndIncrement();
        final CheckedFunction0<Integer> memo = f.memoized();
        // should apply f on first apply()
        final int expected = memo.apply();
        // should return memoized value of second apply()
        assertThat(memo.apply()).isEqualTo(expected);

    }

    @Test
    public void shouldNotMemoizeAlreadyMemoizedFunction() throws Throwable {
        final CheckedFunction0<Integer> f = () -> null;
        final CheckedFunction0<Integer> memo = f.memoized();
        assertThat(memo.memoized() == memo).isTrue();
    }

    @Test
    public void shouldRecognizeMemoizedFunctions() {
        final CheckedFunction0<Integer> f = () -> null;
        final CheckedFunction0<Integer> memo = f.memoized();
        assertThat(f.isMemoized()).isFalse();
        assertThat(memo.isMemoized()).isTrue();
    }

    private static final CheckedFunction0<Integer> recurrent1 = () -> 11;

    @Test
    public void shouldCalculatedRecursively() throws Throwable {
        assertThat(recurrent1.apply()).isEqualTo(11);

    }

    @Test
    public void shouldComposeWithAndThen() {
        final CheckedFunction0<Object> f = () -> null;
        final CheckedFunction1<Object, Object> after = o -> null;
        final CheckedFunction0<Object> composed = f.andThen(after);
        assertThat(composed).isNotNull();
    }

}