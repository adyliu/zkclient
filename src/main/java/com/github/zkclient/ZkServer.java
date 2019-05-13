/**
 * Copyright 2010 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.zkclient;

import com.github.zkclient.exception.ZkException;
import org.apache.zookeeper.client.FourLetterWordMain;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.net.ConnectException;

public class ZkServer extends ZooKeeperServerMain {

    private final static Logger LOG = LoggerFactory.getLogger(ZkServer.class);
    ;

    public static final int DEFAULT_PORT = 2181;

    public static final int DEFAULT_TICK_TIME = 5000;

    public static final int DEFAULT_MIN_SESSION_TIMEOUT = 2 * DEFAULT_TICK_TIME;

    private final String _dataDir;

    private final String _logDir;

    private ZkClient _zkClient;

    private final int _port;

    private final int _tickTime;

    private final int _minSessionTimeout;

    private volatile boolean shutdown = false;

    private boolean daemon = true;

    public ZkServer(String dataDir, String logDir) {
        this(dataDir, logDir, DEFAULT_PORT);
    }

    public ZkServer(String dataDir, String logDir, int port) {
        this(dataDir, logDir, port, DEFAULT_TICK_TIME);
    }

    public ZkServer(String dataDir, String logDir, int port, int tickTime) {
        this(dataDir, logDir, port, tickTime, DEFAULT_MIN_SESSION_TIMEOUT);
    }

    public ZkServer(String dataDir, String logDir, int port, int tickTime, int minSessionTimeout) {
        _dataDir = dataDir;
        _logDir = logDir;
        _port = port;
        _tickTime = tickTime;
        _minSessionTimeout = minSessionTimeout;
    }

    public int getPort() {
        return _port;
    }

    @PostConstruct
    public void start() {
        shutdown = false;
        startZkServer();
        _zkClient = new ZkClient("localhost:" + _port, 10000);
    }

    private void startZkServer() {
        final int port = _port;
        if (ZkClientUtils.isPortFree(port)) {
            final File dataDir = new File(_dataDir);
            final File dataLogDir = new File(_logDir);
            dataDir.mkdirs();

            // single zk server
            LOG.info("Start single zookeeper server, port={} data={} ", port, dataDir.getAbsolutePath());
            //
            final ZooKeeperServerMain serverMain = this;
            final InnerServerConfig config = new InnerServerConfig();
            config.parse(new String[]{"" + port, dataDir.getAbsolutePath(), "" + _tickTime, "60"});
            config.setMinSessionTimeout(_minSessionTimeout);
            //
            final String threadName = "inner-zkserver-" + port;
            final Thread innerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        serverMain.runFromConfig(config);
                    } catch (Exception e) {
                        throw new ZkException("Unable to start single ZooKeeper server.", e);
                    }
                }
            }, threadName);
            innerThread.setDaemon(daemon);
            innerThread.start();
            //
            waitForServerUp(port, 30000, false);

        } else {
            throw new IllegalStateException("Zookeeper port " + port + " was already in use. Running in single machine mode?");
        }
    }

    @PreDestroy
    public void shutdown() {
        if (!shutdown) {
            shutdown = true;
            LOG.info("Shutting down ZkServer port={}...", _port);
            if (_zkClient != null) {
                try {
                    _zkClient.close();
                } catch (ZkException e) {
                    LOG.warn("Error on closing zkclient: " + e.getClass().getName());
                }
                _zkClient = null;
            }
            super.shutdown();
            waitForServerDown(_port, 30000, false);
            LOG.info("Shutting down ZkServer port={}...done", _port);
        }
    }


    public ZkClient getZkClient() {
        return _zkClient;
    }

    class InnerServerConfig extends ServerConfig {
        public void setMinSessionTimeout(int minSessionTimeout) {
            this.minSessionTimeout = minSessionTimeout;
        }
    }

    public static boolean waitForServerUp(int port, long timeout, boolean secure) {
        long start = System.currentTimeMillis();
        while (true) {
            try {
                // if there are multiple hostports, just take the first one
                String result = FourLetterWordMain.send4LetterWord("127.0.0.1", port, "stat");
                if (result.startsWith("Zookeeper version:") &&
                        !result.contains("READ-ONLY")) {
                    return true;
                }
            } catch (ConnectException e) {
                // ignore as this is expected, do not log stacktrace
                LOG.debug("server {} not up: {}", port, e.toString());
            } catch (Exception e) {
                // ignore as this is expected
                LOG.info("server {} not up", port, e);
            }

            if (System.currentTimeMillis() > start + timeout) {
                break;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        return false;
    }

    public static boolean waitForServerDown(int port, long timeout, boolean secure) {
        long start = System.currentTimeMillis();
        while (true) {
            try {
                FourLetterWordMain.send4LetterWord("127.0.0.1", port, "stat");
            } catch (Exception e) {
                return true;
            }

            if (System.currentTimeMillis() > start + timeout) {
                break;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        return false;
    }
}
