/**
 *
 */
package com.github.zkclient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import org.apache.zookeeper.server.ZooKeeperServer;

import com.github.zkclient.ZkClientUtils.ZkVersion;

/**
 * Adapter for zookeeper 3.3.x/3.4.x
 *
 * @author adyliu (imxylz@gmail.com)
 * @since 2012-11-27
 */
public class ServerCnxnFactory {

    private static final String zk33Factory = "org.apache.zookeeper.server.NIOServerCnxn$Factory";
    private static final String zk34Factory = "org.apache.zookeeper.server.ServerCnxnFactory";

    private Object target = null;
    private Method shutdownMethod = null;
    private Method joinMethod = null;
    private Method startupMethod = null;

    public void startup(ZooKeeperServer server) throws InterruptedException {
        try {
            startupMethod.invoke(target, server);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof InterruptedException) {
                throw (InterruptedException) t;
            }
            throw new RuntimeException(t);
        }
    }

    private static ServerCnxnFactory createFactory(final String hostname, int port, int maxcc) {
        ServerCnxnFactory factory = new ServerCnxnFactory();

        try {
            if (ZkClientUtils.zkVersion == ZkVersion.V33) {
                Class<?> clazz = Class.forName(zk33Factory);
                factory.target = clazz.getDeclaredConstructor(InetSocketAddress.class).newInstance(new InetSocketAddress(port));
                factory.shutdownMethod = clazz.getDeclaredMethod("shutdown", new Class[0]);
                factory.joinMethod = clazz.getMethod("join", new Class[0]);
                factory.startupMethod = clazz.getDeclaredMethod("startup", ZooKeeperServer.class);
            } else {
                Class<?> clazz = Class.forName(zk34Factory);
                factory.target = clazz.getMethod("createFactory", int.class, int.class).invoke(null, port, maxcc);
                factory.shutdownMethod = clazz.getDeclaredMethod("shutdown", new Class[0]);
                factory.joinMethod = clazz.getDeclaredMethod("join", new Class[0]);
                factory.startupMethod = clazz.getMethod("startup", ZooKeeperServer.class);
            }

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("unsupported zookeeper version", e);
        } catch (SecurityException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("unsupported zookeeper version", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("unsupported zookeeper version", e);
        }

        return factory;
    }

    public static ServerCnxnFactory createFactory(int port, int maxcc) {
        return createFactory(null, port, maxcc);
    }

    public void shutdown() {
        try {
            shutdownMethod.invoke(target, new Object[0]);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            throw new RuntimeException(t);
        }
    }

    public void join() throws InterruptedException {
        try {
            joinMethod.invoke(target, new Object[0]);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof InterruptedException) {
                throw (InterruptedException) t;
            }
            throw new RuntimeException(t);
        }
    }

    public static void main(String[] args) throws Exception {
        ServerCnxnFactory factory = ServerCnxnFactory.createFactory(8123, 60);
        factory.shutdown();
    }
}
