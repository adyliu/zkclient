/**
 * 
 */
package com.github.zkclient;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.github.zkclient.exception.ZkNoNodeException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 
 * @author adyliu(imxylz@gmail.com)
 * @since 2012-12-3
 */
public class ZkClientTest {
    static {
        System.setProperty("zookeeper.preAllocSize", "1024");// 1M data log
    }
    final Logger logger = Logger.getLogger(ZkClientTest.class);
    private static final AtomicInteger counter = new AtomicInteger();
    //
    private ZkServer server;
    private ZkClient client;

    //
    private static void deleteFile(File f) throws IOException {
        if (f.isFile()) {
            f.delete();
            //System.out.println("[DELETE FILE] "+f.getPath());
        } else if (f.isDirectory()) {
            for (File fs : f.listFiles()) {
                deleteFile(fs);
            }
            f.delete();
            //System.out.println("[DELETE DIRECTORY] "+f.getPath());
        }
    }

    private static ZkServer startZkServer(String testName, int port) throws IOException {
        String dataPath = "build/test/" + testName + "/data";
        String logPath = "build/test/" + testName + "/log";
        File dataDir = new File(".", dataPath).getCanonicalFile();
        File logDir = new File(".", logPath).getCanonicalFile();
        deleteFile(dataDir);
        deleteFile(logDir);
        //start the server with 100ms session timeout
        ZkServer zkServer = new ZkServer(dataDir.getPath(), logDir.getPath(), port, ZkServer.DEFAULT_TICK_TIME, 100);
        zkServer.start();
        return zkServer;
    }

    @AfterClass
    public static void cleanup() throws IOException{
        deleteFile(new File(".","build/test").getCanonicalFile());
    }
    
    @Before
    public void setUp() throws Exception {
        this.server = startZkServer("server_" + counter.incrementAndGet(), 4711);
        this.client = this.server.getZkClient();
    }

    @After
    public void tearDown() throws Exception {
        if (this.server != null)
            this.server.shutdown();
    }

    /**
     * Test method for
     * {@link com.github.zkclient.ZkClient#subscribeChildChanges(java.lang.String, com.github.zkclient.IZkChildListener)}
     * .
     */
    @Test
    public void testSubscribeChildChanges() throws Exception{
        String path = "/a";
        final AtomicInteger count = new AtomicInteger(0);
        final ArrayList<String> children = new ArrayList<String>();
        IZkChildListener listener = new IZkChildListener() {
            public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
                count.incrementAndGet();
                children.clear();
                if(currentChildren!=null)
                children.addAll(currentChildren);
            }
        };
        //
        client.subscribeChildChanges(path, listener);
        //
        client.createPersistent(path);
        //wait some time to make sure the event was triggered
        TestUtil.waitUntil(1, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return count.get();
            }
        }, TimeUnit.SECONDS, 5);
        //
        assertEquals(1,count.get());
        assertEquals(0,children.size());
        //
        //create a child node
        count.set(0);
        client.createPersistent(path+"/child1");
        TestUtil.waitUntil(1, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return count.get();
            }
        }, TimeUnit.SECONDS, 5);
        //
        assertEquals(1,count.get());
        assertEquals(1,children.size());
        assertEquals("child1",children.get(0));
        //
        // create another child node and delete the node
        count.set(0);
        client.createPersistent(path+"/child2");
        client.deleteRecursive(path);
        //
        Boolean eventReceived = TestUtil.waitUntil(true, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return count.get()>0&&children.size()==0;
            }
        }, TimeUnit.SECONDS, 5);
        assertTrue(eventReceived);
        assertEquals(0,children.size());
        // ===========================================
        // do it again and check the listener validate
        // ===========================================
        count.set(0);
        client.createPersistent(path);
        //
        eventReceived = TestUtil.waitUntil(true, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return count.get()>0;
            }
        }, TimeUnit.SECONDS, 5);
        assertTrue(eventReceived);
        assertEquals(0,children.size());
        //
        // now create the first node
        count.set(0);
        client.createPersistent(path+"/child");
        //
        eventReceived = TestUtil.waitUntil(true, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return count.get()>0;
            }
        }, TimeUnit.SECONDS, 5);
        assertTrue(eventReceived);
        assertEquals(1,children.size());
        assertEquals("child",children.get(0));
        //
        // delete root node 
        count.set(0);
        client.deleteRecursive(path);
        //
        eventReceived = TestUtil.waitUntil(true, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return count.get()>0;
            }
        }, TimeUnit.SECONDS, 5);
        assertTrue(eventReceived);
        assertEquals(0,children.size());
    }

    static class Holder<T> {
        T t;
        public void set(T t) {
            this.t = t;
        }
        public T get() {
            return t;
        }
    }

    static byte[] toBytes(String s) {
        try {
            return s!=null?s.getBytes("UTF-8"):null;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    static String toString(byte[] b) {
        try {
            return b!=null?new String(b,"UTF-8"):null;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Test method for
     * {@link com.github.zkclient.ZkClient#subscribeDataChanges(java.lang.String, com.github.zkclient.IZkDataListener)}
     * .
     */
    @Test
    public void testSubscribeDataChanges() throws Exception{
        String path = "/a";
        final AtomicInteger countChanged = new AtomicInteger(0);
        final AtomicInteger countDeleted = new AtomicInteger(0);
        final Holder<String> holder = new Holder<String>();
        IZkDataListener listener = new IZkDataListener() {
            public void handleDataDeleted(String dataPath) throws Exception {
                countDeleted.incrementAndGet();
                holder.set(null);
            }
            public void handleDataChange(String dataPath, byte[] data) throws Exception {
                countChanged.incrementAndGet();
                holder.set(ZkClientTest.toString(data));
            }
        };
        client.subscribeDataChanges(path, listener);
        //
        // create the node
        client.createPersistent(path,toBytes("aaa"));
        //
        //wait some time to make sure the event was triggered
        TestUtil.waitUntil(1, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return countChanged.get();
            }
        }, TimeUnit.SECONDS, 5);
        assertEquals(1,countChanged.get());
        assertEquals(0,countDeleted.get());
        assertEquals("aaa",holder.get());
        //
        countChanged.set(0);
        countDeleted.set(0);
        //
        client.delete(path);
        TestUtil.waitUntil(1, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return countDeleted.get();
            }
        }, TimeUnit.SECONDS, 5);
        assertEquals(0,countChanged.get());
        assertEquals(1,countDeleted.get());
        assertNull(holder.get());
        // ===========================================
        // do it again and check the listener validate
        // ===========================================
        countChanged.set(0);
        countDeleted.set(0);
        client.createPersistent(path,toBytes("bbb"));
        TestUtil.waitUntil(1, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return countChanged.get();
            }
        }, TimeUnit.SECONDS, 5);
        assertEquals(1,countChanged.get());
        assertEquals("bbb",holder.get());
        //
        countChanged.set(0);
        client.writeData(path, toBytes("ccc"));
        //
        TestUtil.waitUntil(1, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return countChanged.get();
            }
        }, TimeUnit.SECONDS, 5);
        assertEquals(1,countChanged.get());
        assertEquals("ccc",holder.get());
    }


    /**
     * Test method for
     * {@link com.github.zkclient.ZkClient#createPersistent(java.lang.String, boolean)}
     * .
     */
    @Test
    public void testCreatePersistent() {
        final String path = "/a/b";
        try {
            client.createPersistent(path, false);
            fail("should throw exception");
        } catch (ZkNoNodeException e) {
            assertFalse(client.exists(path));
        }
        client.createPersistent(path, true);
        assertTrue(client.exists(path));
    }

    /**
     * Test method for
     * {@link com.github.zkclient.ZkClient#createPersistent(java.lang.String, byte[])}
     * .
     */
    @Test
    public void testCreatePersistentStringByteArray() {
        String path = "/a";
        client.createPersistent(path, toBytes("abc"));
        assertEquals("abc",toString(client.readData(path)));
        //
    }

    /**
     * Test method for
     * {@link com.github.zkclient.ZkClient#createPersistentSequential(java.lang.String, byte[])}
     * .
     */
    @Test
    public void testCreatePersistentSequential() {
        String path = "/a";
        String npath = client.createPersistentSequential(path, toBytes("abc"));
        assertTrue(npath!=null&&npath.length()>0);
        npath = client.createPersistentSequential(path, toBytes("abc"));
        assertEquals("abc",toString(client.readData(npath)));
    }

    /**
     * Test method for
     * {@link com.github.zkclient.ZkClient#createEphemeral(java.lang.String)}.
     */
    @Test
    public void testCreateEphemeralString() {
        String path = "/a";
        client.createEphemeral(path);
        Stat stat = new Stat();
        client.readData(path, stat);
        assertTrue(stat.getEphemeralOwner()>0);
    }

    /**
     * Test method for
     * {@link com.github.zkclient.ZkClient#create(java.lang.String, byte[], org.apache.zookeeper.CreateMode)}
     * .
     */
    @Test
    public void testCreate() {
        String path = "/a";
        client.create(path, toBytes("abc"), CreateMode.PERSISTENT);
        assertEquals("abc",toString(client.readData(path)));
    }

   
    @Test
    public void testCreateEphemeralSequential() {
        String path = "/a";
        String npath = client.createEphemeralSequential(path, toBytes("abc"));
        assertTrue(npath!=null&&npath.startsWith("/a"));
        Stat stat = new Stat();
        assertArrayEquals(toBytes("abc"),client.readData(npath, stat));
        assertTrue(stat.getEphemeralOwner()>0);
    }

    

    /**
     * Test method for
     * {@link com.github.zkclient.ZkClient#getChildren(java.lang.String)}.
     */
    @Test
    public void testGetChildrenString() {
        String path = "/a";
        client.createPersistent(path+"/ch1",true);
        client.createPersistent(path+"/ch2");
        client.createPersistent(path+"/ch3");
        List<String> children = client.getChildren(path);
        assertEquals(3,children.size());
        assertEquals(3,client.countChildren(path));
    }

    

    /**
     * Test method for
     * {@link com.github.zkclient.ZkClient#exists(java.lang.String)}.
     */
    @Test
    public void testExistsString() {
        String path = "/a";
        assertFalse(client.exists(path));
        client.createPersistent(path);
        assertTrue(client.exists(path));
        client.delete(path);
        assertFalse(client.exists(path));
    }

    /**
     * Test method for
     * {@link com.github.zkclient.ZkClient#deleteRecursive(java.lang.String)}.
     */
    @Test
    public void testDeleteRecursive() {
        String path = "/a/b/c";
        client.createPersistent(path, true);
        assertTrue(client.exists(path));
        assertTrue(client.deleteRecursive("/a"));
        assertFalse(client.exists(path));
        assertFalse(client.exists("/a/b"));
        assertFalse(client.exists("/a"));
    }

    /**
     * Test method for
     * {@link com.github.zkclient.ZkClient#waitUntilExists(java.lang.String, java.util.concurrent.TimeUnit, long)}
     * .
     */
    @Test
    public void testWaitUntilExists() {
        final String path = "/a";
        new Thread() {
            public void run() {
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
                client.createPersistent(path);
            }
        }.start();
        assertTrue(client.waitUntilExists(path, TimeUnit.SECONDS, 4));
        assertTrue(client.exists(path));
        //
        assertFalse(client.waitUntilExists("/notexists", TimeUnit.SECONDS, 1));
    }

    /**
     * Test method for {@link com.github.zkclient.ZkClient#waitUntilConnected()}
     * .
     */
    @Test
    public void testWaitUntilConnected() {
        ZkClient client2 = new ZkClient("localhost:4711",4000);
        assertTrue(client2.waitUntilConnected());
        server.shutdown();
        //
        assertTrue(client2.waitForKeeperState(KeeperState.Disconnected, 1, TimeUnit.SECONDS));
        //
        assertFalse(client2.waitUntilConnected(1, TimeUnit.SECONDS));
        client2.close();
    }

    

    /**
     * Test method for
     * {@link com.github.zkclient.ZkClient#readData(java.lang.String, org.apache.zookeeper.data.Stat)}
     * .
     */
    @Test
    public void testReadDataStringStat() {
        client.createPersistent("/a", "data".getBytes());
        Stat stat = new Stat();
        client.readData("/a", stat);
        assertEquals(0, stat.getVersion());
        assertTrue(stat.getDataLength() > 0);
    }

   

    /**
     * Test method for {@link com.github.zkclient.ZkClient#numberOfListeners()}.
     */
    @Test
    public void testNumberOfListeners() {
        IZkChildListener zkChildListener = new AbstractListener() {};
        client.subscribeChildChanges("/", zkChildListener);
        assertEquals(1,client.numberOfListeners());
        //
        IZkDataListener zkDataListener = new AbstractListener() {};
        client.subscribeDataChanges("/a", zkDataListener);
        assertEquals(2,client.numberOfListeners());
        //
        client.subscribeDataChanges("/b", zkDataListener);
        assertEquals(3,client.numberOfListeners());
        //
        IZkStateListener zkStateListener = new AbstractListener() {};
        client.subscribeStateChanges(zkStateListener);
        assertEquals(4,client.numberOfListeners());
        //
        client.unsubscribeChildChanges("/", zkChildListener);
        assertEquals(3,client.numberOfListeners());
        //
        client.unsubscribeAll();
        assertEquals(0,client.numberOfListeners());
    }

    /**
     * Test method for {@link com.github.zkclient.ZkClient#getZooKeeper()}.
     */
    @Test
    public void testGetZooKeeper() {
        assertTrue(client.getZooKeeper()!=null);
    }
    
    @Test
    public void testRetryUnitConnected_SessionExpiredException() {
        int sessionTimeout = 200;
        Gateway gateway = new Gateway(4712,4711);
        gateway.start();
        //
        final ZkClient client2 = new ZkClient("localhost:4712",sessionTimeout,15000);
        gateway.stop();
        //
        //start the server after 600ms
        new DeferredGatewayStarter(gateway, sessionTimeout*3).start();
        //
        final Boolean connected = client2.retryUntilConnected(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                client2.createPersistent("/abc");
                return Boolean.TRUE;
            }
        });
        assertTrue(connected);
        assertTrue(client2.exists("/abc"));
        client2.close();
        gateway.stop();
        //
    }
    
    
    @Test
    public void testChildListenerAfterSessionExpiredException() throws Exception{
        final int sessionTimeout = 200;
        ZkClient connectedClient = server.getZkClient();
        connectedClient.createPersistent("/root");
        //
        Gateway gateway = new Gateway(4712,4711);
        gateway.start();
        //
        final ZkClient disconnectedClient = new ZkClient("localhost:"+4712,sessionTimeout,15000);
        final Holder<List<String>> children = new Holder<List<String>>();
        disconnectedClient.subscribeChildChanges("/root", new IZkChildListener() {
            
            @Override
            public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
                children.set(currentChildren);
            }
        });
        gateway.stop();//
        //
        // the connected client created a new child node
        connectedClient.createPersistent("/root/node1");
        //
        // wait for 3x sessionTImeout, the session should have expired
        Thread.sleep(3*sessionTimeout);
        //
        // now start the gateway
        gateway.start();
        //
        Boolean hasOneChild = TestUtil.waitUntil(true, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return children.get()!=null&&children.get().size()==1;
            }
        }, TimeUnit.SECONDS, 5);
        //
        assertTrue(hasOneChild);
        assertEquals("node1",children.get().get(0));
        assertEquals("node1",disconnectedClient.getChildren("/root").get(0));
        //
        disconnectedClient.close();
        gateway.stop();
    }

}
