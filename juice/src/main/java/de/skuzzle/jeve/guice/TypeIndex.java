package de.skuzzle.jeve.guice;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

final class TypeIndex {

    private final Map<Class<?>, Set<Class<?>>> map;

    public TypeIndex() {
        this.map = new HashMap<>();
    }

    public void addImplementor(Class<?> intf, Class<?> implementor) {
        if (!intf.isAssignableFrom(implementor)) {
            throw new IllegalArgumentException(String.format(
                    "%s does not implement/extend %s",
                    implementor.getName(), intf.getName()));
        }
        this.map.computeIfAbsent(intf, k -> new HashSet<>()).add(implementor);
    }

    @SuppressWarnings("unchecked")
    public <T> Stream<Class<T>> findImplementorsOf(Class<T> type) {
        return this.map.getOrDefault(type, Collections.emptySet())
                .stream()
                .map(cls -> (Class<T>) cls);
    }
}
