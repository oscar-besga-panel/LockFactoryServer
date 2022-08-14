package org.obapanel.lockfactoryserver.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Read configuration from file and properties and returns a properties object
 * It can return the configuration object as well
 */
public class LockFactoryConfReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockFactoryServerMain.class);

    public static final String LOCKFACTORY_DEFAULT_PROPERTIES = "lockFactory.properties";

    private final String fileName;

    /**
     * Generate from main class arguments
     * If there's an argument, it will use it as file name (only first argument)
     * @param args Arguments from main from command line
     * @return configuration object
     */
    public static LockFactoryConfiguration generateFromArguments(String[] args) {
        LockFactoryConfReader lockFactoryConfReader;
        if (args == null || args.length == 0) {
            lockFactoryConfReader = new LockFactoryConfReader();
        } else {
            lockFactoryConfReader = new LockFactoryConfReader(args[0]);
        }
        return lockFactoryConfReader.generateLockFactoryConfiguration();
    }

    /**
     * Empty constructor with no filename
     */
    public LockFactoryConfReader() {
        fileName = null;
    }

    /**
     * Constructor with filename
     * @param fileName file name path
     */
    public LockFactoryConfReader(String fileName) {
        this.fileName = fileName;
    }

    /**
     * It generates configuration object from properties
     * If not file / no properties, a new configuration with default values is returned
     * @return LockFactoryConfiguration
     */
    public LockFactoryConfiguration generateLockFactoryConfiguration() {
        Properties properties = read();
        if (properties != null){
            return new LockFactoryConfiguration(properties);
        } else {
            return new LockFactoryConfiguration();
        }
    }

    /**
     * Reads the properties
     * 1- Tries to read given filename if exists
     *    If exists, these file is read and properties are returned
     *    If not...
     * 2- Tries to read default filename if exists
     *    If exists, these file is read and properties are returned
     *    If not...
     * 3- Tries to read default filename from classpath
     *    If exists, these file is read and properties are returned
     *    If not
     * If nothing is read, null is returned
     * @return null or properties from file
     */
    Properties read() {
        Properties properties = null;
        if (fileName != null && !fileName.isEmpty()) {
            properties = readFromFile(fileName);
        }
        if (properties == null) {
            properties = readFromFile(LOCKFACTORY_DEFAULT_PROPERTIES);
        }
        if (properties == null) {
            properties = readFromClasspath(LOCKFACTORY_DEFAULT_PROPERTIES);
        }
        return properties;
    }

    /**
     * Read from file, readed properties are loaded or null is returned
     * @param path file
     * @return properties or null
     */
    Properties readFromFile(String path) {
        try (InputStream input = Files.newInputStream(Paths.get(path))) {
            return loadProperties(input, path, "FILE");
        } catch (IOException e) {
            LOGGER.warn("readFromFile erroneous path {} error {}", path, e.getMessage());
            return null;
        }
    }

    /**
     * Read from classpath, readed properties are loaded or null is returned
     * @param resource file
     * @return properties or null
     */
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

    /**
     * Try to read data into properties object
     * @param input InputStream
     * @param resourcePath name of resoruce, file or classpath
     * @param from CLASSPATH or FILE, type of resource
     * @return loaded properties object
     * @throws IOException If lecture isn't fine
     */
    Properties loadProperties(InputStream input, String resourcePath, String from) throws IOException {
        Properties properties = new Properties();
        properties.load(input);
        LOGGER.debug("loadProperties successful resourcePath {} from {}", resourcePath, from);
        return properties;
    }



}
