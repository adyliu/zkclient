package com.github.zkclient;


import java.io.IOException;
import java.net.ServerSocket;

/**
 * copy from https://github.com/adyliu/jafka/blob/master/src/test/java/com/sohu/jafka/PortUtils.java
 *
 * @author adyliu (imxylz@gmail.com)
 * @since 2013-04-25
 */
public class PortUtils {

    public static int checkAvailablePort(int port) {
        while (port < 65500) {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(port);
                return port;
            } catch (IOException e) {
                //ignore error
            } finally {
                try {
                    if (serverSocket != null)
                        serverSocket.close();
                } catch (IOException e) {
                    //ignore
                }
            }
            port++;
        }
        throw new RuntimeException("no available port");
    }

    public static void main(String[] args) {
        int port = checkAvailablePort(80);
        System.out.println("The available port is " + port);
    }
}
