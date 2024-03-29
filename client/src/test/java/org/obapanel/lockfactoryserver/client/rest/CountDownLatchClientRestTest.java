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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CountDownLatchClientRestTest {


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

    private CountDownLatchClientRest countDownLatchClientRest;

    private final AtomicReference<HttpGet> finalRequest = new AtomicReference<>(null);

    private final AtomicReference<String> finalResult = new AtomicReference<>("");

    private final String name = "codola_" + System.currentTimeMillis();

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
        countDownLatchClientRest = new CountDownLatchClientRest("http://localhost:8080/", name);
    }

    private String finalUrl() {
        if (finalRequest.get() != null) {
            return finalRequest.get().getRequestUri().toString();
        } else {
            return "";
        }
    }

    @After
    public void tearsDown() {
        mockedStaticHttpClient.close();
        mockedStaticEntityUtils.close();
        mockedStaticHttpClientBuilder.close();
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
    public void countDown2Test() throws IOException {
        finalResult.set("ok");
        countDownLatchClientRest.countDown(3);
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("countDownLatch"));
        assertTrue(finalUrl.contains("countDown"));
        assertTrue(finalUrl.contains("3"));
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
    public void tryAwaitWithTimeou1tTest() throws IOException {
        long timeOut = ThreadLocalRandom.current().nextLong(10);
        finalResult.set(Boolean.toString(true));
        boolean result = countDownLatchClientRest.tryAwaitWithTimeOut(timeOut);
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("countDownLatch"));
        assertTrue(finalUrl.contains("tryAwaitWithTimeOut"));
        assertTrue(finalUrl.contains(name));
        assertTrue(finalUrl.contains(Long.toString(timeOut)));
        assertTrue(result);
        verify(httpclient).execute(any(HttpGet.class));
    }

    @Test
    public void tryAwaitWithTimeout2Test() throws IOException {
        long timeOut = ThreadLocalRandom.current().nextLong(10);
        finalResult.set(Boolean.toString(true));
        boolean result = countDownLatchClientRest.tryAwaitWithTimeOut(timeOut, TimeUnit.MILLISECONDS);
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("countDownLatch"));
        assertTrue(finalUrl.contains("tryAwaitWithTimeOut"));
        assertTrue(finalUrl.contains(name));
        assertTrue(finalUrl.contains(Long.toString(timeOut)));
        assertTrue(finalUrl.contains(TimeUnit.MILLISECONDS.name().toLowerCase()));
        assertTrue(result);
        verify(httpclient).execute(any(HttpGet.class));
    }

}
