package org.obapanel.lockfactoryserver.server.connections.rest;

import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;

import java.util.concurrent.TimeUnit;

public class CountDownLatchServerRestImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreServerRestImpl.class);

    public final static String OK = "ok";
    public final static String KO = "ko";

    private final CountDownLatchService countDownLatchService;

    public CountDownLatchServerRestImpl(CountDownLatchService countDownLatchService) {
        this.countDownLatchService = countDownLatchService;
    }

    public void createNew(Context context) {
        String name = context.getPathTokens().get("name");
        int count = Integer.parseInt(context.getPathTokens().get("count"));
        LOGGER.info("rest server> createNew name {} count {}", name, count);
        boolean result = countDownLatchService.createNew(name, count);
        if (!result) {
            context.getResponse().status(500);
        }
        context.getResponse().send(Boolean.toString(result));
    }

    public void countDown(Context context) {
        String name = context.getPathTokens().get("name");
        LOGGER.info("rest server> countDown name {}", name);
        countDownLatchService.countDown(name);
        context.getResponse().send(OK);
    }


    public void getCount(Context context) {
        String name = context.getPathTokens().get("name");
        LOGGER.info("rest server> getCount name {}", name);
        int count = countDownLatchService.getCount(name);
        context.getResponse().send(Integer.toString(count));
    }

    public void await(Context context) {
        boolean result = false;
        String name = context.getPathTokens().get("name");
        if (context.getPathTokens().get("time") != null) {
            long time = Long.parseLong(context.getPathTokens().get("time"));
            String timeUnitName = context.getPathTokens().getOrDefault("timeUnit", TimeUnit.MILLISECONDS.name());
            TimeUnit timeUnit = TimeUnit.valueOf(timeUnitName.toUpperCase());
            LOGGER.info("rest server> await name {} timeOut {} timeUnit {}", name, time, timeUnit);
            result = countDownLatchService.await(name, time, timeUnit);
        } else {
            LOGGER.info("rest server> await name {}", name);
            countDownLatchService.await(name);
            result = true;
        }
        if (!result) {
            context.getResponse().status(500);
        }
        context.getResponse().send(Boolean.toString(result));
    }

}
