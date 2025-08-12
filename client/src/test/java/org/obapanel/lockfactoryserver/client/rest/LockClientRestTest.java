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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LockClientRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockClientRestTest.class);

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

    private LockClientRest lockClientRest;

    private final AtomicReference<HttpGet> finalRequest = new AtomicReference<>(null);

    private final AtomicReference<String> finalResult = new AtomicReference<>("");

    private final String name = "lock" + System.currentTimeMillis();

    @Before
    public void setup() throws IOException {
        mockedStaticHttpClient = Mockito.mockStatic(HttpClients.class);
        mockedStaticHttpClient.when(HttpClients::createDefault).thenReturn(httpclient);
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
        lockClientRest = new LockClientRest("http://localhost:8080/", name);
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
    public void lockTest() throws IOException {
        finalResult.set("token_" + name);
        boolean result = lockClientRest.lock();
        assertTrue(result);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("lock")); //twice
        assertTrue(finalUrl.contains(name));
    }

    @Test
    public void tryLock1Test() throws IOException {
        finalResult.set("token_" + name);
        boolean result = lockClientRest.tryLock();
        assertTrue(result);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("lock"));
        assertTrue(finalUrl.contains("tryLock"));
        assertTrue(finalUrl.contains(name));
    }

    @Test
    public void tryLockWithTimeout2Test() throws IOException {
        int time = ThreadLocalRandom.current().nextInt(10,30);
        finalResult.set("token_" + name);
        boolean result = lockClientRest.tryLockWithTimeout(time, TimeUnit.SECONDS);
        assertTrue(result);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("lock"));
        assertTrue(finalUrl.contains("tryLock"));
        assertTrue(finalUrl.contains(Integer.toString(time)));
        assertTrue(finalUrl.contains(TimeUnit.SECONDS.name().toLowerCase()));
        assertTrue(finalUrl.contains(name));
    }

    @Test
    public void tryLockWithTimeout3Test() throws IOException {
        int timeMillis = ThreadLocalRandom.current().nextInt(10,30);
        finalResult.set("token_" + name);
        boolean result = lockClientRest.tryLockWithTimeout(timeMillis);
        assertTrue(result);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("lock"));
        assertTrue(finalUrl.contains("tryLock"));
        assertTrue(finalUrl.contains(Integer.toString(timeMillis)));
        assertTrue(finalUrl.contains(TimeUnit.MILLISECONDS.name().toLowerCase()));
        assertTrue(finalUrl.contains(name));
    }

    @Test
    public void isLockedTest() throws IOException {
        finalResult.set("true");
        boolean result = lockClientRest.isLocked();
        assertTrue(result);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("lock"));
        assertTrue(finalUrl.contains("isLocked"));
        assertTrue(finalUrl.contains(name));
    }

    @Test
    public void unlockTest() throws IOException {
        finalResult.set("true");
        boolean result = lockClientRest.unLock();
        assertTrue(result);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("lock"));
        assertTrue(finalUrl.contains("unlock"));
        assertTrue(finalUrl.contains(name));
    }

    @Test
    public void doWithLockTest() throws Exception {
        finalResult.set("true");
        lockClientRest.doWithinLock(() -> LOGGER.debug("doWithLock"));
        verify(httpclient, times(2)).execute(any(HttpGet.class));
    }

    @Test
    public void doGetWithLockTest() throws Exception {
        finalResult.set("true");
        lockClientRest.doGetWithinLock(() -> {
            LOGGER.debug("doWithLock");
            return "";
        });
        verify(httpclient, times(2)).execute(any(HttpGet.class));
    }

}
