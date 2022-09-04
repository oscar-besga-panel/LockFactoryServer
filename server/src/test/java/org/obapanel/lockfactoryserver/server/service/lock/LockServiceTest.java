package org.obapanel.lockfactoryserver.server.service.lock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.Services;

import static org.junit.Assert.assertEquals;

public class LockServiceTest {

    private LockService lockService;

    @Before
    public void setup() {
        lockService = new LockService(new LockFactoryConfiguration());
    }

    @After
    public void tearsDown() throws Exception {
        lockService.shutdown();
        lockService = null;
    }

    @Test
    public void getTypeTest() {
        Services services = lockService.getType();
        assertEquals(Services.LOCK, services);
        assertEquals(Services.LOCK.getServiceClass(), lockService.getClass());
    }

    @Test
    public void shutdownTest() throws Exception {
        lockService.shutdown();
    }

}
