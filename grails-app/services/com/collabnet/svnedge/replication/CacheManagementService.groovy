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
package com.collabnet.svnedge.replication

import org.springframework.remoting.RemoteAccessException

import com.collabnet.svnedge.console.security.User

import com.collabnet.svnedge.replication.security.cache.ProxyCache

/**
 * The cache management manages the cache structures for the user 
 * authentication and the file system artifacts.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 */
public class CacheManagementService {

    boolean transactional = true

    def ctfRemoteClientService
    /** 
     * The user role
     */
    public static final String ROLE_USER = "ROLE_USER"
    /**
     * The admin role
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN"
    /**
     * The View permission
     */
    public static final String PERM_USER = "view"
    /**
     * The admin permission 
     */
    public static final String PERM_ADMIN = "admin"
    /**
     * The authentication cache.
     */
    ProxyCache authCache = [:] 
    /**
     * The main users Cache instance as a TreeMap
     */
    ProxyCache oauthCache = [:]
    /**
     * The user details cache
     */
    ProxyCache userInfoCache = [:]
    /** 
     * The user role cache 
     */
    ProxyCache roleCache = [:]
    /**
     * Negative expiration timeout rate for the cache to go stale.
     */
    def negativeExpirationRate
    /**
     * Negative expiration timeout rate for the cache to go stale.
     */
    def positiveExpirationRate

    def bootStrap = { negativeRate, positiveRate ->
        negativeExpirationRate = negativeRate
        positiveExpirationRate = positiveRate
    }

    /**
     * Authenticate a given user without specifying the type of Master.
     * @param params is the parameters used to authenticate
     * @return the expected value from a CTF authentication call.
     */
    def authenticateUser(params) {
        return authenticateUser(params.username, params.password)
    }

    /**
     * Authenticate a given user without specifying the type of Master.
     * @param username is the username for the user
     * @param password is the password for the user.
     * @return the expected value from a CTF authentication call.
     */
    def authenticateUser(username, password) {
        def cacheKey = ProxyCache.newAuthKey(username, password)
        def cacheValue = getValueFromCache(authCache, cacheKey, username)
        if (!cacheValue) {
            def gUser
            try {
                gUser = ctfRemoteClientService.authenticateUser(username, 
                    password)

            } catch (Exception e) {
                log.error("SOAP Fault from Master trying to " + 
                    "authenticate user: " + username, e) 
                throw new RemoteAccessException(e.getMessage())
            }
            try {
                cacheValue = buildCacheValue(gUser ? Boolean.TRUE : 
                    Boolean.FALSE)

            } catch (Exception e) {
                cacheValue = buildCacheValue(e)
            }
            //Update the cache with the new cache entry
            authCache[cacheKey] = cacheValue
            log.debug("$cacheKey cached as $cacheValue")

            def roles = new String[0]
            if (gUser) {
                buildUserInfoCache(username, gUser.domainClass)
                roles = gUser.authorities.collect({ it.authority }).toArray(
                    roles)
            }
            buildRoleCache(username, roles)
        }
        return cacheValue.responseValue
    }

    /**
     * Authorize a given user without specifying the type of Master.
     * @param params is the parameters used to authorize the user
     * @return the expected value from a CTF authorization call.
     */
    def authorizeUser(params) {
        return getPathRoles(params.username, params.repoPath, params.accessType)
    }

    /**
     * Get roles for a user, including domain roles.
     * @param username the user to get roles for.
     * @return a String[] of the user's roles.
     */
    def getUserRoles(username) {
        def cacheKey = ProxyCache.newSimpleKey(username)
        def cacheValue = getValueFromCache(roleCache, cacheKey, username)
        if (!cacheValue) {
            log.warn("getUserRoles was called for a user that was not " +
                "authenticated: " + username)
            cacheValue = buildRoleCache(username, new String[0])
        }
        cacheValue.responseValue
    }

    /**
     * Builds the role cache for a given user.
     * @param username is the username to cache.
     * @param roles is the roles to be cached.
     * @return the cached value.
     */
    private def buildRoleCache(username, roles) {
        def cacheKey = ProxyCache.newSimpleKey(username)
        def cacheValue = buildCacheValue(roles)
        //Update the cache with the new cache entry
        log.debug("Cache size prior to insert=" + getRoleCacheSize())
        roleCache[cacheKey] = cacheValue
        log.debug("Cache size after insert=" + getRoleCacheSize())
        log.debug("$cacheKey cached as $cacheValue")
        return cacheValue
    }

    /**
     * Closure for getting a value from a cache.
     * @return null if the value is expired or not present.
     */
    def getValueFromCache = { cache, key, username ->
        if (!cache.hasKeyValueExpired(key)) {
            log.debug("$key on local cache; not expired")
            return cache[key]
        } else {
            log.debug("$key NOT on local cache or has expired: " 
                      + "verifying on master host")
            return null
        }
    }

    /**
     * Calculates the expiration for the authentication cache entry
     * given the type of the responseValue.
     * @param responseValue is the response from the Master host.
     * @return the number of minutes or seconds depending on the
     * response type
     */
    private buildCacheValue(responseValue) {
        //Values derived from (IntegrationConstants.ACCESS_OK(0), 
        //ACCESS_UNAUTHORIZED(1), ACCESS_FORBIDDEN(2)
        log.debug("Building cache value for response: $responseValue")
        if (responseValue instanceof Exception) {
            log.debug("Creating a short-term cache value for exception")
            return ProxyCache.newValueExpiresInSeconds(
                negativeExpirationRate, 1)
        }
        if (responseValue) {
            log.debug("Creating a long-term cache value for response")
            return ProxyCache.newValueExpiresInMinutes(
                positiveExpirationRate, responseValue)
        } else {
            log.debug("Creating a short-term cache value for response")
            return ProxyCache.newValueExpiresInSeconds(
                    negativeExpirationRate, responseValue)
        }
    }

    /**
     * @param username for the user.
     * @return information about the user
     */
    def getUserInfo(username) {
        def cacheKey = ProxyCache.newSimpleKey(username)
        def cacheValue = getValueFromCache(userInfoCache, cacheKey, username)

        if (!cacheValue) {
            log.warn("getUserInfo was called for a user that was not " +
                "authenticated: " + username)
            cacheValue = buildUserInfoCache(username,
                new User(username: username, realUserName: "Unauthenticated",
                    email: username + "@example.com"))
        }

        return cacheValue.responseValue 
    }

    /**
     * Builds the user Info in cache.
     * @param username is the username
     * @param user
     * @return
     */
    private def buildUserInfoCache(username, user) {
        def cacheKey = ProxyCache.newSimpleKey(username)
        def cacheValue
        try {
            cacheValue = buildCacheValue(user)
        } catch (Exception e) {
            cacheValue = buildCacheValue(e)
        }
        //Update the cache with the new cache entry
        userInfoCache[cacheKey] = cacheValue
        log.debug("$cacheKey cached as $cacheValue")
        cacheValue
    }

    /**
     * Authorize the user on CTF by verifying the local cache. If the user
     * hasn's been cached yet, the verification is done on the remote Master
     * CTF. 
     * 
     * @param username is the username on a Master CTF
     * @param repoPath is the name of the repository on a Master CTF
     * @param accessType is the optional access type for the repository
     * @return String the list authorization pattern 
     */
    String getPathRoles(username, repoPath, accessType) {
        def cacheKey = ProxyCache.newCTFOauthKey(username, repoPath, accessType)
        def cacheValue = getValueFromCache(oauthCache, cacheKey, username)

        if (!cacheValue) {
            try {
                def masterOauthResponseValue = authorizeOnMasterCTF(username,
                    repoPath, accessType)
                cacheValue = buildCacheValue(masterOauthResponseValue)

            } catch (Exception e) {
                cacheValue = buildCacheValue(e)
            }
            //Update the cache with the new cache entry
            oauthCache[cacheKey] = cacheValue
            log.debug("$cacheKey cached as $cacheValue")
        }
        return cacheValue.responseValue
    }

    /**
     * @param username is the username on a Master CTF
     * @param repoPath is the name of the repository on a Master CTF
     * @param accessType is the optional access type for the repository
     * @return String[] the list of patterns 
     * @throws RemoteAccessException if the communication fails with the remote
     * Master host for any reason.
     */
    private String authorizeOnMasterCTF(username, repoPath, accessType){
        try {
            def remoteResponse = ctfRemoteClientService?.getRolePaths(username,
                repoPath, accessType)
            return remoteResponse

        } catch (Exception e) {
            log.warn("SOAP Fault from Master CTF trying to authorize user", e)
            throw new RemoteAccessException(e.getMessage())
        }
    }

    /**
     * @return the current list of users on the Cache.
     */
    synchronized getCurrentAuthCache() {
        return authCache
    }

   synchronized getCurrentOauthCache() {
        return oauthCache
    }

    synchronized getAuthenticationCacheSize() {
        return authCache.size()
    }

    synchronized getAuthorizationCacheSize() {
        return oauthCache.size()
    }

    synchronized getUserInfoCacheSize() {
        return userInfoCache.size()
    }
    
    synchronized getRoleCacheSize() {
        return roleCache.size()
    }

    /**
     * Clears the entries on both the Authentication and Authorization caches.
     */
    synchronized flushAllCache() {
        authCache.clear()
        oauthCache.clear()
        userInfoCache.clear()
        roleCache.clear()
    }

    /**
     * Clears the expired cache entries on all caches
     */
    synchronized flushExpiredCacheEntries() {
        authCache = authCache.findAll{ !it.value.hasExpired() }
        oauthCache = oauthCache.findAll{ !it.value.hasExpired() }
        userInfoCache = userInfoCache.findAll{ !it.value.hasExpired() }
        roleCache = roleCache.findAll{ !it.value.hasExpired() }
    }
}
