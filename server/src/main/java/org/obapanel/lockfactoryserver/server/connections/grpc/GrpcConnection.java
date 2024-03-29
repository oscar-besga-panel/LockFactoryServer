package org.obapanel.lockfactoryserver.server.connections.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
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

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Class that provides a GRPC connection for the services and binds them
 */
public class GrpcConnection implements LockFactoryConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcConnection.class);

    public static final Connections TYPE = Connections.GRPC;

    private Server grpcServer;

    @Override
    public Connections getType() {
        return TYPE;
    }

    @Override
    public void activate(LockFactoryConfiguration configuration, Map<Services, LockFactoryServices> services) throws Exception {
        ServerBuilder serverBuilder = ServerBuilder.forPort(configuration.getGrpcServerPort());
        if (configuration.isManagementEnabled()) {
            ManagementService managementService = (ManagementService)  services.get(Services.MANAGEMENT);
            ManagementServerGrpcImpl managementServerGrpc = new ManagementServerGrpcImpl(managementService);
            serverBuilder.addService(managementServerGrpc);
        }
        if (configuration.isLockEnabled()) {
            LockService lockService = (LockService)  services.get(Services.LOCK);
            LockServerGrpcImpl lockServerGrpc = new LockServerGrpcImpl(lockService);
            serverBuilder.addService(lockServerGrpc);
        }
        if (configuration.isSemaphoreEnabled()) {
            SemaphoreService semaphoreService = (SemaphoreService) services.get(Services.SEMAPHORE);
            SemaphoreServerGrpcImpl semaphoreServerGrpc = new SemaphoreServerGrpcImpl(semaphoreService);
            serverBuilder.addService(semaphoreServerGrpc);
        }
        if (configuration.isCountDownLatchEnabled()) {
            CountDownLatchService countDownLatchService = (CountDownLatchService) services.get(Services.COUNTDOWNLATCH);
            CountDownLatchServerGrpcImpl countDownLatchServerGrpc = new CountDownLatchServerGrpcImpl(countDownLatchService);
            serverBuilder.addService(countDownLatchServerGrpc);
        }
        if (configuration.isHolderEnabled()) {
            HolderService holderService = (HolderService) services.get(Services.HOLDER);
            HolderServerGrpcImpl holderServerGrpc = new HolderServerGrpcImpl(holderService);
            serverBuilder.addService(holderServerGrpc);
        }
        if (configuration.isBucketRateLimiterEnabled()) {
            BucketRateLimiterService bucketRateLimiterService = (BucketRateLimiterService) services.get(Services.BUCKET_RATE_LIMITER);
            BucketRateLimiterGrpcImpl rateLimiterBucketGrpc = new BucketRateLimiterGrpcImpl(bucketRateLimiterService);
            serverBuilder.addService(rateLimiterBucketGrpc);
        }
        grpcServer = serverBuilder.build();
        grpcServer.start();
        LOGGER.debug("GrpcConnection activated");
    }

    @Override
    public void shutdown() throws Exception {
        if (grpcServer != null) {
            grpcServer.shutdown();
            grpcServer.awaitTermination(3, TimeUnit.SECONDS);
        }
        LOGGER.debug("GrpcConnection shutdown");
    }

}
