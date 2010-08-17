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
package com.collabnet.svnedge.console.ui

import org.codehaus.groovy.grails.plugins.springsecurity.Secured

/**
 * OpenCollabNet view controller
 */
@Secured(['ROLE_USER'])
class OcnController {

    def index = {
        try {
            def ocnContent = 'http://tab.open.collab.net/nonav/csvn.html'.
                    toURL().text
            [ocnContent: ocnContent]

        } catch (Exception e) {
            //No connection to the host... Probably because the user is behind
            //a proxy or there's no Internet connectivity. Use the iframe on
            //the browser instead (might be configured with proxy, if that's
            //the case)
            render(view:"index-proxy")
        }
    }
}