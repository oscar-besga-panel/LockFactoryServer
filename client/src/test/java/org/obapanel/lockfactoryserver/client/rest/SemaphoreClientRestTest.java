package org.obapanel.lockfactoryserver.client.rest;


import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.StatusLine;
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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SemaphoreClientRestTest {

    @Mock
    private HttpClientBuilder httpClientBuilder;

    @Mock
    private CloseableHttpClient httpclient;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private HttpEntity httpEntity;

    private MockedStatic<HttpClientBuilder> mockedStaticHttpClientBuilder;

    private MockedStatic<HttpClients> mockedStaticHttpClient;

    private MockedStatic<EntityUtils> mockedStaticEntityUtils;

    private SemaphoreClientRest semaphoreClientRest;

    private final AtomicReference<HttpGet> finalRequest = new AtomicReference<>(null);

    private final AtomicReference<String> finalResult = new AtomicReference<>("");

    private final String name = "sem" + System.currentTimeMillis();

    @Before
    public void setup() throws IOException {
        mockedStaticHttpClient = Mockito.mockStatic(HttpClients.class);
        mockedStaticHttpClient.when(() -> HttpClients.createDefault() ).thenReturn(httpclient);
        mockedStaticHttpClientBuilder = Mockito.mockStatic(HttpClientBuilder.class);
        mockedStaticHttpClientBuilder.when(() -> HttpClientBuilder.create() ).thenReturn(httpClientBuilder);
        when(httpClientBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(httpclient);
        when(httpclient.execute(any(HttpGet.class))).thenAnswer(ioc ->{
            finalRequest.set(ioc.getArgument(0));
            return httpResponse;
        });
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        mockedStaticEntityUtils = Mockito.mockStatic(EntityUtils.class);
        mockedStaticEntityUtils.when(() -> EntityUtils.toString(eq(httpEntity))).
                thenAnswer(ioc -> finalResult.toString());
        StatusLine statusLine = mock(StatusLine.class);
        when(httpResponse.getCode()).thenReturn(200);
        semaphoreClientRest = new SemaphoreClientRest("http://localhost:8080/", name);
    }

    private String finalUrl() {
        if (finalRequest.get() != null) {
            return finalRequest.get().getRequestUri().toString();
        } else {
            return "";
        }
    }

    String semaphoreInit() {
        int origin = ThreadLocalRandom.current().nextInt(7,10);
        String result = Integer.toString(origin);
        finalResult.set(result);
        return result;
    }

    @After
    public void tearsDown() {
        mockedStaticHttpClient.close();
        mockedStaticEntityUtils.close();
        mockedStaticHttpClientBuilder.close();
    }

    @Test
    public void currentTest() throws IOException {
        String result = semaphoreInit();
        int currentPermits = semaphoreClientRest.currentPermits();
        assertEquals(result, Integer.toString(currentPermits));
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("semaphore"));
        assertTrue(finalUrl.contains("currentPermits"));
        assertTrue(finalUrl.contains(name));
    }

    @Test
    public void acquireTest() throws IOException {
        semaphoreClientRest.acquire(2);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("semaphore"));
        assertTrue(finalUrl.contains("acquire"));
        assertTrue(finalUrl.contains("2"));
        assertTrue(finalUrl.contains(name));
    }

    @Test
    public void tryAcquireTest() throws IOException {
        boolean result = semaphoreClientRest.tryAcquire(3);
        assertFalse(result);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("semaphore"));
        assertTrue(finalUrl.contains("tryAcquire"));
        assertTrue(finalUrl.contains("3"));
        assertTrue(finalUrl.contains(name));
    }

    @Test
    public void tryAcquireWithTimeOut1Test() throws Exception {
        boolean result = semaphoreClientRest.tryAcquireWithTimeOut(7);
        assertFalse(result);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("semaphore"));
        assertTrue(finalUrl.contains("tryAcquire"));
        assertTrue(finalUrl.contains("1"));
        assertTrue(finalUrl.contains("7"));
        assertTrue(finalUrl.contains("milliseconds"));
        assertTrue(finalUrl.contains(name));
    }

    @Test
    public void tryAcquireWithTimeOut2Test() throws Exception {
        boolean result = semaphoreClientRest.tryAcquireWithTimeOut(7, TimeUnit.SECONDS);
        assertFalse(result);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("semaphore"));
        assertTrue(finalUrl.contains("tryAcquire"));
        assertTrue(finalUrl.contains("1"));
        assertTrue(finalUrl.contains("7"));
        assertTrue(finalUrl.contains("seconds"));
        assertTrue(finalUrl.contains(name));
    }


    @Test
    public void tryAcquireWithTimeOut3Test() throws IOException {
        boolean result = semaphoreClientRest.tryAcquireWithTimeOut(5, 7);
        assertFalse(result);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("semaphore"));
        assertTrue(finalUrl.contains("tryAcquire"));
        assertTrue(finalUrl.contains("5"));
        assertTrue(finalUrl.contains("7"));
        assertTrue(finalUrl.contains("milliseconds"));
        assertTrue(finalUrl.contains(name));
    }

    @Test
    public void tryAcquireWithTimeOut4Test() throws IOException {
        boolean result = semaphoreClientRest.tryAcquireWithTimeOut(5, 7, TimeUnit.SECONDS);
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
        semaphoreClientRest.release(9);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("semaphore"));
        assertTrue(finalUrl.contains("release"));
        assertTrue(finalUrl.contains("9"));
        assertTrue(finalUrl.contains(name));
    }

}
