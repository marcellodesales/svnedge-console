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

package com.collabnet.svnedge.integration

import groovyx.net.http.RESTClient
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory
import com.collabnet.svnedge.console.AbstractSvnEdgeService
import com.collabnet.svnedge.util.*

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

/**
 * This class provides remote access to the Cloud  Services api
 */
class CloudServicesRemoteClientService extends AbstractSvnEdgeService {

    def retrieveUsers() {

        def http = createRestClient()
        http.request(GET, JSON) {
            uri.path = 'users'

            // response handler for a success response code:
            response.success = { resp, json ->
                println resp.statusLine

                // parse the JSON response object:
                json.responseData.results.each {
                    println "  ${it.titleNoFormatting} : ${it.visibleUrl}"
                }
            }
        }
    }

    /**
     * creates a RESTClient for the codesion API
     * @return a RESTClient
     */
    private RESTClient createRestClient() {
        def restClient = new RESTClient(ConfigUtil.configuration.svnedge.cloudServices.baseUrl)
        def keyStore = SSLUtil.applicationKeyStore
        restClient.client.connectionManager.schemeRegistry.register(
                new Scheme("https", new SSLSocketFactory(keyStore), 443))
        return restClient
    }
}
