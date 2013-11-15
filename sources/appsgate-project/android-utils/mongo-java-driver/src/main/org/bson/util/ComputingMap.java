package org.bson.util;

import static org.bson.util.Assertions.notNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

final class ComputingMap<K, V> implements Map<K, V>, Function<K, V> {

    public static <K, V> Map<K, V> create(Function<K, V> function) {
	return new ComputingMap<K, V>(CopyOnWriteMap.<K, V> newHashMap(),
		function);
    }

    private final ConcurrentMap<K, V> map;
    private final Function<K, V> function;

    ComputingMap(ConcurrentMap<K, V> map, Function<K, V> function) {
	this.map = notNull("map", map);
	this.function = notNull("function", function);
    }

    public V apply(K k) {
	return get(k);
    }

    public void clear() {
	map.clear();
    }

    public boolean containsKey(Object key) {
	return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
	return map.containsValue(value);
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
	return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
	return map.equals(o);
    }

    public V get(Object key) {
	while (true) {
	    V v = map.get(key);
	    if (v != null) {
		return v;
	    }
	    @SuppressWarnings("unchecked")
	    K k = (K) key;
	    V value = function.apply(k);
	    if (value == null) {
		return null;
	    }
	    map.putIfAbsent(k, value);
	}
    }

    @Override
    public int hashCode() {
	return map.hashCode();
    }

    public boolean isEmpty() {
	return map.isEmpty();
    }

    public Set<K> keySet() {
	return map.keySet();
    }

    public V put(K key, V value) {
	return map.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
	map.putAll(m);
    }

    public V putIfAbsent(K key, V value) {
	return map.putIfAbsent(key, value);
    }

    public V remove(Object key) {
	return map.remove(key);
    }

    public boolean remove(Object key, Object value) {
	return map.remove(key, value);
    }

    public V replace(K key, V value) {
	return map.replace(key, value);
    }

    public boolean replace(K key, V oldValue, V newValue) {
	return map.replace(key, oldValue, newValue);
    }

    public int size() {
	return map.size();
    }

    public Collection<V> values() {
	return map.values();
    }
}
