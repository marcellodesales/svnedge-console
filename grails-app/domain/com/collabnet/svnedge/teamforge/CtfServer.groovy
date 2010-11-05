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
package com.collabnet.svnedge.teamforge

/**
 * Defines the teamforge server. 
 * There is to be only one CtfServer defined.
 */
class CtfServer {
    
    static transients = ['webAppUrl']

    String baseUrl
    String mySystemId
    String internalApiKey
    String ctfUsername
    String ctfPassword
    
    public String getWebAppUrl() {
        return baseUrl + "/sf";
    }

    public String soapUrl() {
        (baseUrl.charAt(baseUrl.length() - 1) == '/') ? 
            baseUrl + "ce-soap50/services" : 
            baseUrl + "/ce-soap50/services"
    }
    
    static constraints = {
        baseUrl(nullable: false, blank: false, unique: true)
        mySystemId(nullable: true, blank: true, unique: true)
        internalApiKey(nullable: true, blank: true, unique: true)
    }
    
    static CtfServer getServer() {
        CtfServer ctf = null
        // there should be zero or one of these records, but we
        // don't know the id, so take the last one
        for (def tmp in CtfServer.list()) {
            ctf = tmp
        }
        ctf
    }
}
