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
package com.collabnet.svnedge.replica.error

/**
 * The ReplicaError class is used to store information about errors that
 * are logged by the replica. A given replica error has a list of stack
 * traces.
 */
class ReplicaError {

    static hasMany = [stackTraces: ReplicaErrorTrace]

    long timestamp
    int level
    String message
    String className
    String fileName
    String lineNumber
    String methodName
    String[] exceptionRep

    static constraints = {
        fileName(nullable:true)
        lineNumber(nullable:true)
        exceptionRep(nullable:true)
    }

    static mapping = {
        sort timestamp:"desc"
    }

    public String toString() {
        super.toString() + " " + message + " " + exceptionRep
    }
}

