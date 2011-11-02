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



package com.collabnet.svnedge.api

import sun.misc.BASE64Encoder

/**
 * Helper for API functional tests
 */
class ApiTestHelper {

    static def encodeBase64(input) {
        BASE64Encoder encoder = new BASE64Encoder();
        String output = encoder.encode(input.toString().getBytes());
        return output
    }

    static def makeAuthorization(username, password) {
        return encodeBase64("${username}:${password}")
    }

    static def makeAdminAuthorization() {
        return makeAuthorization("admin", "admin")
    }

    static def makeUserAuthorization() {
        return makeAuthorization("user", "admin")

    }

}
