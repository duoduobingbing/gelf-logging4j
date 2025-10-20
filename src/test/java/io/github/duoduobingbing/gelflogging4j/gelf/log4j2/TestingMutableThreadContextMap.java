package io.github.duoduobingbing.gelflogging4j.gelf.log4j2;

import org.apache.logging.log4j.spi.ObjectThreadContextMap;
import org.apache.logging.log4j.spi.ReadOnlyThreadContextMap;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author duoduobingbing
 */
public class TestingMutableThreadContextMap implements ReadOnlyThreadContextMap, ObjectThreadContextMap {

    private final ThreadLocal<Map<String, Object>> localMap;

    public TestingMutableThreadContextMap() {
        this.localMap = new ThreadLocal<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getValue(String key) {
        return Optional
                .ofNullable(localMap.get())
                .map(map -> (V) map.get(key))
                .orElse(null);
    }

    @Override
    public <V> void putValue(String key, V value) {
        Optional
                .ofNullable(localMap.get())
                .ifPresentOrElse(
                        map -> map.put(key, value),
                        () -> localMap.set(new ConcurrentHashMap<>(Map.of(key, value)))
                );
    }

    @Override
    public <V> void putAllValues(Map<String, V> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        Optional
                .ofNullable(localMap.get())
                .ifPresentOrElse(
                        map -> map.putAll(values),
                        () -> localMap.set(new ConcurrentHashMap<>(values))
                );
    }

    @Override
    public void removeAll(Iterable<String> keys) {
        if (keys == null) {
            return;
        }

        Optional
                .ofNullable(localMap.get())
                .ifPresent(map -> keys.forEach(map::remove));
    }

    @Override
    public void clear() {
        localMap.remove();
    }

    @Override
    public boolean containsKey(String key) {
        return Optional
                .ofNullable(localMap.get())
                .map(map -> map.containsKey(key))
                .orElse(false);
    }

    @Override
    public String get(String key) {
        return Objects.toString(this.<Object>getValue(key));
    }

    @Override
    public Map<String, String> getCopy() {
        return Optional
                .ofNullable(localMap.get())
                .map(map -> new ConcurrentHashMap<>(
                                map
                                        .entrySet()
                                        .stream()
                                        .collect(
                                                Collectors.toMap(
                                                        Entry::getKey,
                                                        v -> Objects.toString(v.getValue(), null)
                                                )
                                        )
                        )
                )
                .orElse(null);
    }

    @Override
    public Map<String, String> getImmutableMapOrNull() {
        return Optional
                .ofNullable(localMap.get())
                .map(map -> new ConcurrentHashMap<>(
                                map
                                        .entrySet()
                                        .stream()
                                        .collect(
                                                Collectors.toMap(
                                                        Entry::getKey,
                                                        v -> Objects.toString(v.getValue(), null)
                                                )
                                        )
                        )
                )
                .orElse(new ConcurrentHashMap<>());
    }

    @Override
    public void putAll(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        Optional
                .ofNullable(localMap.get())
                .ifPresent(map -> map.putAll(values));
    }

    @Override
    public StringMap getReadOnlyContextData() {

        return Optional
                .ofNullable(localMap.get())
                .map(SortedArrayStringMap::new)
                .orElse(new SortedArrayStringMap(1));
    }

    @Override
    public boolean isEmpty() {

        return Optional
                .ofNullable(localMap.get())
                .map(Map::isEmpty)
                .orElse(true);
    }

    @Override
    public void put(String key, String value) {
        putValue(key, value);
    }

    @Override
    public void remove(String key) {

        Optional
                .ofNullable(localMap.get())
                .ifPresent(map -> map.remove(key));
    }

    @Override
    public String toString() {

        return Optional
                .ofNullable(localMap.get())
                .map(Map::toString)
                .orElse("{}");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        final Map<String, Object> map = this.localMap.get();
        result = prime * result + ((map == null) ? 0 : map.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ThreadContextMap other)) {
            return false;
        }

        final Map<String, String> map = this.getImmutableMapOrNull();
        final Map<String, String> otherMap = other.getImmutableMapOrNull();

        if (map == null && otherMap == null) {
            return true;
        }

        if (map == null) {
            return false;
        } else {
            return map.equals(otherMap);
        }
    }
}
