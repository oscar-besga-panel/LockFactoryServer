package org.obapanel.lockfactoryserver.client.rest;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractClientRestTest {


    @Mock
    private HttpClientBuilder httpClientBuilder;

    @Mock
    private CloseableHttpClient httpclient;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private HttpEntity httpEntity;


    private MockedStatic<HttpClients> mockedStaticHttpClients;

    private MockedStatic<HttpClientBuilder> mockedStaticHttpClientBuilder;

    private MockedStatic<EntityUtils> mockedStaticEntityUtils;

    private final AtomicReference<HttpGet> finalRequest = new AtomicReference<>(null);

    private final AtomicReference<String> finalResult = new AtomicReference<>("");

    private final AtomicInteger finalStatus = new AtomicInteger(0);

    @Before
    public void setup() throws IOException {
        mockedStaticHttpClients = Mockito.mockStatic(HttpClients.class);
        mockedStaticHttpClients.when(() -> HttpClients.createDefault() ).thenReturn(httpclient);
        mockedStaticHttpClientBuilder = Mockito.mockStatic(HttpClientBuilder.class);
        mockedStaticHttpClientBuilder.when(() -> HttpClientBuilder.create() ).thenReturn(httpClientBuilder);
        when(httpClientBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(httpclient);
        when(httpclient.execute(any(HttpGet.class), any(HttpClientResponseHandler.class))).thenAnswer(ioc ->{
            finalRequest.set(ioc.getArgument(0,HttpGet.class));
            return ioc.getArgument(1, HttpClientResponseHandler.class).handleResponse(httpResponse);
        });
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        mockedStaticEntityUtils = Mockito.mockStatic(EntityUtils.class);
        mockedStaticEntityUtils.when(() -> EntityUtils.toString(eq(httpEntity))).
                thenAnswer(ioc -> finalResult.toString());
        when(httpResponse.getCode()).thenAnswer(ioc -> finalStatus.get());

    }

    @After
    public void tearsDown() {
        mockedStaticHttpClients.close();
        mockedStaticEntityUtils.close();
        mockedStaticHttpClientBuilder.close();
    }

    @Test
    public void testResponse200Test() {
        finalResult.set(Boolean.toString(true));
        finalStatus.set(200);
        TestAbstractClientRest testAbstractClientRest1 = new TestAbstractClientRest();
        String response = testAbstractClientRest1.requestWithUrl("test/1");
        assertEquals("true", response);
        testAbstractClientRest1.close();
    }

    @Test
    public void testResponse200TryTest() {
        finalResult.set(Boolean.toString(true));
        finalStatus.set(200);
        String response = "";
        try(TestAbstractClientRest testAbstractClientRest1 = new TestAbstractClientRest()){
            response = testAbstractClientRest1.requestWithUrl("test/1");
        }
        assertEquals("true", response);
    }

    @Test(expected = IllegalStateException.class)
    public void testResponse500Test() {
        finalResult.set(Boolean.toString(true));
        finalStatus.set(500);
        TestAbstractClientRest testAbstractClientRest1 = new TestAbstractClientRest();
        String response = testAbstractClientRest1.requestWithUrl("test/1");
        testAbstractClientRest1.close();
    }

    @Test(expected = IllegalStateException.class)
    public void testResponseNoUrlTest() {
        TestAbstractClientRest testAbstractClientRest9 = new TestAbstractClientRest();
        testAbstractClientRest9.requestWithUrl();
        testAbstractClientRest9.close();
    }

    private class TestAbstractClientRest extends AbstractClientRest {

        public TestAbstractClientRest() {
            super("http://127.0.0.1/", "TestAbstractClientRest");
        }

        @Override
        String serviceUrlName() {
            return "serviceUrlName";
        }
    }
}
