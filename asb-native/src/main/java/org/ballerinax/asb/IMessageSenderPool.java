package org.ballerinax.asb;

import com.microsoft.azure.servicebus.IMessageSender;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import java.util.NoSuchElementException;

/**
 * Singleton Pool implementation for the IMessageSender associating with the IMessageSender factory name
 */
public final class IMessageSenderPool extends GenericKeyedObjectPool<String, IMessageSender> {

    private static IMessageSenderPool instance;

    private IMessageSenderPool(IMessageSenderFactory factory, int poolSize) {
        super(factory);
        this.setTestOnBorrow(true);
        this.setMaxTotal(poolSize);
    }

    public static IMessageSenderPool getInstance(IMessageSenderFactory factory) {
        if (instance == null) {
            instance = new IMessageSenderPool(factory, 20); //TODO : Configure as a user input
        }
        return instance;
    }

    /**
     * Obtains a connection from the pool for the specified key
     *
     * @param factoryName pool key
     * @return a {@link IMessageSender} object
     */
    @Override
    public IMessageSender borrowObject(String factoryName) throws ASBException {
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
            throw new ASBException("Exception occurred while borrowing the object " + factoryName);
        }
    }

    /**
     * Returns a messageSender to a keyed pool.
     *
     * @param factoryName   pool key
     * @param messageSender {@link IMessageSender} instance to return to the keyed pool
     */
    @Override
    public void returnObject(String factoryName, IMessageSender messageSender) {
        super.returnObject(factoryName, messageSender);
    }
}

