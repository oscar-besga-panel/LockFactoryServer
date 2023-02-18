package org.obapanel.lockfactoryserver.client.rest;

import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class HolderClientRest extends AbstractClientRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HolderClientRest.class);

    private static final String SERVICE_URL_NAME_HOLDER = "holder";

    public HolderClientRest(String baseServerUrl, String name) {
        super(baseServerUrl, name);
    }

    @Override
    String serviceUrlName() {
        return SERVICE_URL_NAME_HOLDER;
    }

    public HolderResult get() {
        String response = requestWithUrl( "get", getName());
        HolderResult result = HolderResult.fromTextString(response);
        LOGGER.debug("get name {} result {}", getName(), result);
        return result;
    }

    public HolderResult getIfAvailable() {
        String response = requestWithUrl( "getIfAvailable", getName());
        HolderResult result = HolderResult.fromTextString(response);
        LOGGER.debug("getIfAvailable name {} result {}", getName(), result);
        return result;
    }

    public HolderResult getWithTimeOutMillis(long timeOutMillis) {
        return getWithTimeOut(timeOutMillis, TimeUnit.MILLISECONDS);
    }

    public HolderResult getWithTimeOut(long timeOut, TimeUnit timeUnit) {
        String response = requestWithUrl( "getWithTimeOut", getName(), Long.toString(timeOut), timeUnit.name());
        HolderResult result = HolderResult.fromTextString(response);
        LOGGER.debug("getWithTimeOut name {} timeOut {} timeUnit {} result {}", getName(), timeOut, timeUnit, result);
        return result;
    }

    public void set(String newValue) {
        String response = requestWithUrl( "set", getName(), newValue);
        LOGGER.debug("get name {} newValue {} response {}", getName(), newValue, response);
    }

    public void setWithTimeToLiveMillis(String newValue, long timeToLiveMillis) {
        setWithTimeToLive(newValue, timeToLiveMillis, TimeUnit.MILLISECONDS);
    }

    public void setWithTimeToLive(String newValue, long timeToLive, TimeUnit timeUnit) {
        String response = requestWithUrl( "setWithTimeToLive", getName(), newValue,
                Long.toString(timeToLive), timeUnit.name());
        LOGGER.debug("get name {} newValue {} timeToLive {} timeUnit {} response {}", getName(), newValue,
                timeToLive, timeUnit.name(), response);
    }

    public void cancel() {
        String response = requestWithUrl( "cancel", getName());
        LOGGER.debug("cancel name {} response {}", getName(), response);
    }

}
