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
// Place your Spring DSL code here
beans = {
    ctfAuthenticationProvider(com.collabnet.svnedge.integration.security.CtfAuthenticationProvider) {
        ctfRemoteClientService = ref("ctfRemoteClientService")
    }

    csvnAuthenticationProvider(com.collabnet.svnedge.security.CsvnAuthenticationProvider) {
        daoAuthenticationProvider = ref("daoAuthenticationProvider")
    }

    // use the db-based statistics service
    statisticsService(com.collabnet.svnedge.statistics.LastCollectedStatisticsService) { bean ->
        bean.autowire = 'byName'
    }
}
