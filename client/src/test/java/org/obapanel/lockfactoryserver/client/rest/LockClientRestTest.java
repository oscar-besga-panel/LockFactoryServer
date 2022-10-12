package org.obapanel.lockfactoryserver.client.rest;


import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LockClientRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockClientRestTest.class);

    @Mock
    private CloseableHttpClient httpclient;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private HttpEntity httpEntity;

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
        when(httpclient.execute(any(HttpGet.class))).thenAnswer(ioc ->{
            finalRequest.set(ioc.getArgument(0));
            return httpResponse;
        });
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        mockedStaticEntityUtils = Mockito.mockStatic(EntityUtils.class);
        mockedStaticEntityUtils.when(() -> EntityUtils.toString(eq(httpEntity))).
                thenAnswer(ioc -> finalResult.toString());
        StatusLine statusLine = mock(StatusLine.class);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        lockClientRest = new LockClientRest("http://localhost:8080/", name);
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
    public void tryLock2Test() throws IOException {
        int time = ThreadLocalRandom.current().nextInt(10,30);
        finalResult.set("token_" + name);
        boolean result = lockClientRest.tryLock(time, TimeUnit.MILLISECONDS);
        assertTrue(result);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("lock"));
        assertTrue(finalUrl.contains("tryLock"));
        assertTrue(finalUrl.contains(Integer.toString(time)));
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
