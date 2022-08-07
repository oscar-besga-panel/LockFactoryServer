package org.obapanel.lockfactoryserver.server;

import java.util.Properties;

public class LockFactoryConfiguration {


    public static final String TRUE = "true";

    public static final String RMI_SERVER_ACTIVE =  "rmiServerActive";
    public static final String RMI_SERVER_PORT = "rmiServerPort";
    public static final String GRPC_SERVER_ACTIVE = "grpcServerActive";
    public static final String GRPC_SERVER_PORT = "grpcServerPort";
    public static final String REST_SERVER_ACTIVE = "restServerActive";
    public static final String REST_SERVER_PORT = "restServerPort";

    public static final String LOCK_ENABLED = "lockEnabled";
    public static final String SEMAPHORE_ENABLED = "semaphoreEnabled";

    public static final String CACHE_CHECK_DATA_PERIOD_SECONDS = "cacheCheckDataPeriodSeconds";
    public static final String CACHE_TIME_TO_LIVE_SECONDS = "cacheTimeToLiveSeconds";

    public static final String DEFAULT_RMI_SERVER_ACTIVE = TRUE;
    public static final String DEFAULT_RMI_SERVER_PORT = "1099";
    public static final String DEFAULT_GRPC_SERVER_ACTIVE = TRUE;
    public static final String DEFAULT_GRPC_SERVER_PORT = "50051";
    public static final String DEFAULT_REST_SERVER_ACTIVE = TRUE;
    public static final String DEFAULT_REST_SERVER_PORT = "8080";

    public static final String DEFAULT_LOCK_ENABLED = TRUE;
    public static final String DEFAULT_SEMAPHORE_ENABLED = TRUE;

    public static final String DEFAULT_CACHE_CHECK_DATA_PERIOD_SECONDS = "30";
    public static final String DEFAULT_CACHE_TIME_TO_LIVE_SECONDS = "150";



    private final Properties properties;

    public LockFactoryConfiguration() {
        this(new Properties());
    }

    public LockFactoryConfiguration(Properties properties) {
        this.properties = properties;
    }

    Properties getProperties() {
        return properties;
    }

    public boolean isRmiServerActive() {
        return Boolean.parseBoolean(properties.getProperty(RMI_SERVER_ACTIVE, DEFAULT_RMI_SERVER_ACTIVE));
    }

    public int getRmiServerPort() {
        return Integer.parseInt(properties.getProperty(RMI_SERVER_PORT, DEFAULT_RMI_SERVER_PORT));
    }

    public boolean isGrpcServerActive() {
        return Boolean.parseBoolean(properties.getProperty(GRPC_SERVER_ACTIVE, DEFAULT_GRPC_SERVER_ACTIVE));
    }

    public int getGrpcServerPort() {
        return Integer.parseInt(properties.getProperty(GRPC_SERVER_PORT, DEFAULT_GRPC_SERVER_PORT));
    }

    public boolean isRestServerActive() {
        return Boolean.parseBoolean(properties.getProperty(REST_SERVER_ACTIVE, DEFAULT_REST_SERVER_ACTIVE));
    }

    public int getRestServerPort() {
        return Integer.parseInt(properties.getProperty(REST_SERVER_PORT, DEFAULT_REST_SERVER_PORT));
    }


    public boolean isLockEnabled() {
        return Boolean.parseBoolean(properties.getProperty(LOCK_ENABLED, DEFAULT_LOCK_ENABLED));
    }

    public boolean isSemaphoreEnabled() {
        return Boolean.parseBoolean(properties.getProperty(SEMAPHORE_ENABLED, DEFAULT_SEMAPHORE_ENABLED));
    }

    public int getCacheCheckDataPeriodSeconds() {
        return Integer.parseInt(properties.getProperty(CACHE_CHECK_DATA_PERIOD_SECONDS,
                DEFAULT_CACHE_CHECK_DATA_PERIOD_SECONDS));
    }

    public int getCacheTimeToLiveSeconds() {
        return Integer.parseInt(properties.getProperty(CACHE_TIME_TO_LIVE_SECONDS,
                DEFAULT_CACHE_TIME_TO_LIVE_SECONDS));
    }
}