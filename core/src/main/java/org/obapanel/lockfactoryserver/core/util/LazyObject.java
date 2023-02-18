package org.obapanel.lockfactoryserver.core.util;


/*
  From apache commons LazyInitializer
  https://commons.apache.org/proper/commons-lang/apidocs/src-html/org/apache/commons/lang3/concurrent/LazyInitializer.html
 */
public abstract class LazyObject<T> {

    private static final Object NO_INIT = new Object();

    @SuppressWarnings("unchecked")
    private volatile T object = (T) NO_INIT;

    public final T get() {
        T result = object;
        if (result == NO_INIT) {
            result = create();
        }
        return result;
    }

    private synchronized T create() {
        T result = object;
        if (result == NO_INIT) {
            object = result = initialize();
        }
        return result;
    }

    protected abstract T initialize();

}
