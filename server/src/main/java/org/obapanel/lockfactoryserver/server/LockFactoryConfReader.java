package org.obapanel.lockfactoryserver.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LockFactoryConfReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockFactoryServerMain.class);

    public static final String LOCKFACTORY_DEFAULT_PROPERTIES = "lockFactory.properties";

    private final String argsPath;

    public static LockFactoryConfiguration generate(String[] args) {
        LockFactoryConfReader confReader = new LockFactoryConfReader(args);
        return confReader.readLockFactoryConfiguration();
    }

    public LockFactoryConfReader() {
        argsPath = null;
    }

    public LockFactoryConfReader(String[] args) {
        if (args == null || args.length == 0) {
            argsPath = null;
        } else {
            argsPath = args[0];
        }
    }

    public LockFactoryConfiguration readLockFactoryConfiguration() {
        Properties properties = read();
        if (properties != null){
            return new LockFactoryConfiguration(properties);
        } else {
            return new LockFactoryConfiguration();
        }
    }


    Properties read() {
        Properties properties = null;
        if (argsPath != null && !argsPath.isEmpty()) {
            properties = readFromFile(argsPath);
        }
        if (properties == null) {
            properties = readFromFile(LOCKFACTORY_DEFAULT_PROPERTIES);
        }
        if (properties == null && (argsPath != null) && !argsPath.isEmpty()) {
            properties = readFromClasspath(argsPath);
        }
        if (properties == null) {
            properties = readFromClasspath(LOCKFACTORY_DEFAULT_PROPERTIES);
        }
        return properties;
    }

    Properties readFromFile(String path) {
        try (InputStream input = new FileInputStream(path)) {
            return loadProperties(input, path, "FILE");
        } catch (IOException e) {
            LOGGER.warn("readFromFile erroneous path {} error {}", path, e.getMessage());
            return null;
        }
    }


    Properties readFromClasspath(String resource) {
        try (InputStream input = LockFactoryServerMain.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IOException("Resource not found (" + resource +") in classpath");
            }
            return loadProperties(input, resource, "CLASSPATH");
        } catch (IOException e) {
            LOGGER.warn("readFromClasspath erroneous resource {} error {}", resource, e.getMessage());
            return null;
        }
    }

    Properties loadProperties(InputStream input, String resourcePath, String from) throws IOException {
        Properties properties = new Properties();
        properties.load(input);
        LOGGER.debug("loadProperties successful resourcePath {} from {}", resourcePath, from);
        return properties;
    }



}
