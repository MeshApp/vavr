/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2017 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package javaslang.collection;

import javaslang.Kind2;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.control.Option;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Collector;

import static javaslang.collection.Comparators.naturalComparator;

/**
 * SortedMap implementation, backed by a Red/Black Tree.
 *
 * @param <K> Key type
 * @param <V> Value type
 * @author Daniel Dietrich
 * @since 2.0.0
 */
// DEV-NOTE: use entries.min().get() in favor of iterator().next(), it is faster!
public final class TreeMap<K, V> implements Kind2<TreeMap<?, ?>, K, V>, SortedMap<K, V>, Serializable {

    private static final long serialVersionUID = 1L;

    private final RedBlackTree<Tuple2<K, V>> entries;

    private TreeMap(RedBlackTree<Tuple2<K, V>> entries) {
        this.entries = entries;
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a
     * {@link javaslang.collection.TreeMap}.
     * <p>
     * The natural comparator is used to compare TreeMap keys.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @return A {@link TreeMap} Collector.
     */
    public static <K extends Comparable<? super K>, V> Collector<Tuple2<K, V>, ArrayList<Tuple2<K, V>>, TreeMap<K, V>> collector() {
        return createCollector(EntryComparator.natural());
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a
     * {@link javaslang.collection.TreeMap}.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key.
     * @return A {@link TreeMap} Collector.
     */
    public static <K, V> Collector<Tuple2<K, V>, ArrayList<Tuple2<K, V>>, TreeMap<K, V>> collector(Comparator<? super K> keyComparator) {
        return createCollector(EntryComparator.of(keyComparator));
    }

    /**
     * Returns the empty TreeMap. The underlying key comparator is the natural comparator of K.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @return A new empty TreeMap.
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> empty() {
        return new TreeMap<>(RedBlackTree.empty(EntryComparator.natural()));
    }

    /**
     * Returns the empty TreeMap using the given key comparator.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key.
     * @return A new empty TreeMap.
     */
    public static <K, V> TreeMap<K, V> empty(Comparator<? super K> keyComparator) {
        return new TreeMap<>(RedBlackTree.empty(EntryComparator.of(keyComparator)));
    }

    /**
     * Narrows a widened {@code TreeMap<? extends K, ? extends V>} to {@code TreeMap<K, V>}
     * by performing a type safe-cast. This is eligible because immutable/read-only
     * collections are covariant.
     * <p>
     * CAUTION: If {@code K} is narrowed, the underlying {@code Comparator} might fail!
     *
     * @param treeMap A {@code TreeMap}.
     * @param <K>     Key type
     * @param <V>     Value type
     * @return the given {@code treeMap} instance as narrowed type {@code TreeMap<K, V>}.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> TreeMap<K, V> narrow(TreeMap<? extends K, ? extends V> treeMap) {
        return (TreeMap<K, V>) treeMap;
    }

    /**
     * Returns a singleton {@code TreeMap}, i.e. a {@code TreeMap} of one entry.
     * The underlying key comparator is the natural comparator of K.
     *
     * @param <K>   The key type
     * @param <V>   The value type
     * @param entry A map entry.
     * @return A new TreeMap containing the given entry.
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(Tuple2<? extends K, ? extends V> entry) {
        Objects.requireNonNull(entry, "entry is null");
        return createFromTuple(EntryComparator.natural(), entry);
    }

    /**
     * Returns a singleton {@code TreeMap}, i.e. a {@code TreeMap} of one entry using a specific key comparator.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param entry         A map entry.
     * @return A new TreeMap containing the given entry.
     */
    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, Tuple2<? extends K, ? extends V> entry) {
        Objects.requireNonNull(entry, "entry is null");
        return createFromTuple(EntryComparator.of(keyComparator), entry);
    }

    /**
     * Creates a TreeMap of the given list of key-value pairs.
     *
     * @param pairs A list of key-value pairs
     * @param <K>   The key type
     * @param <V>   The value type
     * @return A new Map containing the given entries
     */
    @SuppressWarnings("unchecked")
    public static <K, V> TreeMap<K, V> of(Object... pairs) {
        Objects.requireNonNull(pairs, "pairs is null");
        if ((pairs.length & 1) != 0) {
            throw new IllegalArgumentException("Odd length of key-value pairs list");
        }
        RedBlackTree<Tuple2<K, V>> result = RedBlackTree.empty(EntryComparator.of(naturalComparator()));
        for (int i = 0; i < pairs.length; i += 2) {
            result = result.insert(Tuple.of((K) pairs[i], (V) pairs[i + 1]));
        }
        return new TreeMap<>(result);
    }

    /**
     * Returns a {@code TreeMap}, from a source java.util.Map.
     *
     * @param map A map entry.
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing the given map
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> ofAll(java.util.Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map is null");
        return createFromMap(EntryComparator.natural(), map);
    }

    /**
     * Returns a singleton {@code TreeMap}, i.e. a {@code TreeMap} of one element.
     *
     * @param key   A singleton map key.
     * @param value A singleton map value.
     * @param <K>   The key type
     * @param <V>   The value type
     * @return A new Map containing the given entry
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K key, V value) {
        return createFromPairs(EntryComparator.natural(), key, value);
    }

    /**
     * Returns a singleton {@code TreeMap}, i.e. a {@code TreeMap} of one element.
     *
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param key           A singleton map key.
     * @param value         A singleton map value.
     * @param <K>           The key type
     * @param <V>           The value type
     * @return A new Map containing the given entry
     */
    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K key, V value) {
        return createFromPairs(EntryComparator.of(keyComparator), key, value);
    }

    /**
     * Returns a TreeMap containing {@code n} values of a given Function {@code f}
     * over a range of integer values from 0 to {@code n - 1}.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key
     * @param n             The number of elements in the TreeMap
     * @param f             The Function computing element values
     * @return A TreeMap consisting of elements {@code f(0),f(1), ..., f(n - 1)}
     * @throws NullPointerException if {@code keyComparator} or {@code f} are null
     */
    public static <K, V> TreeMap<K, V> tabulate(Comparator<? super K> keyComparator, int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V>> f) {
        Objects.requireNonNull(f, "f is null");
        return createTreeMap(EntryComparator.of(keyComparator), Collections.tabulate(n, f));
    }

    /**
     * Returns a TreeMap containing {@code n} values of a given Function {@code f}
     * over a range of integer values from 0 to {@code n - 1}.
     * The underlying key comparator is the natural comparator of K.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @param n   The number of elements in the TreeMap
     * @param f   The Function computing element values
     * @return A TreeMap consisting of elements {@code f(0),f(1), ..., f(n - 1)}
     * @throws NullPointerException if {@code f} is null
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> tabulate(int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V>> f) {
        Objects.requireNonNull(f, "f is null");
        return createTreeMap(EntryComparator.natural(), Collections.tabulate(n, f));
    }

    /**
     * Returns a TreeMap containing {@code n} values supplied by a given Supplier {@code s}.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key
     * @param n             The number of elements in the TreeMap
     * @param s             The Supplier computing element values
     * @return A TreeMap of size {@code n}, where each element contains the result supplied by {@code s}.
     * @throws NullPointerException if {@code keyComparator} or {@code s} are null
     */
    @SuppressWarnings("unchecked")
    public static <K, V> TreeMap<K, V> fill(Comparator<? super K> keyComparator, int n, Supplier<? extends Tuple2<? extends K, ? extends V>> s) {
        Objects.requireNonNull(s, "s is null");
        return createTreeMap(EntryComparator.of(keyComparator), Collections.fill(n, s));
    }

    /**
     * Returns a TreeMap containing {@code n} values supplied by a given Supplier {@code s}.
     * The underlying key comparator is the natural comparator of K.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @param n   The number of elements in the TreeMap
     * @param s   The Supplier computing element values
     * @return A TreeMap of size {@code n}, where each element contains the result supplied by {@code s}.
     * @throws NullPointerException if {@code s} is null
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> fill(int n, Supplier<? extends Tuple2<? extends K, ? extends V>> s) {
        Objects.requireNonNull(s, "s is null");
        return createTreeMap(EntryComparator.natural(), Collections.fill(n, s));
    }

    /**
     * Creates a {@code TreeMap} of the given entries using the natural key comparator.
     *
     * @param <K>     The key type
     * @param <V>     The value type
     * @param entries Map entries
     * @return A new TreeMap containing the given entries.
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> ofEntries(Tuple2<? extends K, ? extends V>... entries) {
        return createFromTuples(EntryComparator.natural(), entries);
    }

    /**
     * Creates a {@code TreeMap} of the given entries using the given key comparator.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param entries       Map entries
     * @return A new TreeMap containing the given entries.
     */
    @SuppressWarnings({ "unchecked", "varargs" })
    @SafeVarargs
    public static <K, V> TreeMap<K, V> ofEntries(Comparator<? super K> keyComparator, Tuple2<? extends K, ? extends V>... entries) {
        return createFromTuples(EntryComparator.of(keyComparator), entries);
    }

    /**
     * Creates a {@code TreeMap} of the given entries using the natural key comparator.
     *
     * @param <K>     The key type
     * @param <V>     The value type
     * @param entries Map entries
     * @return A new TreeMap containing the given entries.
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> ofEntries(java.util.Map.Entry<? extends K, ? extends V>... entries) {
        return createFromMapEntries(EntryComparator.natural(), entries);
    }

    /**
     * Creates a {@code TreeMap} of the given entries using the given key comparator.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param entries       Map entries
     * @return A new TreeMap containing the given entries.
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <K, V> TreeMap<K, V> ofEntries(Comparator<? super K> keyComparator, java.util.Map.Entry<? extends K, ? extends V>... entries) {
        return createFromMapEntries(EntryComparator.of(keyComparator), entries);
    }

    /**
     * Creates a {@code TreeMap} of the given entries.
     *
     * @param <K>     The key type
     * @param <V>     The value type
     * @param entries Map entries
     * @return A new TreeMap containing the given entries.
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> ofEntries(Iterable<? extends Tuple2<? extends K, ? extends V>> entries) {
        return createTreeMap(EntryComparator.natural(), entries);
    }

    /**
     * Creates a {@code TreeMap} of the given entries.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param entries       Map entries
     * @return A new TreeMap containing the given entries.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> TreeMap<K, V> ofEntries(Comparator<? super K> keyComparator, Iterable<? extends Tuple2<? extends K, ? extends V>> entries) {
        return createTreeMap(EntryComparator.of(keyComparator), entries);
    }

    // -- TreeMap API

    @Override
    public <K2, V2> TreeMap<K2, V2> bimap(Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valueMapper) {
        return bimap(this, EntryComparator.natural(), keyMapper, valueMapper);
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> bimap(Comparator<? super K2> keyComparator,
            Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valueMapper) {
        return bimap(this, EntryComparator.of(keyComparator), keyMapper, valueMapper);
    }

    @Override
    public boolean containsKey(K key) {
        return entries.contains(new Tuple2<>(key, /*ignored*/null));
    }

    @Override
    public TreeMap<K, V> distinct() {
        return Maps.distinct(this);
    }

    @Override
    public TreeMap<K, V> distinctBy(Comparator<? super Tuple2<K, V>> comparator) {
        return Maps.distinctBy(this, this::createFromEntries, comparator);
    }

    @Override
    public <U> TreeMap<K, V> distinctBy(Function<? super Tuple2<K, V>, ? extends U> keyExtractor) {
        return Maps.distinctBy(this, this::createFromEntries, keyExtractor);
    }

    @Override
    public TreeMap<K, V> drop(long n) {
        return Maps.drop(this, this::createFromEntries, this::emptyInstance, n);
    }

    @Override
    public TreeMap<K, V> dropRight(long n) {
        return Maps.dropRight(this, this::createFromEntries, this::emptyInstance, n);
    }

    @Override
    public TreeMap<K, V> dropUntil(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.dropUntil(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> dropWhile(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.dropWhile(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> filter(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.filter(this, this::createFromEntries, predicate);
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        return flatMap(this, EntryComparator.natural(), mapper);
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> flatMap(Comparator<? super K2> keyComparator,
            BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        return flatMap(this, EntryComparator.of(keyComparator), mapper);
    }

    @Override
    public Option<V> get(K key) {
        final V ignored = null;
        return entries.find(new Tuple2<>(key, ignored)).map(Tuple2::_2);
    }

    @Override
    public <C> Map<C, TreeMap<K, V>> groupBy(Function<? super Tuple2<K, V>, ? extends C> classifier) {
        return Maps.groupBy(this, this::createFromEntries, classifier);
    }

    @Override
    public Iterator<TreeMap<K, V>> grouped(long size) {
        return Maps.grouped(this, this::createFromEntries, size);
    }

    @Override
    public Tuple2<K, V> head() {
        if (isEmpty()) {
            throw new NoSuchElementException("head of empty TreeMap");
        } else {
            return entries.min().get();
        }
    }

    @Override
    public TreeMap<K, V> init() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("init of empty TreeMap");
        } else {
            final Tuple2<K, V> max = entries.max().get();
            return new TreeMap<>(entries.delete(max));
        }
    }

    @Override
    public Option<TreeMap<K, V>> initOption() {
        return Maps.initOption(this);
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public Iterator<Tuple2<K, V>> iterator() {
        return entries.iterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Comparator<K> keyComparator() {
        return ((EntryComparator<K, V>) entries.comparator()).keyComparator();
    }

    @Override
    public SortedSet<K> keySet() {
        return TreeSet.ofAll(keyComparator(), iterator().map(Tuple2::_1));
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> map(BiFunction<? super K, ? super V, Tuple2<K2, V2>> mapper) {
        return map(this, EntryComparator.natural(), mapper);
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> map(Comparator<? super K2> keyComparator,
            BiFunction<? super K, ? super V, Tuple2<K2, V2>> mapper) {
        Objects.requireNonNull(keyComparator, "keyComparator is null");
        return map(this, EntryComparator.of(keyComparator), mapper);
    }

    @Override
    public <W> TreeMap<K, W> mapValues(Function<? super V, ? extends W> valueMapper) {
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return map(keyComparator(), (k, v) -> Tuple.of(k, valueMapper.apply(v)));
    }

    @Override
    public TreeMap<K, V> merge(Map<? extends K, ? extends V> that) {
        return Maps.merge(this, this::createFromEntries, that);
    }

    @Override
    public <U extends V> TreeMap<K, V> merge(Map<? extends K, U> that,
            BiFunction<? super V, ? super U, ? extends V> collisionResolution) {
        return Maps.merge(this, this::createFromEntries, that, collisionResolution);
    }

    @Override
    public Tuple2<TreeMap<K, V>, TreeMap<K, V>> partition(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.partition(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> peek(Consumer<? super Tuple2<K, V>> action) {
        return Maps.peek(this, action);
    }

    @Override
    public TreeMap<K, V> put(K key, V value) {
        return new TreeMap<>(entries.insert(new Tuple2<>(key, value)));
    }

    @Override
    public TreeMap<K, V> put(Tuple2<? extends K, ? extends V> entry) {
        return Maps.put(this, entry);
    }

    @Override
    public TreeMap<K, V> remove(K key) {
        final V ignored = null;
        final Tuple2<K, V> entry = new Tuple2<>(key, ignored);
        if (entries.contains(entry)) {
            return new TreeMap<>(entries.delete(entry));
        } else {
            return this;
        }
    }

    @Override
    public TreeMap<K, V> removeAll(Iterable<? extends K> keys) {
        final V ignored = null;
        RedBlackTree<Tuple2<K, V>> removed = entries;
        for (K key : keys) {
            final Tuple2<K, V> entry = new Tuple2<>(key, ignored);
            if (removed.contains(entry)) {
                removed = removed.delete(entry);
            }
        }
        if (removed.size() == entries.size()) {
            return this;
        } else {
            return new TreeMap<>(removed);
        }
    }

    @Override
    public TreeMap<K, V> replace(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        return Maps.replace(this, currentElement, newElement);
    }

    @Override
    public TreeMap<K, V> replaceAll(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        return Maps.replaceAll(this, currentElement, newElement);
    }

    @Override
    public TreeMap<K, V> retainAll(Iterable<? extends Tuple2<K, V>> elements) {
        Objects.requireNonNull(elements, "elements is null");
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(entries.comparator());
        for (Tuple2<K, V> entry : elements) {
            if (contains(entry)) {
                tree = tree.insert(entry);
            }
        }
        return new TreeMap<>(tree);
    }

    @Override
    public TreeMap<K, V> scan(
            Tuple2<K, V> zero,
            BiFunction<? super Tuple2<K, V>, ? super Tuple2<K, V>, ? extends Tuple2<K, V>> operation) {
        return Maps.scan(this, this::emptyInstance, zero, operation);
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public Iterator<TreeMap<K, V>> sliding(long size) {
        return Maps.sliding(this, this::createFromEntries, size);
    }

    @Override
    public Iterator<TreeMap<K, V>> sliding(long size, long step) {
        return Maps.sliding(this, this::createFromEntries, size, step);
    }

    @Override
    public Tuple2<TreeMap<K, V>, TreeMap<K, V>> span(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.span(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> tail() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("tail of empty TreeMap");
        } else {
            final Tuple2<K, V> min = entries.min().get();
            return new TreeMap<>(entries.delete(min));
        }
    }

    @Override
    public Option<TreeMap<K, V>> tailOption() {
        return Maps.tailOption(this);
    }

    @Override
    public TreeMap<K, V> take(long n) {
        return Maps.take(this, this::createFromEntries, n);
    }

    @Override
    public TreeMap<K, V> takeRight(long n) {
        return Maps.takeRight(this, this::createFromEntries, n);
    }

    @Override
    public TreeMap<K, V> takeUntil(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.takeUntil(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> takeWhile(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.takeWhile(this, this::createFromEntries, predicate);
    }

    @Override
    public java.util.TreeMap<K, V> toJavaMap() {
        return toJavaMap(() -> new java.util.TreeMap<>(keyComparator()), t -> t);
    }

    @Override
    public Seq<V> values() {
        return iterator().map(Tuple2::_2).toStream();
    }

    // -- Object

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof TreeMap) {
            final TreeMap<?, ?> that = (TreeMap<?, ?>) o;
            return entries.equals(that.entries);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return entries.hashCode();
    }

    @Override
    public String stringPrefix() {
        return "TreeMap";
    }

    @Override
    public String toString() {
        return mkString(stringPrefix() + "(", ", ", ")");
    }

    // -- private helpers

    private static <K, K2, V, V2> TreeMap<K2, V2> bimap(TreeMap<K, V> map, EntryComparator<K2, V2> entryComparator,
            Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return createTreeMap(entryComparator, map.entries, entry -> entry.map(keyMapper, valueMapper));
    }

    private static <K, V, K2, V2> TreeMap<K2, V2> flatMap(TreeMap<K, V> map, EntryComparator<K2, V2> entryComparator,
            BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return createTreeMap(entryComparator, map.entries.iterator().flatMap(entry -> mapper.apply(entry._1, entry._2)));
    }

    private static <K, K2, V, V2> TreeMap<K2, V2> map(TreeMap<K, V> map, EntryComparator<K2, V2> entryComparator,
            BiFunction<? super K, ? super V, Tuple2<K2, V2>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return createTreeMap(entryComparator, map.entries, entry -> entry.map(mapper));
    }

    // -- internal factory methods

    private static <K, V> Collector<Tuple2<K, V>, ArrayList<Tuple2<K, V>>, TreeMap<K, V>> createCollector(EntryComparator<K, V> entryComparator) {
        final Supplier<ArrayList<Tuple2<K, V>>> supplier = ArrayList::new;
        final BiConsumer<ArrayList<Tuple2<K, V>>, Tuple2<K, V>> accumulator = ArrayList::add;
        final BinaryOperator<ArrayList<Tuple2<K, V>>> combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        final Function<ArrayList<Tuple2<K, V>>, TreeMap<K, V>> finisher = list -> createTreeMap(entryComparator, list);
        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> TreeMap<K, V> createTreeMap(EntryComparator<K, V> entryComparator,
            Iterable<? extends Tuple2<? extends K, ? extends V>> entries) {
        Objects.requireNonNull(entries, "entries is null");
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(entryComparator);
        for (Tuple2<K, V> entry : (Iterable<Tuple2<K, V>>) entries) {
            tree = tree.insert(entry);
        }
        return new TreeMap<>(tree);
    }

    private static <K, K2, V, V2> TreeMap<K2, V2> createTreeMap(EntryComparator<K2, V2> entryComparator,
            Iterable<Tuple2<K, V>> entries, Function<Tuple2<K, V>, Tuple2<K2, V2>> entryMapper) {
        RedBlackTree<Tuple2<K2, V2>> tree = RedBlackTree.empty(entryComparator);
        for (Tuple2<K, V> entry : entries) {
            tree = tree.insert(entryMapper.apply(entry));
        }
        return new TreeMap<>(tree);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> TreeMap<K, V> createFromMap(EntryComparator<K, V> entryComparator, java.util.Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map is null");
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(entryComparator);
        for (java.util.Map.Entry<K, V> entry : ((java.util.Map<K, V>) map).entrySet()) {
            tree = tree.insert(Tuple.of(entry.getKey(), entry.getValue()));
        }
        return new TreeMap<>(tree);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> TreeMap<K, V> createFromTuple(EntryComparator<K, V> entryComparator, Tuple2<? extends K, ? extends V> entry) {
        Objects.requireNonNull(entry, "entry is null");
        return new TreeMap<>(RedBlackTree.of(entryComparator, (Tuple2<K, V>) entry));
    }

    @SuppressWarnings("unchecked")
    private static <K, V> TreeMap<K, V> createFromTuples(EntryComparator<K, V> entryComparator, Tuple2<? extends K, ? extends V>... entries) {
        Objects.requireNonNull(entries, "entries is null");
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(entryComparator);
        for (Tuple2<? extends K, ? extends V> entry : entries) {
            tree = tree.insert((Tuple2<K, V>) entry);
        }
        return new TreeMap<>(tree);
    }

    @SafeVarargs
    private static <K, V> TreeMap<K, V> createFromMapEntries(EntryComparator<K, V> entryComparator, java.util.Map.Entry<? extends K, ? extends V>... entries) {
        Objects.requireNonNull(entries, "entries is null");
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(entryComparator);
        for (java.util.Map.Entry<? extends K, ? extends V> entry : entries) {
            final K key = entry.getKey();
            final V value = entry.getValue();
            tree = tree.insert(Tuple.of(key, value));
        }
        return new TreeMap<>(tree);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> TreeMap<K, V> createFromPairs(EntryComparator<K, V> entryComparator, Object... pairs) {
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(entryComparator);
        for (int i = 0; i < pairs.length; i += 2) {
            final K key = (K) pairs[i];
            final V value = (V) pairs[i + 1];
            tree = tree.insert(Tuple.of(key, value));
        }
        return new TreeMap<>(tree);
    }

    private TreeMap<K, V> createFromEntries(Iterable<Tuple2<K, V>> tuples) {
        return createTreeMap((EntryComparator<K, V>) entries.comparator(), tuples);
    }

    private TreeMap<K, V> emptyInstance() {
        return isEmpty() ? this : new TreeMap<>(entries.emptyInstance());
    }

    // -- internal types

    private interface EntryComparator<K, V> extends Comparator<Tuple2<K, V>>, Serializable {

        long serialVersionUID = 1L;

        static <K, V> EntryComparator<K, V> of(Comparator<? super K> keyComparator) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            return new Specific<>(keyComparator);
        }

        static <K, V> EntryComparator<K, V> natural() {
            return Natural.instance();
        }

        Comparator<K> keyComparator();

        // -- internal impls

        final class Specific<K, V> implements EntryComparator<K, V> {

            private static final long serialVersionUID = 1L;

            private final Comparator<K> keyComparator;

            @SuppressWarnings("unchecked")
            Specific(Comparator<? super K> keyComparator) {
                this.keyComparator = (Comparator<K>) keyComparator;
            }

            @Override
            public int compare(Tuple2<K, V> e1, Tuple2<K, V> e2) {
                return keyComparator.compare(e1._1, e2._1);
            }

            @Override
            public Comparator<K> keyComparator() {
                return keyComparator;
            }
        }

        final class Natural<K, V> implements EntryComparator<K, V> {

            private static final long serialVersionUID = 1L;

            private static final Natural<?, ?> INSTANCE = new Natural<>();

            // hidden
            private Natural() {
            }

            @SuppressWarnings("unchecked")
            public static <K, V> Natural<K, V> instance() {
                return (Natural<K, V>) INSTANCE;
            }

            @SuppressWarnings("unchecked")
            @Override
            public int compare(Tuple2<K, V> e1, Tuple2<K, V> e2) {
                final K key1 = e1._1;
                final K key2 = e2._1;
                return ((Comparable<K>) key1).compareTo(key2);
            }

            @Override
            public Comparator<K> keyComparator() {
                return naturalComparator();
            }

            /**
             * Instance control for object serialization.
             *
             * @return The singleton instance of NaturalEntryComparator.
             * @see java.io.Serializable
             */
            private Object readResolve() {
                return INSTANCE;
            }
        }
    }
}
