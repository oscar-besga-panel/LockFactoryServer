package org.obapanel.lockfactoryserver.server.connections.rest;

import com.github.arteam.embedhttp.HttpRequest;
import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.obapanel.lockfactoryserver.server.service.holder.HolderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class HolderServerRestImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(HolderServerRestImpl.class);

    public final static String OK = "ok";

    private final HolderService holderService;

    public HolderServerRestImpl(HolderService holderService) {
        this.holderService = holderService;
    }


    public String get(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        LOGGER.info("rest server> get name {}", name);
        HolderResult holderResult = holderService.get(name);
        return holderResult.toTextString();
    }

    public String getWithTimeOut(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        long time;
        if (parameters.size() > 1) {
            time = Long.parseLong(parameters.get(1));
        } else {
            time = 1;
        }
        TimeUnit timeUnit;
        if (parameters.size() > 2) {
            timeUnit = TimeUnit.valueOf(parameters.get(2).toUpperCase());
        } else {
            timeUnit = TimeUnit.MILLISECONDS;
        }
        LOGGER.info("rest server> getWithTimeOut name {} timeOut {} timeUnit {}", name, time, timeUnit);
        HolderResult holderResult = holderService.getWithTimeOut(name, time, timeUnit);
        return holderResult.toTextString();
    }

    public String getIfAvailable(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        LOGGER.info("rest server> getIfAvailable name {}", name);
        HolderResult holderResult = holderService.getIfAvailable(name);
        return holderResult.toTextString();
    }

    public String set(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        String newValue = parameters.get(1);
        LOGGER.info("rest server> set name {} newValue {}", name, newValue);
        holderService.set(name, newValue);
        return OK;
    }

    public String setWithTimeToLive(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        String newValue = parameters.get(1);
        long time;
        if (parameters.size() > 2) {
            time = Long.parseLong(parameters.get(2));
        } else {
            time = 1;
        }
        TimeUnit timeUnit;
        if (parameters.size() > 23) {
            timeUnit = TimeUnit.valueOf(parameters.get(3).toUpperCase());
        } else {
            timeUnit = TimeUnit.MILLISECONDS;
        }
        LOGGER.info("rest server> setWithTimeToLive name {} newValue {} timeToLive {} timeUnit {}",
                name, newValue, time, timeUnit);
        holderService.setWithTimeToLive(name, newValue, time, timeUnit);
        return OK;
    }

    public String cancel(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        LOGGER.info("rest server> cancel name {} ", name);
        holderService.cancel(name);
        return OK;
    }

}
