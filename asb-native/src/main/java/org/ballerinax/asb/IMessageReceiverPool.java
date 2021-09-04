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
 * KINDither express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinax.asb;

import com.microsoft.azure.servicebus.IMessageReceiver;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.ballerinax.asb.util.ASBUtils;

import java.util.NoSuchElementException;

/**
 * Singleton Pool implementation for the IMessageReceiver associating with the IMessageReceiver factory name
 */
public class IMessageReceiverPool extends GenericKeyedObjectPool<String, IMessageReceiver> {

    private static IMessageReceiverPool instance;

    private IMessageReceiverPool(IMessageReceiverFactory factory, int poolSize) {
        super(factory);
        this.setTestOnBorrow(true);
        this.setMaxTotal(poolSize);
    }

    public static IMessageReceiverPool getInstance(IMessageReceiverFactory factory) {
        if (instance == null) {
            instance = new IMessageReceiverPool(factory, 20);
        }
        return instance;
    }

    /**
     * Obtains a connection from the pool for the specified key
     *
     * @param factoryName pool key
     * @return a {@link IMessageReceiver} object
     */
    @Override
    public IMessageReceiver borrowObject(String factoryName) throws ASBException {
        try {
            return super.borrowObject(factoryName);
        } catch (NoSuchElementException nse) {
            /* The exception was caused by an exhausted pool **/
            if (null == nse.getCause()) {
                throw new ASBException("Error occurred while getting a connection of " + factoryName +
                        " since the pool is exhausted", nse);
            }
            /* Otherwise, the exception was caused by the implemented activateObject() or validateObject() **/
            throw new ASBException("Error occurred while borrowing a connection " + factoryName, nse);
        } catch (Exception e) {
            ASBUtils.returnErrorValue("Error occurred while borrowing a connection " + factoryName);
        }
        return null;
    }

    /**
     * Returns a connection to a keyed pool.
     *
     * @param factoryName pool key
     * @param receiver    {@link IMessageReceiver} instance to return to the keyed pool
     */
    @Override
    public void returnObject(String factoryName, IMessageReceiver receiver) {
        super.returnObject(factoryName, receiver);
    }
}

