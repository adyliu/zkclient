/**
 * Copyright 2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.zkclient;

import java.io.IOException;
import java.net.*;

import com.github.zkclient.exception.ZkInterruptedException;

public class ZkClientUtils {

    private ZkClientUtils() {}

    public static enum ZkVersion {
        V33, V34
    }

    public static final ZkVersion zkVersion;

    static {
        ZkVersion version = null;
        try {
            Class.forName("org.apache.zookeeper.OpResult");
            version = ZkVersion.V34;
        }
        catch (ClassNotFoundException e) {
            version = ZkVersion.V33;
        }
        finally {
            zkVersion = version;
        }

    }

    public static RuntimeException convertToRuntimeException(Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        retainInterruptFlag(e);
        return new RuntimeException(e);
    }

    /**
     * This sets the interrupt flag if the catched exception was an
     * {@link InterruptedException}. Catching such an exception always clears
     * the interrupt flag.
     * 
     * @param catchedException
     *            The catched exception.
     */
    public static void retainInterruptFlag(Throwable catchedException) {
        if (catchedException instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    public static void rethrowInterruptedException(Throwable e) throws InterruptedException {
        if (e instanceof InterruptedException) {
            throw (InterruptedException) e;
        }
        if (e instanceof ZkInterruptedException) {
            throw (ZkInterruptedException) e;
        }
    }

    public static String leadingZeros(long number, int numberOfLeadingZeros) {
        return String.format("%0" + numberOfLeadingZeros + "d", number);
    }

    public final static String OVERWRITE_HOSTNAME_SYSTEM_PROPERTY = "zkclient.hostname.overwritten";

    public static boolean isPortFree(int port) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("localhost", port), 200);
            socket.close();
            return false;
        }
        catch (SocketTimeoutException e) {
            return true;
        }
        catch (ConnectException e) {
            return true;
        }
        catch (SocketException e) {
            if (e.getMessage().equals("Connection reset by peer")) {
                return true;
            }
            throw new RuntimeException(e);
        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLocalhostName() {
        String property = System.getProperty(OVERWRITE_HOSTNAME_SYSTEM_PROPERTY);
        if (property != null && property.trim().length() > 0) {
            return property;
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (final UnknownHostException e) {
            throw new RuntimeException("unable to retrieve localhost name");
        }
    }
}
