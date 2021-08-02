package fun.qianrui.staticUtil.function;


import java.util.*;
import java.util.function.Function;

/**
 * @author 20021438
 * 2020/10/22
 */
public class MapBuilder<K, V> {
    public final Map<K, V> map;

    public MapBuilder() {
        this.map = new HashMap<>();
    }

    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public Map<K, V> unmodifiableMap() {
        return map.size() == 0 ? Collections.emptyMap() : Collections.unmodifiableMap(map);
    }

    public String toJson() {
        return toString();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public static <T, K> Map<K, List<T>> toMap(Collection<T> list, Function<T, K> key) {
        Map<K, List<T>> map = new HashMap<>();
        if (list == null) {
            return map;
        }
        for (T t : list) {
            map.computeIfAbsent(key.apply(t), k -> new ArrayList<>())
                    .add(t);
        }
        return map;
    }

    public static <T, K> Map<K, T> toKeyMap(Collection<T> list, Function<T, K> key) {
        return toKeyMap(list, key, Function.identity());
    }

    public static <T, K, V> Map<K, V> toKeyMap(Collection<T> list, Function<T, K> key, Function<T, V> value) {
        Map<K, V> map = new HashMap<>();
        if (list == null) {
            return map;
        }
        for (T t : list) {
            K k = key.apply(t);
            V v = value.apply(t);
            V old = map.put(k, v);
            if (old != null) {
                throw new IllegalArgumentException("has not unique key(k:'" + k + "',v:'" + v + "','" + old + "')" + list);
            }
        }
        return map;
    }

    public static <T, K> Map<K, List<T>> toUnmodifiableMap(Collection<T> list, Function<T, K> key) {
        return Collections.unmodifiableMap(toMap(list, key));
    }

    public static <T, K> Map<K, T> toKeyUnmodifiableMap(Collection<T> list, Function<T, K> key) {
        return Collections.unmodifiableMap(toKeyMap(list, key));
    }

    public static <T, K, V> Map<K, V> toKeyUnmodifiableMap(Collection<T> list, Function<T, K> key, Function<T, V> value) {
        return Collections.unmodifiableMap(toKeyMap(list, key, value));
    }
}