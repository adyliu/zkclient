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

import java.util.List;

/**
 * An {@link IZkChildListener} can be registered at a {@link ZkClient} for listening on zk child changes for a given
 * path.
 * <p/>
 * Node: Also this listener re-subscribes it watch for the path on each zk event (zk watches are one-timers) is is not
 * guaranteed that events on the path are missing (see http://zookeeper.apache.org/doc/current/zookeeperProgrammers.html#ch_zkWatches). An
 * implementation of this class should take that into account.
 */
public interface IZkChildListener {

    /**
     * Called when the children of the given path changed.
     * <p>
     * NOTICE: if subscribing a node(not exists), the event will be triggered while the node(parent) were created.
     * </p>
     *
     * @param parentPath      The parent path
     * @param currentChildren The children or null if the root node (parent path) was deleted.
     * @throws Exception
     */
    public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception;
}
