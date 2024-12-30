package org.obapanel.lockfactoryserver.server.utils.primitivesCache;

public interface RemoveListener<K> {

    void onRemove(String name, K data);
}
