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


import java.util.concurrent.ConcurrentHashMap

/**
 * The ProxyCache deals with any type of cache, given the pair CacheKey and
 * CacheValue. The CacheKey is related to the parameters used to remote method
 * calls to XML-RPC/SOAP servers defined at a given Master host. The rules for
 * those are defied as follows:
 * <li>
 * Positive Entries: CacheKeys that are valid entries to remote method 
 * calls. These calls produced a VALID result from the proxied remote method.
 *   <li>CacheKey contains all properties</li>
 *   <li>CacheValue contains all properties NOT NULL. The cache expiration
 * must be defined to be in the minutes range.</li>
 * </li> 
 * <li>
 * Negative Entries: CacheKeys that are invalid entries to remote method 
 * calls. These calls produced an INVALID result from the proxied remote method
 * such as a SOAP FAULT.
 *   <li>CacheKey contains all properties</li>
 *   <li>CacheValue contains all properties. The cache expiration must be 
 *   defined to be in the seconds range. It can be NULL if the method call
 *   returned a fault.</li>
 * </li>
 * 
 * @author mdesales
 *
 */
public class ProxyCache extends ConcurrentHashMap<CacheKey, CacheValue> {

    /**
     * Creates a new authentication key for any given Master host. The given
     * parameter "newPassword" will be encrypted using the Message Digest 
     * algorith SHA-1. 
     * @param newUsername is the username of a user on a Master host
     * @param newPassword is the password of a user on a Master host
     * @return a new key based on the given parameters
     */
    static CacheKey newAuthKey(String newUsername, String newPassword) {
        return UserAuthenticationKey.newInstance(newUsername, newPassword)
    }

    /**
     * Creates a new cache key, based only on the username.
     * @param username
     * @return a new simple key.
     */
    static CacheKey newSimpleKey(newUsername) {
        return SimpleCacheKey.newInstance(newUsername)
    }

    /**
     * Creates a new authorization key for a CTF Master host. 
     * @param newUsername is the username of a user on a CTF Master host
     * @param newRepoPath is the path to the SVN repo
     * @param newAccessType is an optional parameter for the access type. It
     * will be represented by an empty string "" in case it is no provided or
     * provided as null.
     * @return a new key based on the given parameters
     */
    static CacheKey newCTFOauthKey(newUsername, newRepoPath, 
            newAccessType) {
        return CTFUserAuthorizationKey.newInstance(newUsername, newRepoPath, 
                newAccessType)
    }

    /**
     * Creates a new cache value based on the response value and the given
     * expiration value.
     * @param newExpiresOn is the number of milliseconds for the time that the
     * cache expires.
     * @param newResponseValue is the value returned/calculated from the Master
     * CEE that was proxied for a given user authentication/authorization
     * operation.
     * @return a new value based on the given parameters
     */
    static CacheValue newValue(newExpiresOn, newResponseValue) {
        return new CacheValue(expiresOn:newExpiresOn, 
                responseValue:newResponseValue)
    }

    /**
     * Creates a new Cache Value that will expire in terms of the number of
     * minutes given.
     * @param minutes is the number of minutes for the cache value. It must be
     * a positive number.
     * @param newResponseValue is the response value from the Master host (CEE
     * or CTF). When this value is null, it represents a cached value defined
     * for a falt.
     * @return the cached value related ot the given values.
     */
    static CacheValue newValueExpiresInMinutes(minutes, newResponseValue) {
        if (minutes == null || minutes <= 0) {
            throw new IllegalArgumentException("The minutes value must be a " +
                    "positive number")
        }
        def newExpiresOn = (1000 * 60 * minutes) + System.currentTimeMillis()
        return ProxyCache.newValue(newExpiresOn, newResponseValue)
    }

    /**
     * Creates a new Cache Value that will expire in terms of the number of
     * seconds given.
     * @param secs is the number of seconds for the cache value. It must be
     * a positive number.
     * @param newResponseValue is the response value from the Master host (CEE
     * or CTF). When this value is null, it represents a cached value defined
     * for a falt.
     * @return the cached value related ot the given values.
     */
    static CacheValue newValueExpiresInSeconds(secs, newResponseValue) {
        if (secs == null || secs <= 0) {
            throw new IllegalArgumentException("The minutes value must be a " +
                    "positive number")
        }
        def newExpiresOn = (1000 * secs) + System.currentTimeMillis()
        return ProxyCache.newValue(newExpiresOn, newResponseValue)
    }

    /**
     * Verifies if the instance of the ProxyCache contains a given username.
     * @param username is the username of a user on any Master host.
     * @return if the user is present on any type of cache (authentication
     * or authorization) by verifying the key.
     */
    boolean containsUsername(username) {
        for (cacheKey in keySet()) {
            if (cacheKey.containsUsername(username)) {
                return true
            }
        }
        return false
    }

    CacheKey getKeyByUsername(username) {
        for (cacheKey in keySet()) {
            if (cacheKey.containsUsername(username)) {
                return cacheKey
            }
        }
        return null
    }

    CacheValue getValueByUsername(username) {
        def key = getKeyByUsername(username)
        return get(key)
    }

    /**
     * @param cacheKey is the Cache Key instance. If this parameter does
     * not exist in the cache, it will return false.
     * @return if a given Value has been expired from a given cacheKey
     */
    boolean hasKeyValueExpired(CacheKey cacheKey) {
        if (containsKey(cacheKey)) {
            return get(cacheKey).hasExpired()
        } else {
            return true
        }
    }
}
