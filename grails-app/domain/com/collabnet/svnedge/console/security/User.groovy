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
package com.collabnet.svnedge.console.security

import com.collabnet.svnedge.console.security.Role

/**
 * User domain class.  Initial revision is mostly grails template code.
 */
class User {
    static transients = ['pass']
    static hasMany = [authorities: Role]
    static belongsTo = Role

    /** Username */
    String username
    /** User Real Name*/
    String realUserName
    /** MD5 Password */
    String passwd
    /** enabled */
    boolean enabled = true

    String email

    /** description */
    String description = ''

    /** plain password to create a MD5 password */
    String pass = '[secret]'

    static constraints = {
        username(blank: false, unique: true, minSize: 1, maxSize: 31, 
            matches: "[A-Za-z][._A-Za-z0-9]*", validator: { val ->
                if (val.indexOf(" ") >= 0) {
                    return "spaces.not.allowed"
                }
        })
        email(email:true, blank:false, validator: { val ->
                if (val.indexOf(" ") >= 0) {
                    return "spaces.not.allowed"
                }
        })
        realUserName(blank: false)
        passwd(blank: false, minSize: 5, maxSize: 255,
            matches: "[^\"]*")
        enabled()
    }
}
