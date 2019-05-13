package com.github.zkclient;

import java.io.File;
import java.util.Arrays;

public class ZkServerDemo {
    public static void main(String[] args) throws  Exception{
        File dataDir = new File("/tmp/zkdemo");
        dataDir.mkdirs();
        ZkServer server = new ZkServer(dataDir.getAbsolutePath(), dataDir.getAbsolutePath());
        server.start();

        ZkClient client =  server.getZkClient();
        client.createPersistent("/a", true);
        byte[] dat = client.readData("/a");
        System.out.println(Arrays.toString(dat));
        client.writeData("/a", "OK".getBytes());
        System.out.println("agian="+Arrays.toString(dat));
        //server.shutdown();
    }
}
