/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2017 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package javaslang.collection;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.HashArrayMappedTrieModule.EmptyNode;
import javaslang.control.Option;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import static javaslang.collection.HashArrayMappedTrieModule.Action.PUT;
import static javaslang.collection.HashArrayMappedTrieModule.Action.REMOVE;

/**
 * An immutable <a href="https://en.wikipedia.org/wiki/Hash_array_mapped_trie">Hash array mapped trie (HAMT)</a>.
 *
 * @author Ruslan Sennov
 * @since 2.0.0
 */
interface HashArrayMappedTrie<K, V> extends Iterable<Tuple2<K, V>> {

    static <K, V> HashArrayMappedTrie<K, V> empty() {
        return EmptyNode.instance();
    }

    boolean isEmpty();

    int size();

    Option<V> get(K key);

    boolean containsKey(K key);

    HashArrayMappedTrie<K, V> put(K key, V value);

    HashArrayMappedTrie<K, V> remove(K key);

    @Override
    Iterator<Tuple2<K, V>> iterator();

    Iterator<K> keysIterator();
}

interface HashArrayMappedTrieModule {

    enum Action {
        PUT, REMOVE
    }

    class LeafNodeIterator<K, V> extends AbstractIterator<LeafNode<K, V>> {

        // buckets levels + leaf level = (Integer.SIZE / AbstractNode.SIZE + 1) + 1
        private final static int MAX_LEVELS = Integer.SIZE / AbstractNode.SIZE + 2;

        private final int total;
        private final Object[] nodes = new Object[MAX_LEVELS];
        private final int[] indexes = new int[MAX_LEVELS];

        private int level;
        private int ptr = 0;

        LeafNodeIterator(AbstractNode<K, V> root) {
            total = root.size();
            level = downstairs(nodes, indexes, root, 0);
        }

        @Override
        public boolean hasNext() {
            return ptr < total;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected LeafNode<K, V> getNext() {
            Object node = nodes[level];
            while (!(node instanceof LeafNode)) {
                node = findNextLeaf();
            }
            ptr++;
            if (node instanceof LeafList) {
                LeafList<K, V> leaf = (LeafList<K, V>) node;
                nodes[level] = leaf.tail;
                return leaf;
            } else {
                nodes[level] = EmptyNode.instance();
                return (LeafSingleton<K, V>) node;
            }
        }

        @SuppressWarnings("unchecked")
        private Object findNextLeaf() {
            AbstractNode<K, V> node = null;
            while (level > 0) {
                level--;
                indexes[level]++;
                node = getChild((AbstractNode<K, V>) nodes[level], indexes[level]);
                if (node != null) {
                    break;
                }
            }
            level = downstairs(nodes, indexes, node, level + 1);
            return nodes[level];
        }

        private static <K, V> int downstairs(Object[] nodes, int[] indexes, AbstractNode<K, V> root, int level) {
            while (true) {
                nodes[level] = root;
                indexes[level] = 0;
                root = getChild(root, 0);
                if (root == null) {
                    break;
                } else {
                    level++;
                }
            }
            return level;
        }

        @SuppressWarnings("unchecked")
        private static <K, V> AbstractNode<K, V> getChild(AbstractNode<K, V> node, int index) {
            if (node instanceof IndexedNode) {
                Object[] subNodes = ((IndexedNode<K, V>) node).subNodes;
                return index < subNodes.length ? (AbstractNode<K, V>) subNodes[index] : null;
            } else if (node instanceof ArrayNode) {
                ArrayNode<K, V> arrayNode = (ArrayNode<K, V>) node;
                return index < AbstractNode.BUCKET_SIZE ? (AbstractNode<K, V>) arrayNode.subNodes[index] : null;
            }
            return null;
        }
    }

    /**
     * An abstract base class for nodes of a HAMT.
     *
     * @param <K> Key type
     * @param <V> Value type
     */
    abstract class AbstractNode<K, V> implements HashArrayMappedTrie<K, V> {

        static final int SIZE = 5;
        static final int BUCKET_SIZE = 1 << SIZE;
        static final int MAX_INDEX_NODE = BUCKET_SIZE / 2;
        static final int MIN_ARRAY_NODE = BUCKET_SIZE / 4;

        private static final int M1 = 0x55555555;
        private static final int M2 = 0x33333333;
        private static final int M4 = 0x0f0f0f0f;

        static int bitCount(int x) {
            x = x - ((x >> 1) & M1);
            x = (x & M2) + ((x >> 2) & M2);
            x = (x + (x >> 4)) & M4;
            x = x + (x >> 8);
            x = x + (x >> 16);
            return x & 0x7f;
        }

        static int hashFragment(int shift, int hash) {
            return (hash >>> shift) & (BUCKET_SIZE - 1);
        }

        static int toBitmap(int hash) {
            return 1 << hash;
        }

        static int fromBitmap(int bitmap, int bit) {
            return bitCount(bitmap & (bit - 1));
        }

        static Object[] update(Object[] arr, int index, Object newElement) {
            Object[] newArr = Arrays.copyOf(arr, arr.length);
            newArr[index] = newElement;
            return newArr;
        }

        static Object[] remove(Object[] arr, int index) {
            Object[] newArr = new Object[arr.length - 1];
            System.arraycopy(arr, 0, newArr, 0, index);
            System.arraycopy(arr, index + 1, newArr, index, arr.length - index - 1);
            return newArr;
        }

        static Object[] insert(Object[] arr, int index, Object newElem) {
            Object[] newArr = new Object[arr.length + 1];
            System.arraycopy(arr, 0, newArr, 0, index);
            newArr[index] = newElem;
            System.arraycopy(arr, index, newArr, index + 1, arr.length - index);
            return newArr;
        }

        abstract Option<V> lookup(int shift, int keyHashCode, K key);

        abstract AbstractNode<K, V> modify(int shift, int keyHashCode, K key, V value, Action action);

        Iterator<LeafNode<K, V>> nodes() {
            return new LeafNodeIterator<>(this);
        }

        @Override
        public Iterator<Tuple2<K, V>> iterator() {
            return nodes().map(node -> Tuple.of(node.key(), node.value()));
        }

        @Override
        public Iterator<K> keysIterator() {
            return nodes().map(LeafNode::key);
        }

        @Override
        public Option<V> get(K key) {
            return lookup(0, Objects.hashCode(key), key);
        }

        @Override
        public boolean containsKey(K key) {
            return get(key).isDefined();
        }

        @Override
        public HashArrayMappedTrie<K, V> put(K key, V value) {
            return modify(0, Objects.hashCode(key), key, value, PUT);
        }

        @Override
        public HashArrayMappedTrie<K, V> remove(K key) {
            return modify(0, Objects.hashCode(key), key, null, REMOVE);
        }

        @SuppressWarnings("unchecked")
        @Override
        public final boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o instanceof HashArrayMappedTrie) {
                final HashArrayMappedTrie<Object, ?> that = (HashArrayMappedTrie<Object, ?>) o;
                if (this.size() == that.size()) {
                    final java.util.Iterator<Tuple2<K, V>> it = iterator();
                    while (it.hasNext()) {
                        Tuple2<K, V> thisEntry = it.next();
                        Option<?> thatValue = that.get(thisEntry._1);
                        if (!thatValue.isDefined() || !thatValue.get().equals(thisEntry._2)) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public abstract int hashCode();

        @Override
        public final String toString() {
            return iterator().map(t -> t._1 + " -> " + t._2).mkString("HashArrayMappedTrie(", ", ", ")");
        }
    }

    /**
     * The empty node.
     *
     * @param <K> Key type
     * @param <V> Value type
     */
    final class EmptyNode<K, V> extends AbstractNode<K, V> implements Serializable {

        private static final long serialVersionUID = 1L;

        private static final EmptyNode<?, ?> INSTANCE = new EmptyNode<>();

        private EmptyNode() {
        }

        @SuppressWarnings("unchecked")
        static <K, V> EmptyNode<K, V> instance() {
            return (EmptyNode<K, V>) INSTANCE;
        }

        @Override
        Option<V> lookup(int shift, int keyHashCode, K key) {
            return Option.none();
        }

        @Override
        AbstractNode<K, V> modify(int shift, int keyHashCode, K key, V value, Action action) {
            return (action == REMOVE) ? this : new LeafSingleton<>(keyHashCode, key, value);
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Iterator<LeafNode<K, V>> nodes() {
            return Iterator.empty();
        }

        @Override
        public int hashCode() {
            return 1;
        }

        /**
         * Instance control for object serialization.
         *
         * @return The singleton instance of EmptyNode.
         * @see java.io.Serializable
         */
        private Object readResolve() {
            return INSTANCE;
        }
    }

    /**
     * Representation of a HAMT leaf.
     *
     * @param <K> Key type
     * @param <V> Value type
     */
    abstract class LeafNode<K, V> extends AbstractNode<K, V> {

        abstract K key();

        abstract V value();

        abstract int hash();

        static <K, V> AbstractNode<K, V> mergeLeaves(int shift, LeafNode<K, V> leaf1, LeafSingleton<K, V> leaf2) {
            final int h1 = leaf1.hash();
            final int h2 = leaf2.hash();
            if (h1 == h2) {
                return new LeafList<>(h1, leaf2.key(), leaf2.value(), leaf1);
            }
            final int subH1 = hashFragment(shift, h1);
            final int subH2 = hashFragment(shift, h2);
            final int newBitmap = toBitmap(subH1) | toBitmap(subH2);
            if (subH1 == subH2) {
                AbstractNode<K, V> newLeaves = mergeLeaves(shift + SIZE, leaf1, leaf2);
                return new IndexedNode<>(newBitmap, newLeaves.size(), new Object[] { newLeaves });
            } else {
                return new IndexedNode<>(newBitmap, leaf1.size() + leaf2.size(),
                        subH1 < subH2 ? new Object[] { leaf1, leaf2 } : new Object[] { leaf2, leaf1 });
            }
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    /**
     * Representation of a HAMT leaf node with single element.
     *
     * @param <K> Key type
     * @param <V> Value type
     */
    final class LeafSingleton<K, V> extends LeafNode<K, V> implements Serializable {

        private static final long serialVersionUID = 1L;

        private final int hash;
        private final K key;
        private final V value;

        LeafSingleton(int hash, K key, V value) {
            this.hash = hash;
            this.key = key;
            this.value = value;
        }

        @Override
        Option<V> lookup(int shift, int keyHashCode, K key) {
            if (keyHashCode == hash && Objects.equals(key, this.key)) {
                return Option.some(value);
            } else {
                return Option.none();
            }
        }

        @Override
        AbstractNode<K, V> modify(int shift, int keyHashCode, K key, V value, Action action) {
            if (keyHashCode == hash && Objects.equals(key, this.key)) {
                return (action == REMOVE) ? EmptyNode.instance() : new LeafSingleton<>(hash, key, value);
            } else {
                return (action == REMOVE) ? this : mergeLeaves(shift, this, new LeafSingleton<>(keyHashCode, key, value));
            }
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Iterator<LeafNode<K, V>> nodes() {
            return Iterator.of(this);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hash, value);
        }

        @Override
        int hash() {
            return hash;
        }

        @Override
        K key() {
            return key;
        }

        @Override
        V value() {
            return value;
        }
    }

    /**
     * Representation of a HAMT leaf node with more than one element.
     *
     * @param <K> Key type
     * @param <V> Value type
     */
    final class LeafList<K, V> extends LeafNode<K, V> implements Serializable {

        private static final long serialVersionUID = 1L;

        private final int hash;
        private final K key;
        private final V value;
        private final int size;
        private final LeafNode<K, V> tail;

        LeafList(int hash, K key, V value, LeafNode<K, V> tail) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.size = 1 + tail.size();
            this.tail = tail;
        }

        @Override
        Option<V> lookup(int shift, int keyHashCode, K key) {
            if (hash != keyHashCode) {
                return Option.none();
            }
            return iterator().find(t -> Objects.equals(t._1, key)).map(t -> t._2);
        }

        @Override
        AbstractNode<K, V> modify(int shift, int keyHashCode, K key, V value, Action action) {
            if (keyHashCode == hash) {
                AbstractNode<K, V> filtered = removeElement(key);
                if (action == REMOVE) {
                    return filtered;
                } else {
                    return new LeafList<>(hash, key, value, (LeafNode<K, V>) filtered);
                }
            } else {
                return (action == REMOVE) ? this : mergeLeaves(shift, this, new LeafSingleton<>(keyHashCode, key, value));
            }
        }

        private static <K, V> AbstractNode<K, V> mergeNodes(LeafNode<K, V> leaf1, LeafNode<K, V> leaf2) {
            if(leaf2 == null) {
                return leaf1;
            }
            if(leaf1 instanceof LeafSingleton) {
                return new LeafList<>(leaf1.hash(), leaf1.key(), leaf1.value(), leaf2);
            }
            if(leaf2 instanceof LeafSingleton) {
                return new LeafList<>(leaf2.hash(), leaf2.key(), leaf2.value(), leaf1);
            }
            LeafNode<K, V> result = leaf1;
            LeafNode<K, V> tail = leaf2;
            while (tail instanceof LeafList) {
                final LeafList<K, V> list = (LeafList<K, V>) tail;
                result = new LeafList<>(list.hash, list.key, list.value, result);
                tail = list.tail;
            }
            return new LeafList<>(tail.hash(), tail.key(), tail.value(), result);
        }

        private AbstractNode<K, V> removeElement(K k) {
            if (Objects.equals(k, this.key)) {
                return tail;
            }
            LeafNode<K, V> leaf1 = new LeafSingleton<>(hash, key, value);
            LeafNode<K, V> leaf2 = tail;
            boolean found = false;
            while (!found && leaf2 != null) {
                if (Objects.equals(k, leaf2.key())) {
                    found = true;
                } else {
                    leaf1 = new LeafList<>(leaf2.hash(), leaf2.key(), leaf2.value(), leaf1);
                }
                leaf2 = leaf2 instanceof LeafList ? ((LeafList<K, V>) leaf2).tail : null;
            }
            return mergeNodes(leaf1, leaf2);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public Iterator<LeafNode<K, V>> nodes() {
            return new AbstractIterator<LeafNode<K, V>>() {
                LeafNode<K, V> node = LeafList.this;

                @Override
                public boolean hasNext() {
                    return node != null;
                }

                @Override
                public LeafNode<K, V> getNext() {
                    LeafNode<K, V> result = node;
                    if (node instanceof LeafSingleton) {
                        node = null;
                    } else {
                        node = ((LeafList<K, V>) node).tail;
                    }
                    return result;
                }
            };
        }

        @Override
        public int hashCode() {
            Iterator<LeafNode<K, V>> it = nodes();
            int hashCode = 0;
            while (it.hasNext()) {
                final LeafNode<K, V> node = it.next();
                hashCode += Objects.hash(node.key(), node.value());
            }
            return hashCode;
        }

        @Override
        int hash() {
            return hash;
        }

        @Override
        K key() {
            return key;
        }

        @Override
        V value() {
            return value;
        }
    }

    /**
     * Representation of a HAMT indexed node.
     *
     * @param <K> Key type
     * @param <V> Value type
     */
    final class IndexedNode<K, V> extends AbstractNode<K, V> implements Serializable {

        private static final long serialVersionUID = 1L;

        private final int bitmap;
        private final int size;
        private final Object[] subNodes;

        IndexedNode(int bitmap, int size, Object[] subNodes) {
            this.bitmap = bitmap;
            this.size = size;
            this.subNodes = subNodes;
        }

        @SuppressWarnings("unchecked")
        @Override
        Option<V> lookup(int shift, int keyHashCode, K key) {
            int frag = hashFragment(shift, keyHashCode);
            int bit = toBitmap(frag);
            if ((bitmap & bit) != 0) {
                AbstractNode<K, V> n = (AbstractNode<K, V>) subNodes[fromBitmap(bitmap, bit)];
                return n.lookup(shift + SIZE, keyHashCode, key);
            } else {
                return Option.none();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        AbstractNode<K, V> modify(int shift, int keyHashCode, K key, V value, Action action) {
            final int frag = hashFragment(shift, keyHashCode);
            final int bit = toBitmap(frag);
            final int index = fromBitmap(bitmap, bit);
            final int mask = bitmap;
            final boolean exists = (mask & bit) != 0;
            final AbstractNode<K, V> atIndx = exists ? (AbstractNode<K, V>) subNodes[index] : null;
            AbstractNode<K, V> child = exists ? atIndx.modify(shift + SIZE, keyHashCode, key, value, action)
                    : EmptyNode.<K, V> instance().modify(shift + SIZE, keyHashCode, key, value, action);
            boolean removed = exists && child.isEmpty();
            boolean added = !exists && !child.isEmpty();
            int newBitmap = removed ? mask & ~bit : added ? mask | bit : mask;
            if (newBitmap == 0) {
                return EmptyNode.instance();
            } else if (removed) {
                if (subNodes.length <= 2 && subNodes[index ^ 1] instanceof LeafNode) {
                    return (AbstractNode<K, V>) subNodes[index ^ 1]; // collapse
                } else {
                    return new IndexedNode<>(newBitmap, size - atIndx.size(), remove(subNodes, index));
                }
            } else if (added) {
                if (subNodes.length >= MAX_INDEX_NODE) {
                    return expand(frag, child, mask, subNodes);
                } else {
                    return new IndexedNode<>(newBitmap, size + child.size(), insert(subNodes, index, child));
                }
            } else {
                if (!exists) {
                    return this;
                } else {
                    return new IndexedNode<>(newBitmap, size - atIndx.size() + child.size(), update(subNodes, index, child));
                }
            }
        }

        private ArrayNode<K, V> expand(int frag, AbstractNode<K, V> child, int mask, Object[] subNodes) {
            int bit = mask;
            int count = 0;
            int ptr = 0;
            final Object[] arr = new Object[BUCKET_SIZE];
            for (int i = 0; i < BUCKET_SIZE; i++) {
                if ((bit & 1) != 0) {
                    arr[i] = subNodes[ptr++];
                    count++;
                } else if (i == frag) {
                    arr[i] = child;
                    count++;
                } else {
                    arr[i] = EmptyNode.instance();
                }
                bit = bit >>> 1;
            }
            return new ArrayNode<>(count, size + child.size(), arr);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public int hashCode() {
            return Objects.hash(subNodes);
        }
    }

    /**
     * Representation of a HAMT array node.
     *
     * @param <K> Key type
     * @param <V> Value type
     */
    final class ArrayNode<K, V> extends AbstractNode<K, V> implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Object[] subNodes;
        private final int count;
        private final int size;

        ArrayNode(int count, int size, Object[] subNodes) {
            this.subNodes = subNodes;
            this.count = count;
            this.size = size;
        }

        @SuppressWarnings("unchecked")
        @Override
        Option<V> lookup(int shift, int keyHashCode, K key) {
            int frag = hashFragment(shift, keyHashCode);
            AbstractNode<K, V> child = (AbstractNode<K, V>) subNodes[frag];
            return child.lookup(shift + SIZE, keyHashCode, key);
        }

        @SuppressWarnings("unchecked")
        @Override
        AbstractNode<K, V> modify(int shift, int keyHashCode, K key, V value, Action action) {
            int frag = hashFragment(shift, keyHashCode);
            AbstractNode<K, V> child = (AbstractNode<K, V>) subNodes[frag];
            AbstractNode<K, V> newChild = child.modify(shift + SIZE, keyHashCode, key, value, action);
            if (child.isEmpty() && !newChild.isEmpty()) {
                return new ArrayNode<>(count + 1, size + newChild.size(), update(subNodes, frag, newChild));
            } else if (!child.isEmpty() && newChild.isEmpty()) {
                if (count - 1 <= MIN_ARRAY_NODE) {
                    return pack(frag, subNodes);
                } else {
                    return new ArrayNode<>(count - 1, size - child.size(), update(subNodes, frag, EmptyNode.instance()));
                }
            } else {
                return new ArrayNode<>(count, size - child.size() + newChild.size(), update(subNodes, frag, newChild));
            }
        }

        @SuppressWarnings("unchecked")
        private IndexedNode<K, V> pack(int idx, Object[] elements) {
            Object[] arr = new Object[count - 1];
            int bitmap = 0;
            int size = 0;
            int ptr = 0;
            for (int i = 0; i < BUCKET_SIZE; i++) {
                AbstractNode<K, V> elem = (AbstractNode<K, V>) elements[i];
                if (i != idx && !elem.isEmpty()) {
                    size += elem.size();
                    arr[ptr++] = elem;
                    bitmap = bitmap | (1 << i);
                }
            }
            return new IndexedNode<>(bitmap, size, arr);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public int hashCode() {
            return Objects.hash(subNodes);
        }
    }
}
