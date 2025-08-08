package org.obapanel.lockfactoryserver.server.connections.rest.OLD;

import com.github.arteam.embedhttp.HttpRequest;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CountDownLatchServerRestImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreServerRestImpl.class);

    public final static String OK = "ok";
    public final static String KO = "ko";

    private final CountDownLatchService countDownLatchService;

    public CountDownLatchServerRestImpl(CountDownLatchService countDownLatchService) {
        this.countDownLatchService = countDownLatchService;
    }


    public String createNew(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        int count;
        if (parameters.size() > 1) {
            count = Integer.parseInt(parameters.get(1));
        } else {
            count = 1;
        }
        LOGGER.info("rest server> createNew name {} count {}", name, count);
        boolean result = countDownLatchService.createNew(name, count);
        return Boolean.toString(result);
    }

    public String countDown(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        if (parameters.size() > 1) {
            int count = Integer.parseInt(parameters.get(1));
            LOGGER.info("rest server> countDown name {} count {}", name, count);
            countDownLatchService.countDown(name, count);
        } else {
            LOGGER.info("rest server> countDown name {}", name);
            countDownLatchService.countDown(name);
        }
        return  OK;
    }


    public String getCount(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        LOGGER.info("rest server> getCount name {}", name);
        int count = countDownLatchService.getCount(name);
        return Integer.toString(count);
    }

    public String await(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        LOGGER.info("rest server> await name {}", name);
        countDownLatchService.await(name);
        return OK;
    }

    public String tryAwaitWithTimeOut(String prefix, List<String> parameters, HttpRequest request) {
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
        LOGGER.info("rest server> tryAwaitWithTimeOut name {} timeOut {} timeUnit {}", name, time, timeUnit);
        boolean result = countDownLatchService.tryAwaitWithTimeOut(name, time, timeUnit);
        return Boolean.toString(result);
    }

}
