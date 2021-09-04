package org.ballerinax.asb;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.HashMap;
import java.util.Map;

/**
 * The IMessageSender factory implementation for IMessageSender pool.
 */
public class IMessageSenderFactory extends BaseKeyedPooledObjectFactory<String, IMessageSender> {

    private Map<String, String> messageSenderConnectionString = new HashMap<>();

    @Override
    public IMessageSender create(String entityPath) throws Exception {
        String connectionString = messageSenderConnectionString.get(entityPath);
        return ClientFactory.createMessageSenderFromConnectionStringBuilder(
                new ConnectionStringBuilder(connectionString, entityPath));
    }

    @Override
    public PooledObject<IMessageSender> wrap(IMessageSender connection) {
        return new DefaultPooledObject<>(connection);
    }

    @Override
    public void destroyObject(String key, PooledObject<IMessageSender> pooledObject) throws Exception {
        pooledObject.getObject().close();
    }

    @Override
    public boolean validateObject(String key, PooledObject<IMessageSender> p) {
        return super.validateObject(key, p);
    }

    public void addMessageSenderConnectionString(String entityPath, String connectionString) {
        this.messageSenderConnectionString.put(entityPath, connectionString);
    }
}
