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
 * The cache key represents a key of a given cache. It exposes methods to verify
 * the user at which it relates to, has a set of properties of the parameters
 * used to execute a given proxied method, etc.
 * 
 * @author mdesales 
 */
interface CacheKey extends Comparable {

    /**
     * @return The username related to the cached user key. Since it 
     * identifies the user in every cache, it's the preferred way to verify if
     * an entry cache contains a user.
     */
    String getUsername();
    /**
     * @return true iff the key contains the username.
     */
    boolean containsUsername(username);
    /**
     *  @return true iff the key contains all the keys-value pairs in the 
     *  given properties.
     */
    boolean hasProperties(Map<String, Object> properties);
}
