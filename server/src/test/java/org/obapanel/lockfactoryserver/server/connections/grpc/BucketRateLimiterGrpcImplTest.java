package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.grpc.BucketRateLimiterNew;
import org.obapanel.lockfactoryserver.core.grpc.NameTokensConsume;
import org.obapanel.lockfactoryserver.core.grpc.NameTokensConsumeWithTimeOut;
import org.obapanel.lockfactoryserver.core.grpc.TimeUnitGrpc;
import org.obapanel.lockfactoryserver.server.FakeStreamObserver;
import org.obapanel.lockfactoryserver.server.service.rateLimiter.BucketRateLimiterService;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BucketRateLimiterGrpcImplTest {

    @Mock
    private BucketRateLimiterService bucketRateLimiterService;

    private BucketRateLimiterGrpcImpl bucketRateLimiterGrpcImpl;

    @Before
    public void setup()  {
        bucketRateLimiterGrpcImpl = new BucketRateLimiterGrpcImpl(bucketRateLimiterService);
    }

    @Test
    public void newRateLimiterTest() {
        String name = "burali1_" + System.currentTimeMillis();
        BucketRateLimiterNew bucketRateLimiterNew = BucketRateLimiterNew.newBuilder().
                setName(name).
                setTotalTokens(1L).
                setGreedy(true).
                setTimeRefill(10L).
                setTimeUnit(TimeUnitGrpc.SECONDS).
                build();
        FakeStreamObserver<Empty> responseObserver = new FakeStreamObserver<>();
        bucketRateLimiterGrpcImpl.newRateLimiter(bucketRateLimiterNew, responseObserver);
        verify(bucketRateLimiterService).newRateLimiter(eq(name), eq(1L), eq(true), eq(10L),
                eq(TimeUnit.SECONDS));
        assertTrue(responseObserver.isCompleted());
    }

    @Test
    public void getAvailableTokensTest() {
        long availableTokens = ThreadLocalRandom.current().nextLong(1000L);
        String name = "burali2_" + System.currentTimeMillis();
        FakeStreamObserver<Int64Value> responseObserver = new FakeStreamObserver<>();
        when(bucketRateLimiterService.getAvailableTokens(anyString())).thenReturn(availableTokens);
        bucketRateLimiterGrpcImpl.getAvailableTokens(StringValue.of(name), responseObserver);
        verify(bucketRateLimiterService).getAvailableTokens(eq(name));
        assertTrue(responseObserver.isCompleted());
        assertEquals(availableTokens, responseObserver.getNext().getValue());
    }

    @Test
    public void tryConsumeTest() {
        long tokens = ThreadLocalRandom.current().nextLong(1000L);
        String name = "burali3_" + System.currentTimeMillis();
        when(bucketRateLimiterService.tryConsume(anyString(), anyLong())).thenReturn(tokens % 2 == 0);
        NameTokensConsume nameTokensConsume = NameTokensConsume.newBuilder().
                setName(name).
                setTokens(tokens).
                build();
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        bucketRateLimiterGrpcImpl.tryConsume(nameTokensConsume, responseObserver);
        verify(bucketRateLimiterService).tryConsume(eq(name), eq(tokens));
        assertTrue(responseObserver.isCompleted());
        assertEquals(tokens % 2 == 0, responseObserver.getNext().getValue());
    }

    @Test
    public void tryConsumeWithTimeOutTest() {
        long tokens = ThreadLocalRandom.current().nextLong(1000L);
        String name = "burali4_" + System.currentTimeMillis();
        when(bucketRateLimiterService.tryConsumeWithTimeOut(anyString(), anyLong(), anyLong(), any(TimeUnit.class))).
                thenReturn(tokens % 2 == 0);
        NameTokensConsumeWithTimeOut nameTokensConsumeWithTimeOut = NameTokensConsumeWithTimeOut.newBuilder().
                setName(name).
                setTokens(tokens).
                setTimeOut(17L).
                setTimeUnit(TimeUnitGrpc.SECONDS).
                build();
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        bucketRateLimiterGrpcImpl.tryConsumeWithTimeOut(nameTokensConsumeWithTimeOut, responseObserver);
        verify(bucketRateLimiterService).tryConsumeWithTimeOut(eq(name), eq(tokens), eq(17L), eq(TimeUnit.SECONDS));
        assertTrue(responseObserver.isCompleted());
        assertEquals(tokens % 2 == 0, responseObserver.getNext().getValue());
    }

    @Test
    public void consumeTest() {
        long tokens = ThreadLocalRandom.current().nextLong(1000L);
        String name = "burali5_" + System.currentTimeMillis();
        NameTokensConsume nameTokensConsume = NameTokensConsume.newBuilder().
                setName(name).
                setTokens(tokens).
                build();
        FakeStreamObserver<Empty> responseObserver = new FakeStreamObserver<>();
        bucketRateLimiterGrpcImpl.consume(nameTokensConsume, responseObserver);
        verify(bucketRateLimiterService).consume(eq(name), eq(tokens));
        assertTrue(responseObserver.isCompleted());
    }

    @Test
    public void removeTest() {
        String name = "burali6_" + System.currentTimeMillis();
        FakeStreamObserver<Empty> responseObserver = new FakeStreamObserver<>();
        bucketRateLimiterGrpcImpl.remove(StringValue.of(name), responseObserver);
        verify(bucketRateLimiterService).remove(eq(name));
        assertTrue(responseObserver.isCompleted());
    }

}
