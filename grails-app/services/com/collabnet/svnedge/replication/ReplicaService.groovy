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
package com.collabnet.svnedge.replication

import com.collabnet.svnedge.console.Server


import com.collabnet.svnedge.console.ServerMode
import com.collabnet.svnedge.master.RemoteMasterException
import com.collabnet.svnedge.teamforge.CtfServer
import com.collabnet.svnedge.teamforge.CtfConnectionBean
import com.collabnet.svnedge.replica.manager.ApprovalState
import static com.collabnet.svnedge.console.services.JobsAdminService.REPLICA_GROUP

/**
 * This service handles replication-related functionality
 */
class ReplicaService {

    boolean transactional = true
    public static String DEFAULT_SYNC_RATE = 5 // minutes for polling interval

    def ctfRemoteClientService
    def setupTeamForgeService
    def jobsAdminService
    def securityService
    def serverConfService
    def lifecycleService


    /**
     * Confirm the ctf connection
     */
    public void confirmCtfConnection(CtfConnectionBean ctfConn) {
        // attempt connection -- throws exception on failure
        log.debug("Verifying CTF connection")
        setupTeamForgeService.confirmConnection(ctfConn)        
    }
 
    /**
     * Attempt to register (or re-register) the Replica with the Master.
     * This should be called after the Replica is first setup, or any time
     * the Replica is updated or the Master is changed.
     * @param rc the Replica Configuration data
     * @param conversion the CtfConversionBean holding connection info
     */
    public void registerReplica(ReplicaConversionBean replicaInfo) 
            throws RemoteMasterException, ReplicaConversionException
    {

        log.debug("Attempting replica conversion...")
        String systemId = ctfRemoteClientService.registerReplica(replicaInfo)

        log.debug("Conversion successful, got ID: " + systemId)

        // with success, make modification to this instance
        def server = Server.getServer()
        server.mode = ServerMode.REPLICA

        ReplicaConfiguration rc = ReplicaConfiguration.getCurrentConfig()
        if (!rc) {
            rc = new ReplicaConfiguration()
        }
        rc.svnMasterUrl = replicaInfo.svnMasterURL
        rc.description = replicaInfo.description
        rc.message = replicaInfo.message
        rc.approvalState = ApprovalState.PENDING
        rc.systemId = systemId
        rc.svnSyncRate = DEFAULT_SYNC_RATE

        def ctfServer = CtfServer.getServer() 
        if (!ctfServer) {
            ctfServer = new CtfServer()
        }
        ctfServer.baseUrl = replicaInfo.ctfURL
        ctfServer.internalApiKey = replicaInfo.serverKey
        ctfServer.ctfUsername = replicaInfo.ctfUsername
        ctfServer.ctfPassword = securityService.encrypt(replicaInfo.ctfPassword)
        
        if (!rc.validate() || !ctfServer.validate() || !server.validate()) {
            
            log.error("could not save necessary domain objects")
            [rc, ctfServer, server].each { domainObj ->
                domainObj.errors.each { log.error(it) }
            }
            throw new ReplicaConversionException("Could not convert to replica")
        }

        rc.save(flush:true)
        ctfServer.save(flush:true)
        server.save(flush:true)
        
        log.info("Rewriting server config and restarting")
        // setupTeamForgeService.installIntegrationServer(replicaInfo)
        setupTeamForgeService.unpackIntegrationScripts(replicaInfo.userLocale)

        serverConfService.backupAndOverwriteHttpdConf()
        serverConfService.writeConfigFiles()
        setupTeamForgeService.restartServer()

        log.info("Resuming replica jobs")
        jobsAdminService.resumeGroup(REPLICA_GROUP)
    }

    /**
     * update the TeamForge credentials on file
     * @param ctfConn
     */
    public void updateCtfConnection(CtfConnectionBean ctfConn) {

        // confirm that new credentials are legit
        confirmCtfConnection(ctfConn)

        // persist
        log.warn("Updating the CTF credentials used for SVN Replication")
        def ctfServer = CtfServer.getServer() 

        ctfServer.ctfUsername = ctfConn.ctfUsername
        ctfServer.ctfPassword = securityService.encrypt(ctfConn.ctfPassword)

    }

    
    /**
     * Obtain a list of integration servers from the Ctf connection represented
     * in the conversion bean
     * @param ctfConn
     * @return List of servers which can be mirrored 
     */
    public List<String> getIntegrationServers (CtfConnectionBean ctfConn) {
        
        return ["https://system1/svn", "https://system2/svn", "https://system3/svn"]
//        return ctfRemoteClientService.getIntegrationServers(ctfConn.ctfURL, ctfConn.userSessionId,
//            ctfConn.userLocale)
    }
}
