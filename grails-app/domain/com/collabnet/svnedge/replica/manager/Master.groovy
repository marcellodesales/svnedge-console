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
package com.collabnet.svnedge.replica.manager

/**
 * Defines the representation for the Master site.
 * @author mdesales
 */
public class Master {

    /** 
     * Defines if the master host will be connected by http or https
     */
    boolean sslEnabled
    /**
     * Defines the host name of Master CEE site including the project name
     */
    String hostName
    /** 
     * Defines the main username to access the Master CEE site through SOAP 
     */
    String accessUsername
    /** 
     * Defines the main password to access the Master CEE site through SOAP 
     */
    String accessPassword
    /**
     * The password to access the trust store file for SSL connection. If the
     * value of the protocol is SSL, then this value must be provided.
     **/
    String trustStorePassword
    /**
     * Determines if the Master instance is the default one.
     */
    boolean isActive

    static constraints = {
             hostName(nullable:false, blank:false, unique:true)
             accessUsername(nullable:false, blank:false)
             accessPassword(nullable:false, blank:false)
             trustStorePassword(nullable:true, validator: { val, obj ->
                     obj.sslEnabled ? (val != null) : true
             })
    }

    /**
     * Returns the default Master
     */
    static Master getDefaultMaster() {
         return Master.get(1)
    }

    String toString() {
        def protocol = sslEnabled ? "https" : "http"
        return protocol + "://" + accessUsername + "@" + accessPassword + "." +
                   hostName + "/ws-sec-min"
    }
}
