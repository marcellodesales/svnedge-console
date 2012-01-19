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
package com.collabnet.svnedge.event


import org.springframework.context.ApplicationEvent

import com.collabnet.svnedge.domain.Repository

/**
 * Spring event meant to be published from repository operations
 */
class RepositoryEvent extends ApplicationEvent {
    public static final boolean SUCCESS = true
    public static final boolean FAILED = false

    Repository repo
    boolean isSuccess
    Exception exception
    
    def RepositoryEvent(source, Repository repo, boolean isSuccess, 
            Exception e = null) {
        super(source)
        this.repo = repo
        this.isSuccess = isSuccess
        this.exception = e
    }
}
