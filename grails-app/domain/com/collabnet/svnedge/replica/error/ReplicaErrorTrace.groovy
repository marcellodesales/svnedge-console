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
 * Representation of the error trace. This is a direct representation of the
 * Java's StackTraceElement.
 * @author mdesales
 */
public class ReplicaErrorTrace {

    String className
    String methodName
    String fileName
    String lineNumber
    
    static belongsTo = [replicaError:ReplicaError]

    static constraints = {
        fileName(nullable:true)
        lineNumber(nullable:true)
    }
}
