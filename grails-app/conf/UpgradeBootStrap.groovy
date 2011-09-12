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
import com.collabnet.svnedge.domain.Server
import com.collabnet.svnedge.util.ConfigUtil

/**
 * Bootstrap script for handling any special conditions associated with upgrades
 */
class UpgradeBootStrap {

    def dataSource
    def lifecycleService
    def serverConfService
    

    def init = { servletContext ->
        release2_1_0()
    }


    def void release2_1_0() {

        log.info("Applying 2.1.0 updates if needed")
        Server s = Server.getServer()

        // If this is a new install, Server instance will be created correctly so we can exit
        if (!s) {
            return
        }

        // otherwise, initialize new fields
        if (s.useHttpV2 == null) {
            s.useHttpV2 = true
            if (serverConfService.syncReplicaConfigurationWithMaster()) {
                lifecycleService.gracefulRestartServer()
            }
        }

        if (s.dumpDir == null) {
            s.dumpDir = ConfigUtil.dumpDirPath()
            File dumpDir = new File(s.dumpDir)
            if (!dumpDir.exists()) {
                dumpDir.mkdir()
            }
        }

        s.save(flush:true)


    }
}
