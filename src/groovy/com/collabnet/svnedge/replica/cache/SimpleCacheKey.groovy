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
package com.collabnet.svnedge.replica.cache
/**
 * SimpleCacheKey simply uses the username for a CacheKey.
 */
public class SimpleCacheKey extends AbstractCacheKey {

    private SimpleCacheKey(newUsername) {
        super(newUsername)
    }

    /**
     * Factory method used to create a new instance
     * @param newUsername is the username
     * @return a new instance of the Simple Cache key
     */
    static newInstance(newUsername) {
        if (!newUsername)
            throw new IllegalArgumentException("Parameter 'username' " +
                "must be provided")
        return new SimpleCacheKey(newUsername)
    }

    boolean equals(otherKey) {
        if (otherKey && otherKey instanceof SimpleCacheKey) 
            return keyValues.username == otherKey.getUsername()
        else return false
    }

    int hashCode() {
        return 31 + 32 * keyValues.username.hashCode()
    }

    int compareTo(other) {
        return this.keyValues.username.compareTo(other.keyValues.username)
    }
}
