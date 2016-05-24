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

import com.github.zkclient.exception.ZkException;
import com.github.zkclient.exception.ZkInterruptedException;
import com.github.zkclient.exception.ZkNoNodeException;
import com.github.zkclient.exception.ZkNodeExistsException;
import com.github.zkclient.exception.ZkTimeoutException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * zookeeper client wrapper
 *
 * @author adyliu (imxylz@gmail.com)
 * @since 2.0
 */
public interface IZkClient extends Closeable {

    int DEFAULT_CONNECTION_TIMEOUT = 10000;

    int DEFAULT_SESSION_TIMEOUT = 30000;

    /**
     * Close the client.
     *
     * @throws ZkInterruptedException if interrupted while closing
     */
    void close();

    /**
     * Connect to ZooKeeper.
     *
     * @param timeout max waiting time(ms) until connected
     * @param watcher default watcher
     * @throws ZkInterruptedException if the connection timed out due to thread interruption
     * @throws ZkTimeoutException     if the connection timed out
     * @throws IllegalStateException  if the connection timed out due to thread interruption
     */
    void connect(final long timeout, Watcher watcher);

    /**
     * Counts number of children for the given path.
     *
     * @param path zk path
     * @return number of children or 0 if path does not exist.
     */
    int countChildren(String path);

    /**
     * Create a node.
     *
     * @param path zk path
     * @param data node data
     * @param mode create mode {@link CreateMode}
     * @return created path
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *                                  thread
     * @throws ZkException              if any ZooKeeper exception occurred
     */
    String create(final String path, byte[] data, final CreateMode mode);

    /**
     * Create an ephemeral node with empty data
     *
     * @param path zk path
     * @throws ZkInterruptedException   if operation was interrupted, or a required reconnection
     *                                  got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *                                  thread
     * @throws ZkException              if any ZooKeeper exception occurred
     */
    void createEphemeral(final String path);

    /**
     * Create an ephemeral node.
     *
     * @param path the path for the node
     * @param data node data
     * @throws ZkInterruptedException   if operation was interrupted, or a required reconnection
     *                                  got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *                                  thread
     * @throws ZkException              if any ZooKeeper exception occurred
     */
    void createEphemeral(final String path, final byte[] data);

    /**
     * Create an ephemeral, sequential node.
     *
     * @param path the path for the node
     * @param data the data for the node
     * @return created path
     * @throws ZkInterruptedException   if operation was interrupted, or a required reconnection
     *                                  got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *                                  thread
     * @throws ZkException              if any ZooKeeper exception occurred
     */
    String createEphemeralSequential(final String path, final byte[] data);

    /**
     * Create a persistent node with empty data (null)
     *
     * @param path the path for the node
     * @throws ZkNodeExistsException    if the node exists
     * @throws ZkNoNodeException        if the parent node not exists
     * @throws ZkInterruptedException   if operation was interrupted, or a required reconnection
     *                                  got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *                                  thread
     * @throws ZkException              if any ZooKeeper exception occurred
     * @throws RuntimeException         any other exception
     * @see #createPersistent(String, boolean)
     */
    void createPersistent(String path);

    /**
     * Create a persistent node with empty data (null)
     * <p>
     * If the createParents is true, neither {@link ZkNodeExistsException} nor {@link com.github.zkclient.exception.ZkNoNodeException} were throwed.
     * </p>
     *
     * @param path          the path for the node
     * @param createParents if true all parent dirs are created as well and no
     *                      {@link ZkNodeExistsException} is thrown in case the path already exists
     * @throws ZkInterruptedException   if operation was interrupted, or a required reconnection
     *                                  got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *                                  thread
     * @throws ZkException              if any ZooKeeper exception occurred
     * @throws RuntimeException         any other exception
     */
    void createPersistent(String path, boolean createParents);

    /**
     * Create a persistent node.
     *
     * @param path the path for the node
     * @param data node data
     * @throws ZkInterruptedException   if operation was interrupted, or a required reconnection
     *                                  got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *                                  thread
     * @throws ZkException              if any ZooKeeper exception occurred
     * @throws RuntimeException         any other exception
     */
    void createPersistent(String path, byte[] data);

    /**
     * Create a persistent, sequental node.
     *
     * @param path the path for the node
     * @param data node data
     * @return create node's path
     * @throws ZkInterruptedException   if operation was interrupted, or a required reconnection
     *                                  got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event
     *                                  thread
     * @throws ZkException              if any ZooKeeper exception occurred
     * @throws RuntimeException         any other exception
     */
    String createPersistentSequential(String path, byte[] data);

    /**
     * delete a node
     *
     * @param path the path for the node
     * @return true if deleted; otherwise false
     */
    boolean delete(final String path);

    /**
     * delete a node with all children
     *
     * @param path the path for the node
     * @return true if all deleted; otherwise false
     */
    boolean deleteRecursive(String path);

    /**
     * check the node exists
     *
     * @param path the path for the node
     * @return true if the node exists
     */
    boolean exists(final String path);

    /**
     * get the children for the node
     *
     * @param path the path for the node
     * @return the children node names or null (then node not exists)
     */
    List<String> getChildren(String path);

    /**
     * get the node creation time (unix milliseconds)
     *
     * @param path the path for the node
     * @return the unix milliseconds or -1 if node not exists
     */
    long getCreationTime(String path);

    /**
     * all watcher number in this connection
     *
     * @return watcher number
     */
    int numberOfListeners();

    /**
     * read the data for the node
     *
     * @param path the path for the node
     * @return the data for the node
     * @throws com.github.zkclient.exception.ZkNoNodeException if the node not exists
     * @see #readData(String, boolean)
     */
    byte[] readData(String path);

    /**
     * read the data for the node
     *
     * @param path                      the path for the node
     * @param returnNullIfPathNotExists if true no {@link com.github.zkclient.exception.ZkNoNodeException} thrown
     * @return the data for the node
     */
    byte[] readData(String path, boolean returnNullIfPathNotExists);

    /**
     * read the data and stat for the node
     *
     * @param path the path for the node
     * @param stat the stat for the node
     * @return the data for the node
     * @see #readData(String, boolean)
     */
    byte[] readData(String path, Stat stat);

    /**
     * subscribe the changing for children
     *
     * @param path     the path for the node
     * @param listener the listener
     * @return the children list or null if the node not exists
     * @see IZkChildListener
     */
    List<String> subscribeChildChanges(String path, IZkChildListener listener);

    /**
     * subscribe the data changing for the node
     *
     * @param path     the path for the node
     * @param listener the data changing listener
     * @see IZkDataListener
     */
    void subscribeDataChanges(String path, IZkDataListener listener);

    /**
     * subscribe the connection state
     *
     * @param listener the connection listener
     * @see IZkStateListener
     */
    void subscribeStateChanges(IZkStateListener listener);

    /**
     * unsubscribe all listeners for all path and connection state
     */
    void unsubscribeAll();

    /**
     * unsubscribe the child listener
     *
     * @param path          the path for the node
     * @param childListener the listener
     */
    void unsubscribeChildChanges(String path, IZkChildListener childListener);

    /**
     * unsubscribe the data changing for the node
     *
     * @param path         the path for the node
     * @param dataListener the data changing listener
     */
    void unsubscribeDataChanges(String path, IZkDataListener dataListener);

    /**
     * unsubscribe the connection state
     *
     * @param stateListener the connection listener
     */
    void unsubscribeStateChanges(IZkStateListener stateListener);

    /**
     * Updates data of an existing znode. The current content of the znode is passed to the
     * {@link DataUpdater} that is passed into this method, which returns the new content. The
     * new content is only written back to ZooKeeper if nobody has modified the given znode in
     * between. If a concurrent change has been detected the new data of the znode is passed to
     * the updater once again until the new contents can be successfully written back to
     * ZooKeeper.
     *
     * @param path    the path for the node
     * @param updater Updater that creates the new contents.
     */
    void cas(String path, DataUpdater updater);

    /**
     * wait some time for the state
     *
     * @param keeperState the state
     * @param time        some time
     * @param timeUnit    the time unit
     * @return true if the connection state is the <code>keeperState</code> before the end time
     */
    boolean waitForKeeperState(KeeperState keeperState, long time, TimeUnit timeUnit);

    /**
     * wait for the connected state.
     * <pre>
     *     waitForKeeperState(KeeperState.SyncConnected, Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
     * </pre>
     *
     * @return true if the client connects the server
     * @throws ZkInterruptedException the thread was interrupted
     * @see #waitForKeeperState(org.apache.zookeeper.Watcher.Event.KeeperState, long, java.util.concurrent.TimeUnit)
     */
    boolean waitUntilConnected() throws ZkInterruptedException;

    /**
     * wait for the connected state
     *
     * @param time     soem time
     * @param timeUnit the time unit
     * @return true if the client connects the server before the end time
     */
    boolean waitUntilConnected(long time, TimeUnit timeUnit);

    /**
     * wait some unit until the node exists
     *
     * @param path     the path for the node
     * @param timeUnit the time unit
     * @param time     some time
     * @return true if the node exists
     */
    boolean waitUntilExists(String path, TimeUnit timeUnit, long time);

    /**
     * write the data for the node
     *
     * @param path the path for the node
     * @param data the data for the node
     * @return the stat for the node
     */
    Stat writeData(String path, byte[] data);

    /**
     * write the data for the node
     *
     * @param path            the path for the node
     * @param data            the data for the node
     * @param expectedVersion the version for the node
     * @return the stat for the node
     * @see #cas(String, com.github.zkclient.IZkClient.DataUpdater)
     */
    Stat writeData(String path, byte[] data, int expectedVersion);

    /**
     * multi operation for zookeeper 3.4.x
     *
     * @param ops operations
     * @return op result
     * @see org.apache.zookeeper.ZooKeeper#multi(Iterable)
     * @see org.apache.zookeeper.Op
     * @see org.apache.zookeeper.OpResult
     */
    List<?> multi(Iterable<?> ops);

    /**
     * get the inner zookeeper client
     *
     * @return the inner zookeeper client
     */
    ZooKeeper getZooKeeper();

    /**
     * check the connecting state of zookeeper client
     * @return true if connected
     */
    boolean isConnected();

    /**
     * A CAS operation
     */
    interface DataUpdater {

        /**
         * Updates the current data of a znode.
         *
         * @param currentData The current contents.
         * @return the new data that should be written back to ZooKeeper.
         */
        public byte[] update(byte[] currentData);

    }
}
