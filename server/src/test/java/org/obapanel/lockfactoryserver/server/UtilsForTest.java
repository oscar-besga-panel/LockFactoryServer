package org.obapanel.lockfactoryserver.server;

import java.util.Properties;

public class UtilsForTest {

    public static LockFactoryConfiguration createLockFactoryConfiguration(String... configs) {
        Properties properties = new Properties();
        for (int i=0; i < configs.length; i++) {
            String name = configs[i];
            i++;
            if (i < configs.length ) {
                String value = configs[i];
                properties.setProperty(name, value);
            }
        }
        return new LockFactoryConfiguration(properties);
    }

}
