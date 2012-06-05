/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.zkclient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.github.zkclient.exception.ZkException;
import com.github.zkclient.exception.ZkInterruptedException;
import com.github.zkclient.exception.ZkNodeExistsException;
import com.github.zkclient.exception.ZkTimeoutException;

/**
 * @author adyliu (imxylz@gmail.com)
 * @since 2.0
 */
public interface IZkClient<T> {

    int DEFAULT_CONNECTION_TIMEOUT = 10000;

    int DEFAULT_SESSION_TIMEOUT = 30000;

    /**
     * Close the client.
     * 
     * @throws ZkInterruptedException
     */
    void close();

    /**
     * Connect to ZooKeeper.
     * 
     * @param maxMsToWaitUntilConnected
     * @param watcher
     * @throws ZkInterruptedException if the connection timed out due to thread interruption
     * @throws ZkTimeoutException if the connection timed out
     * @throws IllegalStateException if the connection timed out due to thread interruption
     */
    void connect(final long maxMsToWaitUntilConnected, Watcher watcher) throws ZkInterruptedException,
            ZkTimeoutException, IllegalStateException;

    /**
     * Counts number of children for the given path.
     * 
     * @param path
     * @return number of children or 0 if path does not exist.
     */
    int countChildren(String path);

    /**
     * Create a node.
     * 
     * @param path
     * @param data
     * @param mode
     * @return create node's path
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection
     *         got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *         thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    String create(final String path, T data, final CreateMode mode) throws ZkInterruptedException,
            IllegalArgumentException, ZkException, RuntimeException;

    /**
     * Create an ephemeral node.
     * 
     * @param path
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection
     *         got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *         thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    void createEphemeral(final String path) throws ZkInterruptedException, IllegalArgumentException, ZkException,
            RuntimeException;

    /**
     * Create an ephemeral node.
     * 
     * @param path
     * @param data
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection
     *         got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *         thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    void createEphemeral(final String path, final T data) throws ZkInterruptedException, IllegalArgumentException,
            ZkException, RuntimeException;

    /**
     * Create an ephemeral, sequential node.
     * 
     * @param path
     * @param data
     * @return created path
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection
     *         got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *         thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    String createEphemeralSequential(final String path, final T data) throws ZkInterruptedException,
            IllegalArgumentException, ZkException, RuntimeException;

    /**
     * Create a persistent node.
     * 
     * @param path
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection
     *         got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *         thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    void createPersistent(String path) throws ZkInterruptedException, IllegalArgumentException, ZkException,
            RuntimeException;

    /**
     * Create a persistent node.
     * 
     * @param path
     * @param createParents if true all parent dirs are created as well and no
     *        {@link ZkNodeExistsException} is thrown in case the path already exists
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection
     *         got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *         thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    void createPersistent(String path, boolean createParents) throws ZkInterruptedException, IllegalArgumentException,
            ZkException, RuntimeException;

    /**
     * Create a persistent node.
     * 
     * @param path
     * @param data
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection
     *         got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *         thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    void createPersistent(String path, T data) throws ZkInterruptedException, IllegalArgumentException, ZkException,
            RuntimeException;

    /**
     * Create a persistent, sequental node.
     * 
     * @param path
     * @param data
     * @return create node's path
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection
     *         got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *         thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    String createPersistentSequential(String path, T data) throws ZkInterruptedException, IllegalArgumentException,
            ZkException, RuntimeException;

    boolean delete(final String path);

    boolean deleteRecursive(String path);

    boolean exists(final String path);

    List<String> getChildren(String path);

    List<String> getChildren(final String path, final boolean watch);

    long getCreationTime(String path);

    int numberOfListeners();

    T readData(String path);

    T readData(String path, boolean returnNullIfPathNotExists);

    T readData(String path, Stat stat);

    List<String> subscribeChildChanges(String path, IZkChildListener listener);

    void subscribeDataChanges(String path, IZkDataListener listener);

    void subscribeStateChanges(final IZkStateListener listener);

    void unsubscribeAll();

    void unsubscribeChildChanges(String path, IZkChildListener childListener);

    void unsubscribeDataChanges(String path, IZkDataListener dataListener);

    void unsubscribeStateChanges(IZkStateListener stateListener);

    /**
     * Updates data of an existing znode. The current content of the znode is passed to the
     * {@link DataUpdater} that is passed into this method, which returns the new content. The
     * new content is only written back to ZooKeeper if nobody has modified the given znode in
     * between. If a concurrent change has been detected the new data of the znode is passed to
     * the updater once again until the new contents can be successfully written back to
     * ZooKeeper.
     * 
     * @param <T>
     * @param path The path of the znode.
     * @param updater Updater that creates the new contents.
     */
    void updateDataSerialized(String path, DataUpdater<T> updater);

    boolean waitForKeeperState(KeeperState keeperState, long time, TimeUnit timeUnit);

    void waitUntilConnected() throws ZkInterruptedException;

    boolean waitUntilConnected(long time, TimeUnit timeUnit);

    boolean waitUntilExists(String path, TimeUnit timeUnit, long time);

    /**
     * Installs a child watch for the given path.
     * 
     * @param path
     * @return the current children of the path or null if the zk node with the given path
     *         doesn't exist.
     */
    List<String> watchForChilds(final String path);

    void watchForData(final String path);

    Stat writeData(String path, T object);

    Stat writeData(final String path, T datat, final int expectedVersion);
    
    ZooKeeper getZooKeeper();
}
