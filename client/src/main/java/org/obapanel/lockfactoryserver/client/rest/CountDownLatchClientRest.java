package org.obapanel.lockfactoryserver.client.rest;


import org.obapanel.lockfactoryserver.client.CountDownLatchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class CountDownLatchClientRest extends AbstractClientRest implements CountDownLatchClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchClientRest.class);

    private static final String SERVICE_URL_NAME_COUNT_DOWN_LATCH = "countDownLatch";

    public CountDownLatchClientRest(String baseServerUrl, String name) {
        super(baseServerUrl, name);
    }

    @Override
    String serviceUrlName() {
        return SERVICE_URL_NAME_COUNT_DOWN_LATCH;
    }

    public boolean createNew(int count) {
        String response = requestWithUrl( "createNew", getName(), Integer.toString(count));
        boolean result = Boolean.parseBoolean(response);
        LOGGER.debug("createNew name {} count {} response {}", getName(), count, result);
        return result;
    }

    public void countDown() {
        String response = requestWithUrl( "countDown", getName());
        LOGGER.debug("countDown name {} response {}", getName(), response);
    }

    public void countDown(int count) {
        String response = requestWithUrl( "countDown", getName(), Integer.toString(count));
        LOGGER.debug("countDown name {} response {}", getName(), response);
    }

    public boolean isActive() {
        return getCount() > 0;
    }

    public int getCount() {
        String response = requestWithUrl( "getCount", getName());
        int result = Integer.parseInt(response);
        LOGGER.debug("getCount name {} response {}", getName(), result);
        return result;
    }

    public void awaitLatch() {
        String response = requestWithUrl( "await", getName());
        LOGGER.debug("await name {} response {}", getName(), response);
    }

    public boolean tryAwaitWithTimeOut(long timeOut, TimeUnit timeUnit) {
        String response = requestWithUrl( "tryAwaitWithTimeOut", getName(), Long.toString(timeOut), timeUnit.name().toLowerCase());
        boolean result = Boolean.parseBoolean(response);
        LOGGER.debug("tryAwaitWithTimeOut name {} timeOut {} timeUnit {} response {}", getName(), timeOut, timeUnit, result);
        return result;
    }

    public boolean tryAwaitWithTimeOut(long timeOut) {
        String response = requestWithUrl( "tryAwaitWithTimeOut", getName(), Long.toString(timeOut));
        boolean result = Boolean.parseBoolean(response);
        LOGGER.debug("tryAwaitWithTimeOut name {} timeOut(ms) {} response {}", getName(), timeOut, result);
        return result;
    }

}
