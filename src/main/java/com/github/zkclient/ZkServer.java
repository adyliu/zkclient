/**
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.zkclient;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.zookeeper.server.ZooKeeperServer;

import com.github.zkclient.exception.ZkException;
import com.github.zkclient.exception.ZkInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkServer {

    private final static Logger LOG = LoggerFactory.getLogger(ZkServer.class);;

    public static final int DEFAULT_PORT = 2181;

    public static final int DEFAULT_TICK_TIME = 5000;

    public static final int DEFAULT_MIN_SESSION_TIMEOUT = 2 * DEFAULT_TICK_TIME;

    private final String _dataDir;

    private final String _logDir;

    private ZooKeeperServer _zk;

    private ServerCnxnFactory _nioFactory;

    private ZkClient _zkClient;

    private final int _port;

    private final int _tickTime;

    private final int _minSessionTimeout;

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
        startZkServer();
        _zkClient = new ZkClient("localhost:" + _port, 10000);
    }

    private void startZkServer() {
        final int port = _port;
        if (ZkClientUtils.isPortFree(port)) {
            final File dataDir = new File(_dataDir);
            final File dataLogDir = new File(_logDir);
            dataDir.mkdirs();
            dataLogDir.mkdirs();

            // single zk server
            LOG.info("Start single zookeeper server...");
            LOG.info("data dir: " + dataDir.getAbsolutePath());
            LOG.info("data log dir: " + dataLogDir.getAbsolutePath());
            startSingleZkServer(_tickTime, dataDir, dataLogDir, port);
        } else {
            throw new IllegalStateException("Zookeeper port " + port + " was already in use. Running in single machine mode?");
        }
    }

    private void startSingleZkServer(final int tickTime, final File dataDir, final File dataLogDir, final int port) {
        try {
            _zk = new ZooKeeperServer(dataDir, dataLogDir, tickTime);
            _zk.setMinSessionTimeout(_minSessionTimeout);
            _nioFactory = ServerCnxnFactory.createFactory(port, 60);
            _nioFactory.startup(_zk);
        } catch (IOException e) {
            throw new ZkException("Unable to start single ZooKeeper server.", e);
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        }
    }

    @PreDestroy
    public void shutdown() {
        ZooKeeperServer zk = _zk;
        if (zk == null) {
            LOG.warn("shutdown duplication");
            return;
        }else {
            _zk = null;
        }
        LOG.info("Shutting down ZkServer...");
        try {
            _zkClient.close();
        } catch (ZkException e) {
            LOG.warn("Error on closing zkclient: " + e.getClass().getName());
        }
        if (_nioFactory != null) {
            _nioFactory.shutdown();
            try {
                _nioFactory.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            _nioFactory = null;
        }
        zk.shutdown();
        if (zk.getZKDatabase() != null) {
            try {
                // release file description
                zk.getZKDatabase().close();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        LOG.info("Shutting down ZkServer...done");
    }

    public ZkClient getZkClient() {
        return _zkClient;
    }
}
