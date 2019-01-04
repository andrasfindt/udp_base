package com.company.cache;

import org.apache.commons.collections4.map.PassiveExpiringMap;

import java.util.Map;


public abstract class Cache<T> {
    private final Map<Key, T> expiringMap = new PassiveExpiringMap<>(getTimeToLiveInMilliseconds());

    protected abstract long getTimeToLiveInMilliseconds();

    protected interface Key {
        public String toString();

        public int hashCode();

        public boolean equals(Object key);
    }
}
