package org.ballerinax.asb.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.ballerinax.asb.util.ASBConstants.LOCK_DURATION;
/**
 * This class will hold auto-expire map of messages that are received from the service bus.
 */
public class ExpiringMessageMap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpiringMessageMap.class);
    private final Map<String, Object> map = new ConcurrentHashMap<>();
    private Thread cleanupThread;

    public void initialize() {
        if (cleanupThread == null) {
            cleanupThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    removeExpiredObjects();
                    try {
                        Thread.sleep(3000); // Adjust the cleanup interval as needed
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            cleanupThread.setDaemon(true);
            cleanupThread.start();
            LOGGER.debug("Expiring message map cleanup thread started");
        }
    }

    public void put(String key, Object value) {
        map.put(key, value);
    }

    public Object get(String key) {
        return map.get(key);
    }

    public void remove(String key) {
        map.remove(key);
    }

    private void removeExpiredObjects() {
        map.entrySet().removeIf(entry -> {
            Map<String, Object> messageData = (Map<String, Object>) entry.getValue();
            OffsetDateTime lockTime = (OffsetDateTime) messageData.get(LOCK_DURATION);
            if (lockTime.isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
                LOGGER.debug("Entry with key {} is being deleted.", entry.getKey());
                return true;
            }
            return false;
        });

    }

}
