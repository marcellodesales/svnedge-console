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
package com.collabnet.svnedge.domain.integration

/**
 * This class stores the Cloud Services configuration.
 */
public class CloudServicesConfiguration {

    String username
    String password
    String domain

    /**
     * factory to obtain singleton row
     * @return pseudo singleton instance or null
     */
    static CloudServicesConfiguration getCurrentConfig() {
        def cloudSvcRows = CloudServicesConfiguration.list()
        if (cloudSvcRows) {
            return cloudSvcRows.last()
        }
        else {
            return null
        }
    }

}

