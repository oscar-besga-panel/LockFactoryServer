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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BucketRateLimiterRestTest {

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

    private BucketRateLimiterRestClient bucketRateLimiterRestClient;

    private final AtomicReference<HttpGet> finalRequest = new AtomicReference<>(null);

    private final AtomicReference<String> finalResult = new AtomicReference<>("");

    private final String name = "burali_" + System.currentTimeMillis();

    @Before
    public void setup() throws IOException {
        mockedStaticHttpClient = Mockito.mockStatic(HttpClients.class);
        mockedStaticHttpClient.when(HttpClients::createDefault).thenReturn(httpclient);
        mockedStaticHttpClientBuilder = Mockito.mockStatic(HttpClientBuilder.class);
        mockedStaticHttpClientBuilder.when(HttpClientBuilder::create).thenReturn(httpClientBuilder);
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
        bucketRateLimiterRestClient = new BucketRateLimiterRestClient("http://localhost:8080/", name);
    }

    private String finalUrl() {
        if (finalRequest.get() != null) {
            return finalRequest.get().getRequestUri();
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
    public void newRateLimiterTest1() throws IOException {
        long tokens = ThreadLocalRandom.current().nextLong(5,50);
        finalResult.set("ok");
        bucketRateLimiterRestClient.newRateLimiter(tokens, true, 117L, TimeUnit.SECONDS);
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("bucketRateLimiter"));
        assertTrue(finalUrl.contains("newRateLimiter"));
        assertTrue(finalUrl.contains(name));
        assertTrue(finalUrl.contains(Long.toString(tokens)));
        assertTrue(finalUrl.contains(Long.toString(tokens)));
        assertTrue(finalUrl.contains(Boolean.toString(true)));
        assertTrue(finalUrl.contains(TimeUnit.SECONDS.toString()));
        verify(httpclient).execute(any(HttpGet.class));
    }

    @Test
    public void newRateLimiterTest2() throws IOException {
        long tokens = ThreadLocalRandom.current().nextLong(5,50);
        finalResult.set("ok");
        bucketRateLimiterRestClient.newRateLimiter(tokens, true, 117L);
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("bucketRateLimiter"));
        assertTrue(finalUrl.contains("newRateLimiter"));
        assertTrue(finalUrl.contains(name));
        assertTrue(finalUrl.contains(Long.toString(tokens)));
        assertTrue(finalUrl.contains(Long.toString(tokens)));
        assertTrue(finalUrl.contains(Boolean.toString(true)));
        assertTrue(finalUrl.contains(TimeUnit.MILLISECONDS.toString()));
        verify(httpclient).execute(any(HttpGet.class));
    }

    @Test
    public void getAvailableTokensTest() throws IOException {
        long tokens = ThreadLocalRandom.current().nextLong(5,50);
        finalResult.set(Long.toString(tokens));
        long result = bucketRateLimiterRestClient.getAvailableTokens();
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("bucketRateLimiter"));
        assertTrue(finalUrl.contains("getAvailableTokens"));
        assertTrue(finalUrl.contains(name));
        verify(httpclient).execute(any(HttpGet.class));
        assertEquals(tokens, result);
    }

    @Test
    public void tryConsumeTest() throws IOException {
        long tokens = ThreadLocalRandom.current().nextLong(5,50);
        finalResult.set("true");
        boolean result = bucketRateLimiterRestClient.tryConsume(tokens);
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("bucketRateLimiter"));
        assertTrue(finalUrl.contains("tryConsume"));
        assertTrue(finalUrl.contains(name));
        assertTrue(finalUrl.contains(Long.toString(tokens)));
        verify(httpclient).execute(any(HttpGet.class));
        assertTrue(result);
    }

    @Test
    public void tryConsume1Test() throws IOException {
        finalResult.set("true");
        boolean result = bucketRateLimiterRestClient.tryConsume();
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("bucketRateLimiter"));
        assertTrue(finalUrl.contains("tryConsume"));
        assertTrue(finalUrl.contains(name));
        assertTrue(finalUrl.contains(Long.toString(1L)));
        verify(httpclient).execute(any(HttpGet.class));
        assertTrue(result);
    }

    @Test
    public void tryConsumeWithTimeOutTest1() throws IOException {
        long tokens = ThreadLocalRandom.current().nextLong(5,50);
        finalResult.set("false");
        boolean result = bucketRateLimiterRestClient.tryConsumeWithTimeOut(tokens, 117L, TimeUnit.MICROSECONDS);
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("bucketRateLimiter"));
        assertTrue(finalUrl.contains("tryConsumeWithTimeOut"));
        assertTrue(finalUrl.contains(name));
        assertTrue(finalUrl.contains(Long.toString(tokens)));
        assertTrue(finalUrl.contains(Long.toString(117L)));
        assertTrue(finalUrl.contains(TimeUnit.MICROSECONDS.toString()));
        verify(httpclient).execute(any(HttpGet.class));
        assertFalse(result);
    }

    @Test
    public void tryConsumeWithTimeOutTest2() throws IOException {
        long tokens = ThreadLocalRandom.current().nextLong(5,50);
        finalResult.set("false");
        boolean result = bucketRateLimiterRestClient.tryConsumeWithTimeOut(tokens, 117L);
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("bucketRateLimiter"));
        assertTrue(finalUrl.contains("tryConsumeWithTimeOut"));
        assertTrue(finalUrl.contains(name));
        assertTrue(finalUrl.contains(Long.toString(tokens)));
        assertTrue(finalUrl.contains(Long.toString(117L)));
        assertTrue(finalUrl.contains(TimeUnit.MILLISECONDS.toString()));
        verify(httpclient).execute(any(HttpGet.class));
        assertFalse(result);
    }

    @Test
    public void consumeTest() throws IOException {
        long tokens = ThreadLocalRandom.current().nextLong(5,50);
        finalResult.set("ok");
        bucketRateLimiterRestClient.consume(tokens);
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("bucketRateLimiter"));
        assertTrue(finalUrl.contains("consume"));
        assertTrue(finalUrl.contains(name));
        assertTrue(finalUrl.contains(Long.toString(tokens)));
        verify(httpclient).execute(any(HttpGet.class));
    }

    @Test
    public void consume1Test() throws IOException {
        finalResult.set("ok");
        bucketRateLimiterRestClient.consume();
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("bucketRateLimiter"));
        assertTrue(finalUrl.contains("consume"));
        assertTrue(finalUrl.contains(name));
        assertTrue(finalUrl.contains(Long.toString(1L)));
        verify(httpclient).execute(any(HttpGet.class));
    }

    @Test
    public void removeTest() throws IOException {
        finalResult.set("ok");
        bucketRateLimiterRestClient.remove();
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("bucketRateLimiter"));
        assertTrue(finalUrl.contains("remove"));
        assertTrue(finalUrl.contains(name));
        verify(httpclient).execute(any(HttpGet.class));
    }


}
