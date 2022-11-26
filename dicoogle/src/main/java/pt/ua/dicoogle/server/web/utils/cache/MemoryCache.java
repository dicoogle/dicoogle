package pt.ua.dicoogle.server.web.utils.cache;

import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 * General purpose memory cache to be used by plugins
 * @author Rui Jesus <r.jesus@ua.pt>
 * @param <T>
 */
public abstract class MemoryCache<T> {

    protected LoadingCache<String, T> memoryCache;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected int hoursToKeep = 12;
    protected int maximumSize = 1000;

    protected MemoryCache() {
    }

    protected MemoryCache(int hoursToKeep, int maximumSize) {
        this.hoursToKeep = hoursToKeep;
        this.maximumSize = maximumSize;
    }

    public T get(String key){
        try {
            return memoryCache.get(key);
        } catch (ExecutionException e) {
            logger.error("Error retrieving key {} from cache", key, e);
            return null;
        }
    }

    public int getHoursToKeep() {
        return hoursToKeep;
    }

    public int getMaximumSize() {
        return maximumSize;
    }
}