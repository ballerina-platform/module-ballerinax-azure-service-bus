/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinax.asb;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.ballerinax.asb.util.ASBConstants.RECEIVEANDDELETE;

/**
 * The IMessageReceiver factory implementation for IMessageReceiver pool.
 */
public class IMessageReceiverFactory extends BaseKeyedPooledObjectFactory<String, IMessageReceiver> {

    private Map<String, String[]> messageReceiverProperties = new LinkedHashMap<>();

    @Override
    public IMessageReceiver create(String entityPath) throws Exception {
        String[] values = messageReceiverProperties.get(entityPath);
        String connectionString = values[0];
        String receiveMode = values[1];
        if (Objects.equals(receiveMode, RECEIVEANDDELETE)) {
            return ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                    new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.RECEIVEANDDELETE);
        } else {
            return ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                    new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);
        }
    }

    @Override
    public PooledObject<IMessageReceiver> wrap(IMessageReceiver connection) {
        return new DefaultPooledObject<>(connection);
    }

    @Override
    public void destroyObject(String key, PooledObject<IMessageReceiver> pooledObject) throws Exception {
        pooledObject.getObject().close();
    }

    @Override
    public boolean validateObject(String key, PooledObject<IMessageReceiver> p) {
        return super.validateObject(key, p);
    }

    public void addMessageReceiverProperties(String entityPath, String connectionString, String receiveMode) {
        String[] propertiesArray = {connectionString, receiveMode};
        this.messageReceiverProperties.put(entityPath, propertiesArray);
    }
}
