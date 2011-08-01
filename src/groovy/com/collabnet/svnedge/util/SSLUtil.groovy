/*
 * CollabNet Subversion Edge
 * Copyright (C) 2011, CollabNet Inc. All rights reserved.
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

import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory

/**
 * Utility class for SSL-related functions
 */
class SSLUtil {
    
    private static TrustManager[] trustAllCerts 
    private static SSLContext sslContext 
    private static SSLSocketFactory sslSocketFactory 

    /**
     * static constructor
     */
    static {
        
        // Create the all-trusting manager
        TrustManager[] trustAllCerts = [ new X509TrustManager() {
                @Override
                public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
                }
                @Override
                public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
                }
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }].toArray(new TrustManager[1])
        
            
        // Install the all-trusting trust manager
        sslContext = SSLContext.getInstance( "SSL" );
        sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
        // Create an ssl socket factory with all-trusting manager
        sslSocketFactory = sslContext.getSocketFactory();
    }

    /**
     * Creates a client Socket that will trust all SSL certificates
     * @param host
     * @param port
     * @return an SSL-capable socket that ignores certificate trust problems
     */
    public static Socket createTrustingSocket(String host, int port) {
        return sslSocketFactory.createSocket (host, port)
    } 
    

    
    
}
