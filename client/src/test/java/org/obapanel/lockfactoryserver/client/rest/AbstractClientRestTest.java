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
    private CloseableHttpClient httpclient;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private StatusLine statusLine;

    @Mock
    private HttpEntity httpEntity;


    private MockedStatic<HttpClients> mockedStaticHttpClient;

    private MockedStatic<EntityUtils> mockedStaticEntityUtils;

    private final AtomicReference<HttpGet> finalRequest = new AtomicReference<>(null);

    private final AtomicReference<String> finalResult = new AtomicReference<>("");

    private final AtomicInteger finalStatus = new AtomicInteger(0);

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
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenAnswer(ioc -> finalStatus.get());

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
    public void testResponse200Test() {
        finalResult.set(Boolean.toString(true));
        finalStatus.set(200);
        TestAbstractClientRest testAbstractClientRest1 = new TestAbstractClientRest();
        String response = testAbstractClientRest1.requestWithUrl("test/1");
        assertEquals("true", response);
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
    }

    @Test(expected = IllegalStateException.class)
    public void testResponseNoUrlTest() {
        TestAbstractClientRest testAbstractClientRest9 = new TestAbstractClientRest();
        testAbstractClientRest9.requestWithUrl();
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
