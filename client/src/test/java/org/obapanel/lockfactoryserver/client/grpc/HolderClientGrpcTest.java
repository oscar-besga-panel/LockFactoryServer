package org.obapanel.lockfactoryserver.client.grpc;

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
import org.obapanel.lockfactoryserver.core.grpc.HolderNameWithTimeOut;
import org.obapanel.lockfactoryserver.core.grpc.HolderResultGrpc;
import org.obapanel.lockfactoryserver.core.grpc.HolderResultStatusGrpc;
import org.obapanel.lockfactoryserver.core.grpc.HolderServerGrpc;
import org.obapanel.lockfactoryserver.core.grpc.HolderSet;
import org.obapanel.lockfactoryserver.core.grpc.HolderSetWithTimeToLive;
import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromJavaToGrpc;

@RunWith(MockitoJUnitRunner.class)
public class HolderClientGrpcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HolderClientGrpcTest.class);

    @Mock
    private ManagedChannel managedChannel;

    @Mock
    private HolderServerGrpc.HolderServerBlockingStub stub;

    @Mock
    private HolderServerGrpc.HolderServerFutureStub futureStub;


    private MockedStatic<HolderServerGrpc> mockedStaticHolderServerGrpc;

    private HolderClientGrpc holderClientGrpc;

    private final String name = "holder_" + System.currentTimeMillis();

    private final String value = "value_" + name;

    private final HolderResultGrpc result = HolderResultGrpc.newBuilder().
            setStatus(HolderResultStatusGrpc.RETRIEVED).
            setValue(value).
            build();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final List<FakeListenableFuture> listenableFutures = new ArrayList<>();


    @Before
    public void setup() {
        mockedStaticHolderServerGrpc = Mockito.mockStatic(HolderServerGrpc.class);
        mockedStaticHolderServerGrpc.when(() -> HolderServerGrpc.newBlockingStub(any(ManagedChannel.class))).
                thenReturn(stub);
        mockedStaticHolderServerGrpc.when(() -> HolderServerGrpc.newFutureStub(any(ManagedChannel.class))).
                thenReturn(futureStub);
        when(stub.get(any(StringValue.class))).thenReturn(result);
        when(futureStub.get(any(StringValue.class))).thenAnswer(ioc -> {
            FakeListenableFuture<HolderResultGrpc> f = new FakeListenableFuture<>(result).execute();
            listenableFutures.add(f);
            return f;
        });
        when(stub.getIfAvailable(any(StringValue.class))).thenReturn(result);
        when(stub.getWithTimeOut(any(HolderNameWithTimeOut.class))).thenReturn(result);
        holderClientGrpc = new HolderClientGrpc(managedChannel, name);
    }

    @After
    public void tearsDown() {
        listenableFutures.forEach(FakeListenableFuture::close);
        mockedStaticHolderServerGrpc.close();
        executorService.shutdown();
        executorService.shutdownNow();
    }

    @Test
    public void getTest() {
        HolderResult holderResult = holderClientGrpc.get();
        verify(stub).get(any(StringValue.class));
        assertTrue(holderResult.getValue().contains(name));
        assertEquals(HolderResult.Status.RETRIEVED, holderResult.getStatus());
        assertEquals(holderResult, holderClientGrpc.getResult());
    }

    @Test
    public void asyncGetTest() throws InterruptedException {
        Semaphore sem = new Semaphore(0);
        AtomicReference<HolderResult> holderResult = new AtomicReference<>();
        holderClientGrpc.asyncGet(hr -> {
            LOGGER.debug("holderClientGrpc.asyncGet consumer");
            holderResult.set(hr);
            sem.release();
        });
        assertTrue(sem.tryAcquire(1500, TimeUnit.MILLISECONDS));
        assertNotNull(holderResult.get());
        verify(futureStub).get(any(StringValue.class));
        assertTrue(holderResult.get().getValue().contains(name));
        assertEquals(HolderResult.Status.RETRIEVED, holderResult.get().getStatus());
        assertEquals(holderResult.get(), holderClientGrpc.getResult());
    }


    @Test
    public void getIfAvailableTest() {
        HolderResult holderResult = holderClientGrpc.getIfAvailable();
        verify(stub).getIfAvailable(any(StringValue.class));
        assertTrue(holderResult.getValue().contains(name));
        assertEquals(HolderResult.Status.RETRIEVED, holderResult.getStatus());
        assertEquals(holderResult, holderClientGrpc.getResult());
    }

    @Test
    public void getWithTimeOutTest() {
        HolderResult holderResult = holderClientGrpc.getWithTimeOut(1000);
        verify(stub).getWithTimeOut(any(HolderNameWithTimeOut.class));
        assertTrue(holderResult.getValue().contains(name));
        assertEquals(HolderResult.Status.RETRIEVED, holderResult.getStatus());
        assertEquals(holderResult, holderClientGrpc.getResult());
    }

    @Test
    public void setTest() {
        ArgumentCaptor<HolderSet> captor = ArgumentCaptor.forClass(HolderSet.class);
        holderClientGrpc.set("value_" + name);
        verify(stub).set(captor.capture());
        assertEquals(name, captor.getValue().getName());
        assertEquals("value_" + name, captor.getValue().getNewValue());
    }

    @Test
    public void setWithTimeToLiveTest() {
        ArgumentCaptor<HolderSetWithTimeToLive> captor = ArgumentCaptor.forClass(HolderSetWithTimeToLive.class);
        holderClientGrpc.setWithTimeToLive("value_" + name, 1200L);
        verify(stub).setWithTimeToLive(captor.capture());
        assertEquals(name, captor.getValue().getName());
        assertEquals("value_" + name, captor.getValue().getNewValue());
        assertEquals(1200L, captor.getValue().getTimeToLive());
        assertEquals(fromJavaToGrpc(TimeUnit.MILLISECONDS), captor.getValue().getTimeUnit());
    }

    @Test
    public void cancelTest() {
        ArgumentCaptor<StringValue> captor = ArgumentCaptor.forClass(StringValue.class);
        holderClientGrpc.cancel();
        verify(stub).cancel(captor.capture());
        assertEquals(name, captor.getValue().getValue());
        assertEquals(holderClientGrpc.getName(), captor.getValue().getValue());
        assertNull(holderClientGrpc.getResult());
    }


}
