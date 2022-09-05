package org.obapanel.lockfactoryserver.client.rest;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

public class LockObjectClientRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockObjectClientRest.class);

    private final String baseUrl;
    private final String name;
    private String token;

    public LockObjectClientRest(String name) {
        this("http://localhost:8080/lock/", name);
    }

    public LockObjectClientRest(String baseUrl, String name) {
        this.baseUrl = baseUrl;
        this.name = name;
    }

    protected String requestWithUrl(String... parts) {
        return request(String.join("/", parts));
    }


    protected String request(String operation) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl + operation);
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error in requesrt", e);
        }
    }

    public boolean lock() {
        token = requestWithUrl( "lock", name);
        return currentlyBlocked();
    }

    public boolean tryLock() throws RemoteException {
        token = requestWithUrl("tryLock", name);
        return currentlyBlocked();
    }

    public boolean tryLock(long time, TimeUnit timeUnit) throws RemoteException {
        token = requestWithUrl("tryLock", name, Long.toString(time), timeUnit.name().toLowerCase());
        return currentlyBlocked();
    }

    protected boolean currentlyBlocked() {
        return token != null && !token.isEmpty();
    }

    public boolean isLocked() throws RemoteException {
        String result = requestWithUrl( "isLocked", name);
        return Boolean.parseBoolean(result);
    }

    public boolean unLock() throws RemoteException {
        String requerstResult = requestWithUrl( "unlock", name, token);
        boolean unlocked = Boolean.parseBoolean(requerstResult);
        if (unlocked) {
            token = null;
        }
        return unlocked;
    }

    public void close() {
        LOGGER.debug("closed");
    }
}
