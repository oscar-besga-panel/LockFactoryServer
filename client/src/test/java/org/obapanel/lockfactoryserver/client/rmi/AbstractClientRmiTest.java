package org.obapanel.lockfactoryserver.client.rmi;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractClientRmiTest {

    @Mock
    private Registry registry;

    @Mock
    private TestRemote remote;

    private MockedStatic<LocateRegistry> mockedStaticLocateRegistry;

    private AtomicInteger remoteCallResponse = new AtomicInteger(0);

    private AtomicBoolean lauchError = new AtomicBoolean(false);

    @Before
    public void setUp() throws NotBoundException, RemoteException {
        mockedStaticLocateRegistry = Mockito.mockStatic(LocateRegistry.class);
        mockedStaticLocateRegistry.when(() -> LocateRegistry.getRegistry(anyString(), anyInt())).
                thenReturn(registry);
        when(registry.lookup(anyString())).thenReturn(remote);
        when(remote.remoteCall()).thenAnswer(ioc -> remoteCall());
    }

    private int remoteCall() throws RemoteException {
        if (lauchError.get()) {
            throw new RemoteException("Test remote exception");
        } else {
            return remoteCallResponse.get();
        }
    }

    @After
    public void tearsDown() {
        mockedStaticLocateRegistry.close();
    }


    @Test
    public void newClient1Test() throws NotBoundException, RemoteException {
        remoteCallResponse.set(55);
        TestAbstractClientRmi testAbstractClientRmi = new TestAbstractClientRmi("127.0.0.1",8088, "TestAbstractClientRmi1");
        String lookupName = testAbstractClientRmi.registryLookupName();
        String name = testAbstractClientRmi.getName();
        int remoteResult = testAbstractClientRmi.remoteCall();
        testAbstractClientRmi.close();
        assertEquals("TestAbstractClientRmiLookupName", lookupName);
        assertEquals("TestAbstractClientRmi1", name);
        assertEquals(55, remoteResult);
    }

    @Test
    public void newClient2Test() throws NotBoundException, RemoteException {
        remoteCallResponse.set(66);
        TestAbstractClientRmi testAbstractClientRmi = new TestAbstractClientRmi(registry, "TestAbstractClientRmi2");
        String name = testAbstractClientRmi.getName();
        String lookupName = testAbstractClientRmi.registryLookupName();
        int remoteResult = testAbstractClientRmi.remoteCall();
        testAbstractClientRmi.close();
        assertEquals("TestAbstractClientRmiLookupName", lookupName);
        assertEquals("TestAbstractClientRmi2", name);
        assertEquals(66, remoteResult);
    }

    @Test
    public void newClientTry1Test() throws NotBoundException, RemoteException {
        remoteCallResponse.set(77);
        String name = "";
        String lookupName = "";
        int remoteResult = 0;
        Exception errorHere = null;
        try {
            TestAbstractClientRmi testAbstractClientRmi = new TestAbstractClientRmi(registry, "TestAbstractClientRmiTry1");
            name = testAbstractClientRmi.getName();
            lookupName = testAbstractClientRmi.registryLookupName();
            remoteResult = testAbstractClientRmi.remoteCall();
        } catch (RemoteException re) {
            errorHere = re;
        }
        assertEquals("TestAbstractClientRmiLookupName", lookupName);
        assertEquals("TestAbstractClientRmiTry1", name);
        assertEquals(77, remoteResult);
        assertNull(errorHere);
    }

    @Test
    public void newClientTry2Test() throws NotBoundException, RemoteException {
        remoteCallResponse.set(88);
        lauchError.set(true);
        String name = "";
        String lookupName = "";
        int remoteResult = 0;
        Exception errorHere = null;
        try {
            TestAbstractClientRmi testAbstractClientRmi = new TestAbstractClientRmi(registry, "TestAbstractClientRmiTry2");
            name = testAbstractClientRmi.getName();
            lookupName = testAbstractClientRmi.registryLookupName();
            remoteResult = testAbstractClientRmi.remoteCall();
        } catch (RemoteException re) {
            errorHere = re;
        }
        assertEquals("TestAbstractClientRmiLookupName", lookupName);
        assertEquals("TestAbstractClientRmiTry2", name);
        assertEquals(0, remoteResult);
        assertNotNull(errorHere);
    }

    interface TestRemote extends Remote {

        int remoteCall() throws RemoteException;

    }

    class TestAbstractClientRmi extends AbstractClientRmi<TestRemote> {

        public TestAbstractClientRmi(String host, int port, String name) throws NotBoundException, RemoteException {
            super(host, port, name);
        }

        public TestAbstractClientRmi(Registry registry, String name) throws NotBoundException, RemoteException {
            super(registry, name);
        }

        @Override
        String registryLookupName() {
            return "TestAbstractClientRmiLookupName";
        }

        int remoteCall() throws RemoteException {
            return getServerRmi().remoteCall();
        }
    }


}
