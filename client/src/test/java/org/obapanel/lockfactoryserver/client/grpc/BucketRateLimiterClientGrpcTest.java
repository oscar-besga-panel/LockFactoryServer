package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.grpc.BucketRateLimiterGrpc;
import org.obapanel.lockfactoryserver.core.grpc.BucketRateLimiterNew;
import org.obapanel.lockfactoryserver.core.grpc.NameTokensConsume;
import org.obapanel.lockfactoryserver.core.grpc.NameTokensConsumeWithTimeOut;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromJavaToGrpc;

@RunWith(MockitoJUnitRunner.class)
public class BucketRateLimiterClientGrpcTest {

    @Mock
    private ManagedChannel managedChannel;

    @Mock
    private BucketRateLimiterGrpc.BucketRateLimiterBlockingStub stub;

    @Mock
    private BucketRateLimiterGrpc.BucketRateLimiterFutureStub futureStub;

    private MockedStatic<BucketRateLimiterGrpc> mockedStaticBucketRateLimiterServerGrpc;


    private final String name = "burali" + System.currentTimeMillis();

    private final AtomicInteger current = new AtomicInteger(1);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final List<FakeListenableFuture<Empty>> listenableFutures = new ArrayList<>();

    private BucketRateLimiterClientGrpc bucketRateLimiterClientGrpc;


    @Before
    public void setup() {
        current.set(ThreadLocalRandom.current().nextInt(5,10));
        mockedStaticBucketRateLimiterServerGrpc = Mockito.mockStatic(BucketRateLimiterGrpc.class);
        mockedStaticBucketRateLimiterServerGrpc.when(() -> BucketRateLimiterGrpc.newBlockingStub(any(ManagedChannel.class))).
                thenReturn(stub);
        mockedStaticBucketRateLimiterServerGrpc.when(() -> BucketRateLimiterGrpc.newFutureStub(any(ManagedChannel.class))).
                thenReturn(futureStub);
        bucketRateLimiterClientGrpc = new BucketRateLimiterClientGrpc(managedChannel, name);

    }

    @After
    public void tearsDown() {
        listenableFutures.forEach(FakeListenableFuture::close);
        mockedStaticBucketRateLimiterServerGrpc.close();
        executorService.shutdown();
        executorService.shutdownNow();
        bucketRateLimiterClientGrpc.close();
    }

    @Test
    public void createNewTest1() {
        ArgumentCaptor<BucketRateLimiterNew> captor = ArgumentCaptor.forClass(BucketRateLimiterNew.class);
        bucketRateLimiterClientGrpc.newRateLimiter(current.get(), false, 10L, TimeUnit.SECONDS);
        verify(stub).newRateLimiter(captor.capture());
        assertEquals(name, captor.getValue().getName());
        assertEquals(current.get(), captor.getValue().getTotalTokens());
        assertFalse(captor.getValue().getGreedy());
        assertEquals(10L, captor.getValue().getTimeRefill());
        assertEquals(fromJavaToGrpc(TimeUnit.SECONDS), captor.getValue().getTimeUnit());
    }

    @Test
    public void createNewTest2() {
        ArgumentCaptor<BucketRateLimiterNew> captor = ArgumentCaptor.forClass(BucketRateLimiterNew.class);
        bucketRateLimiterClientGrpc.newRateLimiter(current.get(), false, 10L);
        verify(stub).newRateLimiter(captor.capture());
        assertEquals(name, captor.getValue().getName());
        assertEquals(current.get(), captor.getValue().getTotalTokens());
        assertFalse(captor.getValue().getGreedy());
        assertEquals(10L, captor.getValue().getTimeRefill());
        assertEquals(fromJavaToGrpc(TimeUnit.MILLISECONDS), captor.getValue().getTimeUnit());
    }

    @Test
    public void getAvailableTokensTest() {
        when(stub.getAvailableTokens(any(StringValue.class))).thenReturn(Int64Value.of(current.get()));
        ArgumentCaptor<StringValue> captor = ArgumentCaptor.forClass(StringValue.class);
        long result = bucketRateLimiterClientGrpc.getAvailableTokens();
        verify(stub).getAvailableTokens(captor.capture());
        assertEquals(name, captor.getValue().getValue());
        assertEquals(current.get(), result);
    }

    @Test
    public void tryConsumeTest() {
        long tokens = ThreadLocalRandom.current().nextLong(3);
        when(stub.tryConsume(any(NameTokensConsume.class))).thenReturn(BoolValue.of(current.get() % 2 == 0));
        ArgumentCaptor<NameTokensConsume> captor = ArgumentCaptor.forClass(NameTokensConsume.class);
        boolean result = bucketRateLimiterClientGrpc.tryConsume(tokens);
        verify(stub).tryConsume(captor.capture());
        assertEquals(name, captor.getValue().getName());
        assertEquals(tokens, captor.getValue().getTokens());
        assertEquals(current.get() % 2 == 0, result);
    }

    @Test
    public void tryConsumeWithTimeOut1() {
        long tokens = ThreadLocalRandom.current().nextLong(3);
        when(stub.tryConsumeWithTimeOut(any(NameTokensConsumeWithTimeOut.class))).thenReturn(BoolValue.of(current.get() % 2 == 0));
        ArgumentCaptor<NameTokensConsumeWithTimeOut> captor = ArgumentCaptor.forClass(NameTokensConsumeWithTimeOut.class);
        boolean result = bucketRateLimiterClientGrpc.tryConsumeWithTimeOut(tokens, 435L, TimeUnit.SECONDS);
        verify(stub).tryConsumeWithTimeOut(captor.capture());
        assertEquals(name, captor.getValue().getName());
        assertEquals(tokens, captor.getValue().getTokens());
        assertEquals(435L, captor.getValue().getTimeOut());
        assertEquals(fromJavaToGrpc(TimeUnit.SECONDS), captor.getValue().getTimeUnit());
        assertEquals(current.get() % 2 == 0, result);
    }

    @Test
    public void tryConsumeWithTimeOut2() {
        long tokens = ThreadLocalRandom.current().nextLong(3);
        when(stub.tryConsumeWithTimeOut(any(NameTokensConsumeWithTimeOut.class))).thenReturn(BoolValue.of(current.get() % 2 == 0));
        ArgumentCaptor<NameTokensConsumeWithTimeOut> captor = ArgumentCaptor.forClass(NameTokensConsumeWithTimeOut.class);
        boolean result = bucketRateLimiterClientGrpc.tryConsumeWithTimeOut(tokens, 435L);
        verify(stub).tryConsumeWithTimeOut(captor.capture());
        assertEquals(name, captor.getValue().getName());
        assertEquals(tokens, captor.getValue().getTokens());
        assertEquals(435L, captor.getValue().getTimeOut());
        assertEquals(fromJavaToGrpc(TimeUnit.MILLISECONDS), captor.getValue().getTimeUnit());
        assertEquals(current.get() % 2 == 0, result);
    }

    @Test
    public void consumeTest() {
        long tokens = ThreadLocalRandom.current().nextLong(3);
        when(stub.consume(any(NameTokensConsume.class))).thenReturn(Empty.getDefaultInstance());
        ArgumentCaptor<NameTokensConsume> captor = ArgumentCaptor.forClass(NameTokensConsume.class);
        bucketRateLimiterClientGrpc.consume(tokens);
        verify(stub).consume(captor.capture());
        assertEquals(name, captor.getValue().getName());
        assertEquals(tokens, captor.getValue().getTokens());
    }

    @Test
    public void asyncConsumeTest() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        long tokens = ThreadLocalRandom.current().nextLong(3);
        when(futureStub.asyncConsume(any(NameTokensConsume.class))).thenAnswer(ioc -> {
            inner.release();
            FakeListenableFuture<Empty> f = new FakeListenableFuture<>(Empty.newBuilder().build()).execute();
            listenableFutures.add(f);
            return f;
        });
        ArgumentCaptor<NameTokensConsume> captor = ArgumentCaptor.forClass(NameTokensConsume.class);
        bucketRateLimiterClientGrpc.asyncConsume(tokens, inner::release);
        boolean released = inner.tryAcquire(2, 1000, TimeUnit.MILLISECONDS);
        verify(futureStub).asyncConsume(captor.capture());
        assertEquals(name, captor.getValue().getName());
        assertTrue(released);
        assertEquals(tokens, captor.getValue().getTokens());
    }

    @Test
    public void removeTest() {
        when(stub.remove(any(StringValue.class))).thenReturn(Empty.getDefaultInstance());
        ArgumentCaptor<StringValue> captor = ArgumentCaptor.forClass(StringValue.class);
        bucketRateLimiterClientGrpc.remove();
        verify(stub).remove(captor.capture());
        assertEquals(name, captor.getValue().getValue());
    }

}
