/**
 * Copyright (C) 2014  Universidade de Aveiro, DETI/IEETA, Bioinformatics Group - http://bioinformatics.ua.pt/
 *
 * This file is part of Dicoogle/dicoogle.
 *
 * Dicoogle/dicoogle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dicoogle/dicoogle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dicoogle.  If not, see <http://www.gnu.org/licenses/>.
 */
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