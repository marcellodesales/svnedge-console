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
package com.collabnet.svnedge.util

import javax.net.ssl.HostnameVerifier

public class SvnEdgeCertHostnameVerifier implements HostnameVerifier {
    private def log

    SvnEdgeCertHostnameVerifier(log) {
        this.log = log
    }

    @Override 
    public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
        def certDN = sslSession.peerCertificates[0].issuerX500Principal.name
        if (certDN.startsWith("CN=svnedge.collab.net")) {
            log.debug("Using default cert, so using sslSession hostname," + 
                      sslSession.peerHost + ", for verification. Comparing to " + hostname)
            return (hostname == sslSession.peerHost)
        }
        return false
    }
}
