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


import java.text.SimpleDateFormat

/**
 * The cache value is responsible for holding the default response.
 * This class is backed up with a map with the keys and values, and therefore,
 * implementing classes must directly access the values through the properties.
 * 
 * Since this class is designed for both Master CTF and CEE, the
 * response value can be retrieved by the method getResponseValue. 
 * @author mdesales
 */
public class CacheValue {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat(
            "MMM dd, yyyy HH:mm:ss") 
     
    /** Value on which the key value expires */
    protected expiresOn
    /** Value of the proxied method that was returned by the Master */
    protected responseValue

    /**
     * @return if the cached value has been expired. This value will depend if
     * the cache entry is a positive or negative entry. 
     */
    boolean hasExpired() {
         return System.currentTimeMillis() > expiresOn
    }

    String toString() {
        return "($responseValue): expires on " + getHumanReadableExpiration()
    }
    
    public String getHumanReadableExpiration() {
        return getHumanReadableExpiration(expiresOn)
    }

    static String getHumanReadableExpiration(expirationLongValue) {
        return FORMATTER.format(new Date(expirationLongValue))
    }
}
