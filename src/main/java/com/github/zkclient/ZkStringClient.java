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

import com.github.zkclient.serialize.StringSeralizer;
import com.github.zkclient.serialize.ZkSerializer;

/**
 * Zookeeper client with string encoder
 * @since 2.0
 */
public class ZkStringClient extends AbstractZkClient<String> {

    public ZkStringClient(String zkServers) {
        this(zkServers, DEFAULT_CONNECTION_TIMEOUT);
    }

    public ZkStringClient(String zkServers, int connectionTimeout) {
        this(new ZkConnection(zkServers), connectionTimeout);
    }

    public ZkStringClient(String zkServers, int sessionTimeout, int connectionTimeout) {
        this(new ZkConnection(zkServers, sessionTimeout), connectionTimeout);
    }

    public ZkStringClient(String zkServers, int sessionTimeout, int connectionTimeout, ZkSerializer<String> zkSerializer) {
        this(new ZkConnection(zkServers, sessionTimeout), connectionTimeout, zkSerializer);
    }

    public ZkStringClient(IZkConnection connection) {
        this(connection, DEFAULT_CONNECTION_TIMEOUT);
    }

    public ZkStringClient(IZkConnection connection, int connectionTimeout) {
        this(connection, connectionTimeout, new StringSeralizer());
    }

    public ZkStringClient(IZkConnection zkConnection, int connectionTimeout, ZkSerializer<String> zkSerializer) {
        super(zkConnection, connectionTimeout, zkSerializer);
    }

}
