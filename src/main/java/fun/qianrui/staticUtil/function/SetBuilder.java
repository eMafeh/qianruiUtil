package fun.qianrui.staticUtil.function;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 20021438
 * 2021/1/13
 */
public class SetBuilder<T> {
    public final Set<T> set;

    public SetBuilder() {
        this.set = new HashSet<>();
    }

    public SetBuilder<T> add(T t) {
        set.add(t);
        return this;
    }

    public SetBuilder<T> remove(T t) {
        set.remove(t);
        return this;
    }

    public Set<T> unmodifiableSet() {
        return set.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(set);
    }

    public String toJson() {
        return toString();
    }

    @Override
    public String toString() {
        return set.toString();
    }

    public static <S, T> Set<T> mapping(Collection<S> list, Function<S, T> mapping) {
        return list == null ? Collections.emptySet() : list.stream()
                .map(mapping)
                .collect(Collectors.toSet());
    }

    public static <S, T> Set<T> mappingFilterNull(Collection<S> list, Function<S, T> mapping) {
        return list == null ? Collections.emptySet() : list.stream()
                .map(mapping)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static <S> Set<S> filter(Collection<S> list, Predicate<? super S> predicate) {
        return list == null ? Collections.emptySet() : list.stream()
                .filter(predicate)
                .collect(Collectors.toSet());
    }

    public static <T> Set<T> concat(Collection<T> list1, Collection<T> list2) {
        return Stream.concat(list1.stream(), list2.stream())
                .collect(Collectors.toSet());
    }
}
