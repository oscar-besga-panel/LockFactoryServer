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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ManagementClientRestTest {

    @Mock
    private CloseableHttpClient httpclient;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private HttpEntity httpEntity;

    private MockedStatic<HttpClients> mockedStaticHttpClient;

    private MockedStatic<EntityUtils> mockedStaticEntityUtils;

    private ManagementClientRest managementClientRest;

    private final AtomicReference<HttpGet> finalRequest = new AtomicReference<>(null);

    private final AtomicReference<String> finalResult = new AtomicReference<>("");

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
        managementClientRest = new ManagementClientRest("http://localhost:8080/");
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
    public void isRunningTest() throws IOException {
        finalResult.set("true");
        boolean result = managementClientRest.isRunning();
        assertTrue(result);
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("management"));
        assertTrue(finalUrl.contains("isRunning"));
    }

    @Test
    public void shutdownServerTest() throws IOException {
        finalResult.set("ok");
        managementClientRest.shutdownServer();
        verify(httpclient).execute(any(HttpGet.class));
        String finalUrl = finalUrl();
        assertTrue(finalUrl.contains("management"));
        assertTrue(finalUrl.contains("shutdownServer"));
    }

}
