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
package com.collabnet.svnedge.console

import com.collabnet.svnedge.statistics.StatValue

/**
 * Repository domain class.
 */
class Repository {
    
    static final def RECOMMENDED_NAME_PATTERN = ~/[a-z][_a-z0-9\-]*/
    static final int NAME_MAX_LENGTH = 32

    /** Name */
    String name
    /**
     * PermissionsOk -- flag to indicate need for permissions fix-up
     */
    Boolean permissionsOk = true

    /**
     * Repo statistics are FK'd, so this is used for cascade delete 
     */
    static hasMany = [ statValues: StatValue ]

    /**
     * In the web UI we try to guide users to create CTF-compatible
     * repo names, but we don't want to eliminate the ability to discover
     * repos which are created with non-matching names, so this can't be
     * a constraint on the object
     */
    boolean validateName() {
        def b = name.length() <= NAME_MAX_LENGTH
        if (b) {
            b = name.matches(RECOMMENDED_NAME_PATTERN)
            if (!b) {
                errors.rejectValue("name", "repository.name.matches.invalid")
            }
        } else {
            errors.rejectValue("name", "repository.name.size.toobig",
                [NAME_MAX_LENGTH].toArray(), "Name exceeds maximum length.")
        }
        b
    }

    static constraints = {
        name(blank: false, unique: true)
    }
}
