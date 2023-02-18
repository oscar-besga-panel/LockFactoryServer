package org.obapanel.lockfactoryserver.client.rest;


import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
import org.obapanel.lockfactoryserver.core.holder.HolderResult;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HolderClientRestTest {


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

    private HolderClientRest holderClientRest;

    private final AtomicReference<HttpGet> finalRequest = new AtomicReference<>(null);

    private final AtomicReference<Object> finalResult = new AtomicReference<>("");

    private final String name = "holder_" + System.currentTimeMillis();

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
                thenAnswer(ioc -> resolveFinalResult());
        StatusLine statusLine = mock(StatusLine.class);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        holderClientRest = new HolderClientRest("http://localhost:8080/", name);
    }

    private String resolveFinalResult() {
        if (finalResult.get() == null) {
            throw new IllegalArgumentException("finalResult null");
        } else if (finalResult.get().getClass().isAssignableFrom(String.class)) {
            return (String) finalResult.get();
        } else if (finalResult.get().getClass().isAssignableFrom(HolderResult .class)) {
            return ((HolderResult) finalResult.get()).toTextString();
        } else {
            throw new IllegalArgumentException("finalResult unknown: " + finalResult);
        }
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
        mockedStaticHttpClientBuilder.close();
    }

    @Test
    public void getTest() throws IOException {
        finalResult.set(new HolderResult("value_" + name));
        HolderResult holderResult = holderClientRest.get();
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("holder"));
        assertTrue(finalUrl.contains("get"));
        assertTrue(finalUrl.contains(name));
        assertEquals(HolderResult.Status.RETRIEVED, holderResult.getStatus());
        assertEquals("value_" + name, holderResult.getValue());
        verify(httpclient).execute(any(HttpGet.class));
    }

    @Test
    public void getIfAvailableTest() throws IOException {
        finalResult.set(new HolderResult("value_" + name));
        HolderResult holderResult = holderClientRest.getIfAvailable();
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("holder"));
        assertTrue(finalUrl.contains("getIfAvailable"));
        assertTrue(finalUrl.contains(name));
        assertEquals(HolderResult.Status.RETRIEVED, holderResult.getStatus());
        assertEquals("value_" + name, holderResult.getValue());
        verify(httpclient).execute(any(HttpGet.class));
    }

    @Test
    public void getWithTimeOutMillisTest() throws IOException {
        finalResult.set(new HolderResult("value_" + name));
        HolderResult holderResult = holderClientRest.getWithTimeOutMillis(1234L);
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("holder"));
        assertTrue(finalUrl.contains("getWithTimeOut"));
        assertTrue(finalUrl.contains(Long.toString(1234L)));
        assertTrue(finalUrl.contains(TimeUnit.MILLISECONDS.name()));
        assertTrue(finalUrl.contains(name));
        assertEquals(HolderResult.Status.RETRIEVED, holderResult.getStatus());
        assertEquals("value_" + name, holderResult.getValue());
        verify(httpclient).execute(any(HttpGet.class));
    }

    @Test
    public void setTest() throws IOException {
        finalResult.set("ok");
        String newValue = "value_" + name;
        holderClientRest.set(newValue);
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("holder"));
        assertTrue(finalUrl.contains("set"));
        assertTrue(finalUrl.contains(name));
        assertTrue(finalUrl.contains(newValue));
        verify(httpclient).execute(any(HttpGet.class));
    }

    @Test
    public void setWithTimeToLiveTest() throws IOException {
        finalResult.set("ok");
        String newValue = "value_" + name;
        holderClientRest.setWithTimeToLiveMillis(newValue, 1234L);
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("holder"));
        assertTrue(finalUrl.contains("setWithTimeToLive"));
        assertTrue(finalUrl.contains(name));
        assertTrue(finalUrl.contains(newValue));
        assertTrue(finalUrl.contains(Long.toString(1234L)));
        assertTrue(finalUrl.contains(TimeUnit.MILLISECONDS.name()));
        verify(httpclient).execute(any(HttpGet.class));
    }

    @Test
    public void cancelTest() throws IOException {
        finalResult.set("ok");
        holderClientRest.cancel();
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("holder"));
        assertTrue(finalUrl.contains("cancel"));
        assertTrue(finalUrl.contains(name));
        verify(httpclient).execute(any(HttpGet.class));
    }

}
