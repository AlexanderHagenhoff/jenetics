package io.jenetics.incubator.bean;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

final class PropertyIterator implements Iterator<Property> {

    private final Predicate<? super Class<?>> filter;

    private final Deque<Iterator<Property>> deque = new ArrayDeque<>();

    private PropertyIterator(
        final String basePath,
        final Object root,
        final Predicate<? super Class<?>> filter
    ) {
        requireNonNull(root);
        this.filter = requireNonNull(filter);

        deque.push(properties(basePath, root));
    }

    PropertyIterator(final String basePath, final Object root) {
        this(basePath, root, cls -> cls.getPackageName().startsWith("com.vig"));
    }

    PropertyIterator(final Object root) {
        this(null, root);
    }

    @Override
    public boolean hasNext() {
        final Iterator<Property> peek = deque.peek();
        return peek != null && peek.hasNext();
    }

    @Override
    public Property next() {
        final Iterator<Property> it = deque.peek();
        if (it == null) {
            throw new NoSuchElementException("No next element.");
        }

        final Property node = it.next();
        if (!it.hasNext()) {
            deque.pop();
        }

        final Iterator<Property> children = properties(node.path(), node.value());
        if (children.hasNext()) {
            deque.push(children);
        }

        return node;
    }

    private Iterator<Property> properties(
		final String basePath,
		final Object parent
	) {
        if (parent != null) {
            if (filter.test(parent.getClass())) {
                final var properties = new ArrayList<Property>();
                Beans.properties(basePath, parent).forEach(properties::add);
                return properties.iterator();
            }
        }

        return Collections.emptyIterator();
    }

}
