package org.obapanel.lockfactoryserver.client.rest;


import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CountDownLatchClientRestTest {

    @Mock
    private CloseableHttpClient httpclient;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private HttpEntity httpEntity;

    private MockedStatic<HttpClients> mockedStaticHttpClient;

    private MockedStatic<EntityUtils> mockedStaticEntityUtils;

    private CountDownLatchClientRest countDownLatchClientRest;

    private final AtomicReference<HttpGet> finalRequest = new AtomicReference<>(null);

    private final AtomicReference<String> finalResult = new AtomicReference<>("");

    private final String name = "codola_" + System.currentTimeMillis();

    @Before
    public void setup() throws IOException {
        mockedStaticHttpClient = Mockito.mockStatic(HttpClients.class);
        mockedStaticHttpClient.when(() -> HttpClients.createDefault() ).thenReturn(httpclient);
        when(httpclient.execute(any(HttpGet.class))).thenAnswer(ioc ->{
            finalRequest.set(ioc.getArgument(0));
            return httpResponse;
        });
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        mockedStaticEntityUtils = Mockito.mockStatic(EntityUtils.class);
        mockedStaticEntityUtils.when(() -> EntityUtils.toString(eq(httpEntity))).
                thenAnswer(ioc -> finalResult.toString());
        countDownLatchClientRest = new CountDownLatchClientRest("http://localhost:8080/", name);
    }

    private String finalUrl() {
        if (finalRequest.get() != null) {
            return finalRequest.get().getURI().toString();
        } else {
            return "";
        }
    }



    @After
    public void tearsDown() {
        mockedStaticHttpClient.close();
        mockedStaticEntityUtils.close();
    }

    @Test
    public void createNewTest() throws IOException {
        finalResult.set("true");
        int count = ThreadLocalRandom.current().nextInt(10);
        boolean result = countDownLatchClientRest.createNew(count);
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("countDownLatch"));
        assertTrue(finalUrl.contains("createNew"));
        assertTrue(finalUrl.contains(name));
        assertTrue(finalUrl.contains(Integer.toString(count)));
        assertTrue(result);
        verify(httpclient).execute(any(HttpGet.class));
    }

    @Test
    public void getCountTest() throws IOException {
        int count = ThreadLocalRandom.current().nextInt(10);
        finalResult.set(Integer.toString(count));
        int result = countDownLatchClientRest.getCount();
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("countDownLatch"));
        assertTrue(finalUrl.contains("getCount"));
        assertTrue(finalUrl.contains(name));
        assertEquals(count, result);
        verify(httpclient).execute(any(HttpGet.class));
    }

    @Test
    public void isActive() throws IOException {
        int count = ThreadLocalRandom.current().nextInt(3, 10);
        finalResult.set(Integer.toString(count));
        boolean result = countDownLatchClientRest.isActive();
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("countDownLatch"));
        assertTrue(finalUrl.contains("getCount"));
        assertTrue(finalUrl.contains(name));
        assertTrue(result);
        verify(httpclient).execute(any(HttpGet.class));
    }

    @Test
    public void countDownTest() throws IOException {
        finalResult.set("ok");
        countDownLatchClientRest.countDown();
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("countDownLatch"));
        assertTrue(finalUrl.contains("countDown"));
        assertTrue(finalUrl.contains(name));
        verify(httpclient).execute(any(HttpGet.class));
    }

    @Test
    public void awaitTest() throws IOException {
        finalResult.set("ok");
        countDownLatchClientRest.await();
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("countDownLatch"));
        assertTrue(finalUrl.contains("await"));
        assertTrue(finalUrl.contains(name));
        verify(httpclient).execute(any(HttpGet.class));
    }

    @Test
    public void awaitWithTimeoutTest() throws IOException {
        long timeOut = ThreadLocalRandom.current().nextLong(10);
        finalResult.set(Boolean.toString(true));
        boolean result = countDownLatchClientRest.await(timeOut, TimeUnit.MILLISECONDS);
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("countDownLatch"));
        assertTrue(finalUrl.contains("await"));
        assertTrue(finalUrl.contains(name));
        assertTrue(finalUrl.contains(Long.toString(timeOut)));
        assertTrue(finalUrl.contains(TimeUnit.MILLISECONDS.name().toLowerCase()));
        assertTrue(result);
        verify(httpclient).execute(any(HttpGet.class));
    }


/*
    String semaphoreInit() {
        int origin = ThreadLocalRandom.current().nextInt(7,10);
        String result = Integer.toString(origin);
        finalResult.set(result);
        return result;
    }

    @Test
    public void currentTest() throws IOException {
        String result = semaphoreInit();
        int currentPermits = countDownLatchClientRest.currentPermits();
        assertEquals(result, Integer.toString(currentPermits));
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("semaphore"));
        assertTrue(finalUrl.contains("currentPermits"));
        assertTrue(finalUrl.contains(name));
    }

    @Test
    public void acquireTest() throws IOException {
        countDownLatchClientRest.acquire(2);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("semaphore"));
        assertTrue(finalUrl.contains("acquire"));
        assertTrue(finalUrl.contains("2"));
        assertTrue(finalUrl.contains(name));
    }

    @Test
    public void tryAcquireTest() throws IOException {
        boolean result = countDownLatchClientRest.tryAcquire(3);
        assertFalse(result);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("semaphore"));
        assertTrue(finalUrl.contains("tryAcquire"));
        assertTrue(finalUrl.contains("3"));
        assertTrue(finalUrl.contains(name));
    }

    @Test
    public void tryAcquireWithTimeOutTest() throws IOException {
        boolean result = countDownLatchClientRest.tryAcquire(5, 7, TimeUnit.SECONDS);
        assertFalse(result);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("semaphore"));
        assertTrue(finalUrl.contains("tryAcquire"));
        assertTrue(finalUrl.contains("5"));
        assertTrue(finalUrl.contains("7"));
        assertTrue(finalUrl.contains("seconds"));
        assertTrue(finalUrl.contains(name));
    }

    @Test
    public void releaseTest() throws IOException {
        countDownLatchClientRest.release(9);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("semaphore"));
        assertTrue(finalUrl.contains("release"));
        assertTrue(finalUrl.contains("9"));
        assertTrue(finalUrl.contains(name));
    }

 */

}
