package org.obapanel.lockfactoryserver.server;

import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FakeStreamObserver<K> implements StreamObserver<K> {

    private final AtomicBoolean completed = new AtomicBoolean(false);
    private final List<K> nextValues = new ArrayList<>();
    private final List<Throwable> errorValues = new ArrayList<>();

    public boolean isCompleted() {
        return completed.get();
    }

    public List<K> getNextValues() {
        return nextValues;
    }

    public K getNext() {
        return nextValues.isEmpty() ? null :nextValues.get(0);
    }


    public List<Throwable> getErrorValues() {
        return errorValues;
    }

    public Throwable getError() {
        return errorValues.isEmpty() ? null :errorValues.get(0);
    }

    @Override
    public void onNext(K value) {
        nextValues.add(value);
    }

    @Override
    public void onError(Throwable t) {
        errorValues.add(t);
    }

    @Override
    public void onCompleted() {
        completed.set(true);
    }


}
