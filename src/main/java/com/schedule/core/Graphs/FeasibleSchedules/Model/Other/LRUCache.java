package com.schedule.core.Graphs.FeasibleSchedules.Model.Other;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Least Recently Used Cache Data structure
 *
 * @param <K>
 *         Key
 * @param <V>
 *         Value
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    /** Cache size. */
    private Integer cacheSize;

    /**
     * Constructor
     */
    public LRUCache(final Integer cacheSize) {
        super(16, 0.75f, true);

        this.cacheSize = cacheSize;
    }

    /**
     * Removes eldest entry when size exceeds cacheSize; Automatic when we put key/values in hashmap
     *
     * @param eldest
     *         Eldest entry.
     * @return success/failure
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > cacheSize;
    }
}
