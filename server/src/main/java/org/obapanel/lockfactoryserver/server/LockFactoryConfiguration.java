package org.obapanel.lockfactoryserver.server;

import java.util.Properties;

/**
 * Class that holds a properties file and gives configuration to all other components of the project
 */
public class LockFactoryConfiguration {


    public static final String TRUE = "true";
    public static final String FALSE = "false";


    public static final String RMI_SERVER_ACTIVE =  "rmiServerActive";
    public static final String RMI_SERVER_PORT = "rmiServerPort";
    public static final String GRPC_SERVER_ACTIVE = "grpcServerActive";
    public static final String GRPC_SERVER_PORT = "grpcServerPort";
    public static final String REST_SERVER_ACTIVE = "restServerActive";
    public static final String REST_SERVER_PORT = "restServerPort";
    public static final String REST_CONNECT_QUEUE_SIZE = "restConnectQueueSize";
    public static final String REST_CONNECT_TIMEOUT_MILLIS = "restConnectTimeoutMillis";
    public static final String REST_SERVER_THREADS = "restServerThreads";

    public static final String LOCK_ENABLED = "lockEnabled";
    public static final String SEMAPHORE_ENABLED = "semaphoreEnabled";
    public static final String COUNTDOWNLATCH_ENABLED = "countDownLatchEnabled";
    public static final String MANAGEMENT_ENABLED = "managementEnabled";
    public static final String HOLDER_ENABLED = "holderEnabled";
    public static final String HOLDER_MAXIMUM_SIZE = "holderMaximumSize";


    public static final String CACHE_CHECK_DATA_PERIOD_SECONDS = "cacheCheckDataPeriodSeconds";
    public static final String CACHE_TIME_TO_LIVE_SECONDS = "cacheTimeToLiveSeconds";
    public static final String CACHE_CHECK_CONTINUOUSLY = "cacheCheckContinuously";

    public static final String SYNCHRONIZED_SERVICES = "synchronizedServices";

    public static final String DEFAULT_RMI_SERVER_ACTIVE = TRUE;
    public static final String DEFAULT_RMI_SERVER_PORT = "1099";
    public static final String DEFAULT_GRPC_SERVER_ACTIVE = TRUE;
    public static final String DEFAULT_GRPC_SERVER_PORT = "50051";
    public static final String DEFAULT_REST_SERVER_ACTIVE = TRUE;
    public static final String DEFAULT_REST_SERVER_PORT = "8080";
    public static final String DEFAULT_REST_CONNECT_QUEUE_SIZE = "20";
    public static final String DEFAULT_REST_CONNECT_TIMEOUT_MILLIS = "30000";
    public static final String DEFAULT_REST_SERVER_THREADS = "8"; //DEFAULT_THREADS = Runtime.getRuntime().availableProcessors() * 2

    public static final String DEFAULT_LOCK_ENABLED = TRUE;
    public static final String DEFAULT_SEMAPHORE_ENABLED = TRUE;
    public static final String DEFAULT_COUNTDOWNLATCH_ENABLED = TRUE;
    public static final String DEFAULT_MANAGEMENT_ENABLED = TRUE;
    public static final String DEFAULT_HOLDER_ENABLED = TRUE;
    public static final String DEFAULT_HOLDER_MAXIMUM_SIZE = "1024";


    public static final String DEFAULT_CACHE_CHECK_DATA_PERIOD_SECONDS = "10";
    public static final String DEFAULT_CACHE_TIME_TO_LIVE_SECONDS = "10";
    public static final String DEFAULT_CACHE_CHECK_CONTINUOUSLY = TRUE;

//     checking
    public static final String DEFAULT_SYNCHRONIZED_SERVICES = FALSE;
//    public static final String DEFAULT_SYNCHRONIZED_SERVICES = TRUE;



    private final Properties properties;

    /**
     * New configuration with empty properties, it will use default values
     */
    public LockFactoryConfiguration() {
        this(new Properties());
    }

    /**
     * New configuration with given properties, it will use given values
     * @param properties from external source, like a file
     */
    public LockFactoryConfiguration(Properties properties) {
        this.properties = properties;
    }

    /**
     * New configuration clone
     * @param other Other configuration to be cloned
     */
    public LockFactoryConfiguration(LockFactoryConfiguration other) {
        this.properties = new Properties(other.properties);
    }


    /**
     * Get the properties under the hood
     * @return propeties of the object
     */
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

    public int getRestConnectQueueSize() {
        return Integer.parseInt(properties.getProperty(REST_CONNECT_QUEUE_SIZE, DEFAULT_REST_CONNECT_QUEUE_SIZE));
    }

    public int getRestConnectTimeoutMillis() {
        return Integer.parseInt(properties.getProperty(REST_CONNECT_TIMEOUT_MILLIS, DEFAULT_REST_CONNECT_TIMEOUT_MILLIS));
    }

    public int getRestServerThreads() {
        return Integer.parseInt(properties.getProperty(REST_SERVER_THREADS, DEFAULT_REST_SERVER_THREADS));
    }

    public boolean isLockEnabled() {
        return Boolean.parseBoolean(properties.getProperty(LOCK_ENABLED, DEFAULT_LOCK_ENABLED));
    }

    public boolean isSemaphoreEnabled() {
        return Boolean.parseBoolean(properties.getProperty(SEMAPHORE_ENABLED, DEFAULT_SEMAPHORE_ENABLED));
    }

    public boolean isCountDownLatchEnabled() {
        return Boolean.parseBoolean(properties.getProperty(COUNTDOWNLATCH_ENABLED, DEFAULT_COUNTDOWNLATCH_ENABLED));
    }

    public int getCacheCheckDataPeriodSeconds() {
        return Integer.parseInt(properties.getProperty(CACHE_CHECK_DATA_PERIOD_SECONDS,
                DEFAULT_CACHE_CHECK_DATA_PERIOD_SECONDS));
    }

    public int getCacheTimeToLiveSeconds() {
        return Integer.parseInt(properties.getProperty(CACHE_TIME_TO_LIVE_SECONDS,
                DEFAULT_CACHE_TIME_TO_LIVE_SECONDS));
    }

    public boolean isCacheCheckContinuously() {
        return Boolean.parseBoolean(properties.getProperty(CACHE_CHECK_CONTINUOUSLY, DEFAULT_CACHE_CHECK_CONTINUOUSLY));
    }

    public boolean isManagementEnabled() {
        return Boolean.parseBoolean(properties.getProperty(MANAGEMENT_ENABLED, DEFAULT_MANAGEMENT_ENABLED));
    }

    public boolean isSynchronizedServices() {
        return Boolean.parseBoolean(properties.getProperty(SYNCHRONIZED_SERVICES, DEFAULT_SYNCHRONIZED_SERVICES));
    }

    public boolean isHolderEnabled() {
        return Boolean.parseBoolean(properties.getProperty(HOLDER_ENABLED, DEFAULT_HOLDER_ENABLED));
    }

    public long getHolderMaximumSize() {
        return Long.parseLong(properties.getProperty(HOLDER_MAXIMUM_SIZE, DEFAULT_HOLDER_MAXIMUM_SIZE));
    }

}