package org.obapanel.lockfactoryserver.server.connections.rest;

import com.github.arteam.embedhttp.EmbeddedHttpServer;
import com.github.arteam.embedhttp.EmbeddedHttpServerBuilder;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.connections.Connections;
import org.obapanel.lockfactoryserver.server.connections.LockFactoryConnection;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;
import org.obapanel.lockfactoryserver.server.service.holder.HolderService;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;
import org.obapanel.lockfactoryserver.server.service.rateLimiter.BucketRateLimiterService;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class that provides a REST connection for the services and binds them
 */
public class RestConnection implements LockFactoryConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestConnection.class);

    public static final Connections TYPE = Connections.REST;

    private EmbeddedHttpServer embeddedHttpServer;

    @Override
    public Connections getType() {
        return TYPE;
    }

    @Override
    public void activate(LockFactoryConfiguration configuration, Map<Services, LockFactoryServices> services) {

        if (TYPE == Connections.REST) {
            throw new IllegalStateException("RestConnection should not be activated with Connections.REST, use other instead");
        }

        EmbeddedHttpServerBuilder builder = EmbeddedHttpServerBuilder.createNew();
        builder.withPort(configuration.getRestServerPort());
        builder.withBackLog(configuration.getRestConnectQueueSize());
        ExecutorService executor = Executors.newFixedThreadPool(configuration.getRestServerThreads());
        builder.withExecutor(executor);
        if (configuration.isManagementEnabled()) {
            chainManagement(builder, (ManagementService) services.get(Services.MANAGEMENT));
        }
        if (configuration.isLockEnabled()) {
            chainLock(builder, (LockService) services.get(Services.LOCK));
        }
        if (configuration.isSemaphoreEnabled()) {
            chainSemaphore(builder, (SemaphoreService) services.get(Services.SEMAPHORE));
        }
        if (configuration.isCountDownLatchEnabled()) {
            chainCountDownLatch(builder, (CountDownLatchService) services.get(Services.COUNTDOWNLATCH));
        }
        if (configuration.isHolderEnabled()) {
            chainHolder(builder, (HolderService) services.get(Services.HOLDER));
        }
        if (configuration.isBucketRateLimiterEnabled()) {
            chainBucketRateLimiter(builder, (BucketRateLimiterService) services.get(Services.BUCKET_RATE_LIMITER));
        }
        embeddedHttpServer = builder.buildAndRun();
        LOGGER.debug("RestConnection activated");
    }

    private void chainManagement(EmbeddedHttpServerBuilder builder, ManagementService managementService) {
        ManagementServerRestImpl managementServerRest = new ManagementServerRestImpl(managementService);
        addPlainTextHandler(builder, "/management/shutdownServer", managementServerRest::shutdownServer);
        addPlainTextHandler(builder,"/management/shutdownserver", managementServerRest::shutdownServer);
        addPlainTextHandler(builder,"/management/isRunning", managementServerRest::isRunning);
        addPlainTextHandler(builder,"/management/isrunning", managementServerRest::isRunning);
    }

    private void chainLock(EmbeddedHttpServerBuilder builder, LockService lockService) {
        LockServerRestImpl lockServerRest = new LockServerRestImpl(lockService);
        addPlainTextHandlerWithPrefix(builder, "/lock/lock", lockServerRest::lock );
        addPlainTextHandlerWithPrefix(builder, "/lock/unlock", lockServerRest::unlock );
        addPlainTextHandlerWithPrefix(builder, "/lock/unLock", lockServerRest::unlock );
        addPlainTextHandlerWithPrefix(builder, "/lock/tryLock", lockServerRest::tryLock);
        addPlainTextHandlerWithPrefix(builder, "/lock/trylock", lockServerRest::tryLock);
        addPlainTextHandlerWithPrefix(builder, "/lock/tryLockWithTimeout", lockServerRest::tryLockWithTimeout);
        addPlainTextHandlerWithPrefix(builder, "/lock/trylockwithtimeout", lockServerRest::tryLockWithTimeout);
        addPlainTextHandlerWithPrefix(builder, "/lock/lockStatus", lockServerRest::lockStatus);
        addPlainTextHandlerWithPrefix(builder, "/lock/lockstatus", lockServerRest::lockStatus);
        addPlainTextHandlerWithPrefix(builder, "/lock/unLock", lockServerRest::unlock );
        addPlainTextHandlerWithPrefix(builder, "/lock/unlock", lockServerRest::unlock );
    }

    private void chainSemaphore(EmbeddedHttpServerBuilder builder, SemaphoreService semaphoreService) {
        SemaphoreServerRestImpl semaphoreServerRest = new SemaphoreServerRestImpl(semaphoreService);
        addPlainTextHandlerWithPrefix(builder, "/semaphore/currentPermits", semaphoreServerRest::currentPermits );
        addPlainTextHandlerWithPrefix(builder, "/semaphore/currentpermits", semaphoreServerRest::currentPermits );
        addPlainTextHandlerWithPrefix(builder, "/semaphore/acquire", semaphoreServerRest::acquire );
        addPlainTextHandlerWithPrefix(builder, "/semaphore/tryAcquire", semaphoreServerRest::tryAcquire );
        addPlainTextHandlerWithPrefix(builder, "/semaphore/tryacquire", semaphoreServerRest::tryAcquire );
        addPlainTextHandlerWithPrefix(builder, "/semaphore/tryAcquireWithTimeOut", semaphoreServerRest::tryAcquireWithTimeOut );
        addPlainTextHandlerWithPrefix(builder, "/semaphore/tryacquirewithtimeout", semaphoreServerRest::tryAcquireWithTimeOut );
        addPlainTextHandlerWithPrefix(builder, "/semaphore/release", semaphoreServerRest::release );
    }

    private void chainCountDownLatch(EmbeddedHttpServerBuilder builder, CountDownLatchService countDownLatchService) {
        CountDownLatchServerRestImpl countDownLatchServerRest = new CountDownLatchServerRestImpl(countDownLatchService);
        for(String prefix: Arrays.asList("/countDownLatch", "/countdownlatch")) {
            addPlainTextHandlerWithPrefix(builder, prefix + "/createNew", countDownLatchServerRest::createNew );
            addPlainTextHandlerWithPrefix(builder, prefix + "/createnew", countDownLatchServerRest::createNew );
            addPlainTextHandlerWithPrefix(builder, prefix + "/countDown", countDownLatchServerRest::countDown );
            addPlainTextHandlerWithPrefix(builder, prefix + "/countdown", countDownLatchServerRest::countDown );
            addPlainTextHandlerWithPrefix(builder, prefix + "/getCount", countDownLatchServerRest::getCount );
            addPlainTextHandlerWithPrefix(builder, prefix + "/getcount", countDownLatchServerRest::getCount );
            addPlainTextHandlerWithPrefix(builder, prefix + "/await", countDownLatchServerRest::await );
            addPlainTextHandlerWithPrefix(builder, prefix + "/tryAwaitWithTimeOut", countDownLatchServerRest::tryAwaitWithTimeOut );
            addPlainTextHandlerWithPrefix(builder, prefix + "/tryawaitwithtimeout", countDownLatchServerRest::tryAwaitWithTimeOut );
        }
    }

    private void chainHolder(EmbeddedHttpServerBuilder builder, HolderService holderService) {
        HolderServerRestImpl holderServerRest = new HolderServerRestImpl(holderService);
        addPlainTextHandlerWithPrefix(builder, "/holder/get", holderServerRest::get);
        addPlainTextHandlerWithPrefix(builder, "/holder/getWithTimeOut", holderServerRest::getWithTimeOut);
        addPlainTextHandlerWithPrefix(builder, "/holder/getwithtimeout", holderServerRest::getWithTimeOut);
        addPlainTextHandlerWithPrefix(builder, "/holder/getIfAvailable", holderServerRest::getIfAvailable);
        addPlainTextHandlerWithPrefix(builder, "/holder/getifavailable", holderServerRest::getIfAvailable);
        addPlainTextHandlerWithPrefix(builder, "/holder/set", holderServerRest::set);
        addPlainTextHandlerWithPrefix(builder, "/holder/setWithTimeToLive", holderServerRest::setWithTimeToLive);
        addPlainTextHandlerWithPrefix(builder, "/holder/setwithtimetolive", holderServerRest::setWithTimeToLive);
        addPlainTextHandlerWithPrefix(builder, "/holder/cancel", holderServerRest::cancel);
    }

    private void chainBucketRateLimiter(EmbeddedHttpServerBuilder builder, BucketRateLimiterService bucketRateLimiterService) {
        BucketRateLimiterServerRestImpl bucketRateLimiterServerRest = new BucketRateLimiterServerRestImpl(bucketRateLimiterService);
        for(String prefix: Arrays.asList("/bucketRateLimiter", "/bucketratelimiter", "/rateLimiter", "/ratelimiter")) {
            addPlainTextHandlerWithPrefix(builder, prefix + "/newRateLimiter", bucketRateLimiterServerRest::newRateLimiter);
            addPlainTextHandlerWithPrefix(builder, prefix + "/newratelimiter", bucketRateLimiterServerRest::newRateLimiter);
            addPlainTextHandlerWithPrefix(builder, prefix + "/getAvailableTokens", bucketRateLimiterServerRest::getAvailableTokens);
            addPlainTextHandlerWithPrefix(builder, prefix + "/getavailabletokens", bucketRateLimiterServerRest::getAvailableTokens);
            addPlainTextHandlerWithPrefix(builder, prefix + "/tryConsume", bucketRateLimiterServerRest::tryConsume);
            addPlainTextHandlerWithPrefix(builder, prefix + "/tryconsume", bucketRateLimiterServerRest::tryConsume);
            addPlainTextHandlerWithPrefix(builder, prefix + "/tryConsumeWithTimeOut", bucketRateLimiterServerRest::tryConsumeWithTimeOut);
            addPlainTextHandlerWithPrefix(builder, prefix + "/tryconsumewithtimeOut", bucketRateLimiterServerRest::tryConsumeWithTimeOut);
            addPlainTextHandlerWithPrefix(builder, prefix + "/consume", bucketRateLimiterServerRest::consume);
            addPlainTextHandlerWithPrefix(builder, prefix + "/remove", bucketRateLimiterServerRest::remove);
        }
    }

    private void addPlainTextHandlerWithPrefix(EmbeddedHttpServerBuilder builder, String prefix, RestConnectionHelper.PlainTextHandlerWithPrefix handler) {
        builder.addHandler(prefix, (RestConnectionHelper.PlainTextHandler) request -> handler.execute(prefix, request));
    }

    private void addPlainTextHandler(EmbeddedHttpServerBuilder builder, String prefix, RestConnectionHelper.PlainTextHandler handler) {
        builder.addHandler(prefix, handler);
    }

    @Override
    public void shutdown() throws Exception {
        if (embeddedHttpServer != null) {
            embeddedHttpServer.stop();
        }
        LOGGER.debug("RestConnection shutdown");
    }



}
