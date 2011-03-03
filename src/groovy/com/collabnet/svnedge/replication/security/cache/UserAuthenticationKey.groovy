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
package com.collabnet.svnedge.replication.security.cache

import java.security.MessageDigest 
import sun.misc.BASE64Encoder 

/**
 * The UserAuthenticationKey is used to identify an authentication cache key
 * for both the Master CEE and CTF authentication proxied methods. It is
 * comprised of the username and password of the user on the Master host.
 * 
 * @author mdesales
 */
final class UserAuthenticationKey extends AbstractCacheKey {

     private UserAuthenticationKey(newUsername, newPassword) {
         super(newUsername)
         keyValues.password = encrypt(newPassword)
     }

     /**
      * Encrypts a given text using the SHA-1 algorithm. It uses the UTF-8 bytes
      * of the given text. 
      * @param plainText is the plain text to be encrypted. 
      * @return the SHA-1 encryption of the given plainText 
      */
     public static String encrypt(String plainText) {
         MessageDigest md = null;
         md = MessageDigest.getInstance("SHA");
         md.update(plainText.getBytes("UTF-8"));
         def raw = md.digest();
         return (new BASE64Encoder()).encode(raw);
     }

     /**
      * Creates a new instance of the User authentication for any given
      * Master host (CEE or CTF).
      * @param username is the username of a user on a Master host.
      * @param is the password of a user on a Master host.
      * @return a new instance of this key class based on the given username
      * and password.
      */
     static newInstance(username, password) {
         return new UserAuthenticationKey(username, password)
     }

     /**
      * @return the password related to the authentication key.
      */
     String getPassword() {
         return keyValues.password
     }

     boolean equals(otherKey) {
         if (otherKey && otherKey instanceof UserAuthenticationKey) 
             return keyValues.username == otherKey.getUsername() && 
                    keyValues.password == otherKey.getPassword()
         else return false
     }

     int hashCode() {
         return 31 + 32 * keyValues.username.hashCode() + 
           33 * keyValues.password.hashCode()
     }

     /**
      * @param username is the username of a user on a Master CTF host.
      * @param password is the username of a user on on a Master host.
      * @return the String representation of the given username, repoPath and
      * access type
      */
     static String format(username, password) {
         return username + "|" + password
     }

     public String toString() {
         return format(keyValues.username, keyValues.password)
     }

     int compareTo(other) {
         return this.toString().compareTo(other.toString());
     }
}
