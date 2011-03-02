/*
 * CollabNet Subversion Edge
 * Copyright (C) 2010, CollabNet Inc. All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.collabnet.svnedge.replication.auth.cache

/**
 * The abstract cache key is responsible for holding the default key values.
 * This class is backed up with a map with the keys and values, and therefore,
 * implementing classes must directly access the values through the properties.
 * 
 * Since this class was designed to be shared between the Master CEE and CTF,
 * conveninent methods to retrieve regular username is implemented here instead
 * of the concrete implementing classes.
 * @author mdesales
 */
public abstract class AbstractCacheKey implements CacheKey {

    /** Holds the properties of any type of key as key-value pair */
    protected keyValues = [:]

    def AbstractCacheKey(newUsername) {
        keyValues.username = newUsername
    }

    /**
     * @return The username related to the cached user key. Since it 
     * identifies the user in every cache, it's the preferred way to verify if
     * an entry cache contains a user.
     */
    @Override
    String getUsername() {
         return keyValues.username
    }

    /**
     * @return true iff the key contains the username.
     */
    @Override
    boolean containsUsername(searchUsername) {
        return keyValues.username == searchUsername
    }

    /**
     * @param givenProperty is a map containing the exact same number of 
     * properties as the key being used. 
     * @return true iff the key contains all the keys-value pairs in the 
     * given properties.
     */
    @Override
    boolean hasProperties(Map<String, Object> givenProperty) {
        if (keyValues.size() == givenProperty.size()) {
            return keyValues.every{ entry -> 
                 keyValues[entry.key] == givenProperty[entry.key]
            }
        }
        return false
    }
}
