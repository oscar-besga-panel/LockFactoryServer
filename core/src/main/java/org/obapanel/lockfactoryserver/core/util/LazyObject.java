package org.obapanel.lockfactoryserver.core.util;

import java.util.function.Supplier;

public class LazyObject<T> extends AbstractLazyObject<T> {

    private final Supplier<T> initializator;

    public LazyObject(Supplier<T> initializator) {
        this.initializator = initializator;
    }

    @Override
    protected T initialize() {
        return initializator.get();
    }
}
