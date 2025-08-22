package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.grpc.NamePermits;
import org.obapanel.lockfactoryserver.core.grpc.NamePermitsWithTimeout;
import org.obapanel.lockfactoryserver.core.grpc.SemaphoreServerGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SemaphoreClientGrpcTest {

    @Mock
    private ManagedChannel managedChannel;

    @Mock
    private SemaphoreServerGrpc.SemaphoreServerBlockingStub stub;

    @Mock
    private SemaphoreServerGrpc.SemaphoreServerFutureStub futureStub;

    private MockedStatic<SemaphoreServerGrpc> mockedStaticSemaphoreServerGrpc;

    private SemaphoreClientGrpc semaphoreClientGrpc;

    private final String name = "sem" + System.currentTimeMillis();

    private final AtomicInteger current = new AtomicInteger(1);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final List<FakeListenableFuture<Empty>> listenableFutures = new ArrayList<>();

    @Before
    public void setup() {
        current.set(ThreadLocalRandom.current().nextInt(10));
        mockedStaticSemaphoreServerGrpc = Mockito.mockStatic(SemaphoreServerGrpc.class);
        mockedStaticSemaphoreServerGrpc.when(() -> SemaphoreServerGrpc.newBlockingStub(any(ManagedChannel.class))).
                thenReturn(stub);
        mockedStaticSemaphoreServerGrpc.when(() -> SemaphoreServerGrpc.newFutureStub(any(ManagedChannel.class))).
                thenReturn(futureStub);
        when(stub.currentPermits(any(StringValue.class))).thenAnswer(ioc -> Int32Value.of(current.get()));
        when(stub.acquire(any(NamePermits.class))).thenAnswer(ioc -> {
            NamePermits namePermits = ioc.getArgument(0,NamePermits.class);
            current.set( current.get() - namePermits.getPermits());
            return null;
        });
        when(futureStub.asyncAcquire(any(NamePermits.class))).thenAnswer(ioc -> {
            NamePermits namePermits = ioc.getArgument(0,NamePermits.class);
            current.set( current.get() - namePermits.getPermits());
            FakeListenableFuture<Empty> f = new FakeListenableFuture<>(Empty.newBuilder().build()).execute();
            listenableFutures.add(f);
            return f;
        });
        when(stub.tryAcquire(any(NamePermits.class))).thenAnswer(ioc -> {
            current.set( current.get() - ioc.getArgument(0, NamePermits.class).getPermits());
            return BoolValue.of(true);
        });
        when(stub.tryAcquireWithTimeOut(any(NamePermitsWithTimeout.class))).thenAnswer(ioc -> {
            current.set( current.get() - ioc.getArgument(0, NamePermitsWithTimeout.class).getPermits());
            return BoolValue.of(true);
        });
        when(stub.release(any(NamePermits.class))).thenAnswer(ioc -> {
            NamePermits namePermits = ioc.getArgument(0,NamePermits.class);
            current.set( current.get() + namePermits.getPermits());
            return null;
        });
        semaphoreClientGrpc = new SemaphoreClientGrpc(managedChannel, name);
    }

    @After
    public void tearsDown() {
        listenableFutures.forEach(FakeListenableFuture::close);
        mockedStaticSemaphoreServerGrpc.close();
        executorService.shutdown();
        executorService.shutdownNow();
    }

    int semaphoreInit() {
        int origin = ThreadLocalRandom.current().nextInt(7,10);
        current.set(origin);
        return origin;
    }

    @Test
    public void currentTest() {
        int origin = semaphoreInit();
        int currentValue = semaphoreClientGrpc.currentPermits();
        assertEquals(origin, current.get());
        assertEquals(origin, currentValue);
        verify(stub).currentPermits(any(StringValue.class));
    }

    @Test
    public void acquireTest() {
        int origin = semaphoreInit();
        semaphoreClientGrpc.acquire(2);
        assertEquals(origin - 2, current.get());
        assertEquals(origin - 2, semaphoreClientGrpc.currentPermits());
        verify(stub).acquire(any(NamePermits.class));
    }

    @Test
    public void asyncAcquireTest() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        int origin = semaphoreInit();
        semaphoreClientGrpc.asyncAcquire(3, executorService, () -> {
            inner.release();
        });
        boolean released = inner.tryAcquire(30, TimeUnit.SECONDS);
        assertTrue(released);
        assertEquals(origin - 3, current.get());
        assertEquals(origin - 3, semaphoreClientGrpc.currentPermits());
        verify(futureStub).asyncAcquire(any(NamePermits.class));
    }

    @Test
    public void tryAcquireOneTest() throws Exception {
        int origin = semaphoreInit();
        boolean response = semaphoreClientGrpc.tryAcquire();
        assertTrue(response);
        assertEquals(origin - 1, current.get());
        assertEquals(origin - 1, semaphoreClientGrpc.currentPermits());
        verify(stub).tryAcquire(any(NamePermits.class));
    }

    @Test
    public void tryAcquireTest() {
        int origin = semaphoreInit();
        boolean response = semaphoreClientGrpc.tryAcquire(2);
        assertTrue(response);
        assertEquals(origin - 2, current.get());
        assertEquals(origin - 2, semaphoreClientGrpc.currentPermits());
        verify(stub).tryAcquire(any(NamePermits.class));
    }

    @Test
    public void tryAcquireWithTimeOut1Test() {
        int origin = semaphoreInit();
        boolean response = semaphoreClientGrpc.tryAcquireWithTimeOut(3, 1, TimeUnit.SECONDS);
        assertTrue(response);
        assertEquals(origin - 3, current.get());
        assertEquals(origin - 3, semaphoreClientGrpc.currentPermits());
        verify(stub).tryAcquireWithTimeOut(any(NamePermitsWithTimeout.class));
    }

    @Test
    public void tryAcquireWithTimeOut2Test() throws Exception {
        int origin = semaphoreInit();
        boolean response = semaphoreClientGrpc.tryAcquireWithTimeOut(3, 1);
        assertTrue(response);
        assertEquals(origin - 3, current.get());
        assertEquals(origin - 3, semaphoreClientGrpc.currentPermits());
        verify(stub).tryAcquireWithTimeOut(any(NamePermitsWithTimeout.class));
    }

    @Test
    public void tryAcquireWithTimeOut3Test() throws Exception {
        int origin = semaphoreInit();
        boolean response = semaphoreClientGrpc.tryAcquireWithTimeOut( 1, TimeUnit.MILLISECONDS);
        assertTrue(response);
        assertEquals(origin - 1, current.get());
        assertEquals(origin - 1, semaphoreClientGrpc.currentPermits());
        verify(stub).tryAcquireWithTimeOut(any(NamePermitsWithTimeout.class));
    }

    @Test
    public void tryAcquireWithTimeOut4Test() throws Exception {
        int origin = semaphoreInit();
        boolean response = semaphoreClientGrpc.tryAcquireWithTimeOut( 1);
        assertTrue(response);
        assertEquals(origin - 1, current.get());
        assertEquals(origin - 1, semaphoreClientGrpc.currentPermits());
        verify(stub).tryAcquireWithTimeOut(any(NamePermitsWithTimeout.class));
    }

    @Test
    public void releaseTest() {
        int origin = semaphoreInit();
        semaphoreClientGrpc.release(2);
        assertEquals(origin + 2, current.get());
        assertEquals(origin + 2, semaphoreClientGrpc.currentPermits());
        verify(stub).release(any(NamePermits.class));
    }

}
