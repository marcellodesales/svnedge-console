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
 * The CTF User Authorization key is related to the parameters used to the
 * proxied remote method invocation ScmListener.getRolePaths to the 
 * Master CTF. It includes the values of the username, systemId, repoPath, 
 * accessType
 * @author mdesales
 */
public class CTFUserAuthorizationKey extends AbstractCacheKey {

    private CTFUserAuthorizationKey(newUsername, newRepoPath, newAccessType) {
        super(newUsername)
        keyValues.repoPath = newRepoPath
        keyValues.accessType = newAccessType
    }

    /**
     * Factory method used to create a new instance
     * @param newUsername is the username
     * @param newRepoPath is the repository path
     * @param newAccessType is the optional access ty.
     * @return a new instance of the CTF Authorization key
     */
    static newInstance(newUsername, newRepoPath, newAccessType) {
        if (!newUsername || !newRepoPath) {
            throw new IllegalArgumentException("All parameters 'username', " +
               "'systemId', repo path must be provided accessType is optional")
        }
        newAccessType = newAccessType != null ? newAccessType : ""
        return new CTFUserAuthorizationKey(newUsername, newRepoPath, 
                newAccessType)
    }

    /**
     * @return the repository path property of the key
     */
    String getRepoPath() {
        return keyValues.repoPath
    }

    /**
     * @return the system id property of the key
     */
    String getAccessType() {
        return keyValues.accessType
    }

    boolean equals(otherKey) {
        if (otherKey && otherKey instanceof CTFUserAuthorizationKey) 
            return keyValues.repoPath == otherKey.getRepoPath() &&
                   keyValues.accessType == otherKey.getAccessType() &&
                   keyValues.username == otherKey.getUsername()
        else return false
    }

    int hashCode() {
        return 31 + 32 * keyValues.username.hashCode() + 
                33 * keyValues.repoPath.hashCode() +
                34 * keyValues.accessType.hashCode()
    }

    /**
     * @param username is the username of a user on a Master CTF host.
     * @param repoPath is the path of the SVN repository on a Master CTF host.
     * @param accessType is the optional value of the access type for the
     * given repoPaty.
     * @return the String representation of the given username, repoPath and
     * access type
     */
    static String format(username, repoPath, accessType) {
        return username + "|" + repoPath + 
                (accessType != null && !accessType.equals("") ? 
                        "|" + accessType : "")
    }

    public String toString() {
        return format(keyValues.username, keyValues.repoPath, 
                keyValues.accessType)
    }

    int compareTo(other) {
        return this.toString().compareTo(other.toString());
    }
}
