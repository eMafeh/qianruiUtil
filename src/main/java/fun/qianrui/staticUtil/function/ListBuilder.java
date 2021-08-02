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
public class ListBuilder<T> {
    public final List<T> list;

    public ListBuilder() {
        this.list = new ArrayList<>();
    }

    public ListBuilder<T> add(T t) {
        list.add(t);
        return this;
    }

    public List<T> unmodifiableList() {
        return list.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    public String toJson() {
        return toString();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    public static <S, T> List<T> mapping(Collection<S> list, Function<S, T> mapping) {
        return list == null ? Collections.emptyList() : list.stream()
                .map(mapping)
                .collect(Collectors.toList());
    }

    public static <S, T> List<T> mappingFilterNull(Collection<S> list, Function<S, T> mapping) {
        return list == null ? Collections.emptyList() : list.stream()
                .map(mapping)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static <S> List<S> filter(Collection<S> list, Predicate<? super S> predicate) {
        return list == null ? Collections.emptyList() : list.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public static <T> List<T> concat(Collection<? extends T> list1, Collection<? extends T> list2) {
        return Stream.concat(list1.stream(), list2.stream())
                .collect(Collectors.toList());
    }

    public static <T> LinkedList<List<T>> batch(Collection<T> list, int batchNum) {
        LinkedList<List<T>> result = new LinkedList<>();
        if (list != null) {
            List<T> batch = new ArrayList<>(batchNum);
            for (T t : list) {
                batch.add(t);
                if (batch.size() == batchNum) {
                    result.add(batch);
                    batch = new ArrayList<>(batchNum);
                }
            }
            if (!batch.isEmpty()) {
                result.add(batch);
            }
        }
        return result;
    }

}
