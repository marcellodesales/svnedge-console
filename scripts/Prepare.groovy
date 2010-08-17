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
/**
 * Gant script that prepares the development environment for development, 
 * getting the latest version of the csvn-binaries from the BPL
 *
 * @author Marcello de Sales (mdesales@collab.net)
 *
 * @since 0.1.1
 */

target(build: 'Builds the distribution file structure') {

    Ant.condition(property:"windowsPrepare") {
        and() {
            os(family:"windows")
        }
    }
    Ant.condition(property:"linuxPrepare") {
        and() {
            os(family:"unix")
        }
    }
    Ant.condition(property:"macPrepare") {
        and() {
            os(family:"mac")
        }
    }
    Ant.condition(property:"x64") {
        or() {
            os(arch: "x86_64")
            os(arch: "amd64")
        }
    }
    
    if (Ant.project.properties."macPrepare") {
        osName = "mac";
    }
    if (Ant.project.properties."windowsPrepare") {
        osName = "windows"
    }
    if (Ant.project.properties."linuxPrepare") {
        osName = "linux"
    }
    

    Ant.echo(message: "Preparing CSVN binaries / development environment for " +
            "$osName.")

    csvnbinDir = "${basedir}/svn-server"

    logsDir = csvnbinDir + "/data/logs"
    Ant.mkdir(dir: logsDir )

    downloadCSVNbin()
}

target(downloadCSVNbin: 'Downloads the csvn binaries') {
    Ant.echo(message: "Downloading the CSVN binaries")

    //Generating the truststore files for downloading
    //echo | openssl s_client -connect mgr.cubit.sp.collab.net:443 | openssl 
    //  x509 -inform PEM -outform DER -trustout -out outfile.crt
    //keytool -import -storepass together -file outfile.crt -keystore 
    //   trust.keystore -alias mgrcubitsp
    //keytool -keystore trust.keystore -list >> enter password "together"
    def trustStore = "${basedir}/scripts/" +
            "cubit.keystore"
    Ant.echo(message: "Truststore File: " + trustStore)
    System.setProperty( 'javax.net.ssl.trustStore', trustStore )
    System.setProperty( 'javax.net.ssl.keyStorePassword', "together" )

    //Downloading from the Cubit Project Build Library... "guest" access...
    if (osName == "linux") {
        if (Ant.project.properties."x64") {
	        Ant.get(dest: "${csvnbinDir}/" +
                    "svn-apache-viewvc-linux-latest.tar.gz", 
                src: "https://mgr.cubit.sp.collab.net/pbl/svnedge/pub/" +
                    "3rdPartyPkgs/linux/" +
                    "CollabNet_Subversion-Linux-x86_64-latest.tar.gz")
        } else {
	        Ant.get(dest: "${csvnbinDir}/" +
	                    "svn-apache-viewvc-linux-latest.tar.gz", 
	                src: "https://mgr.cubit.sp.collab.net/pbl/svnedge/pub/" +
	                    "3rdPartyPkgs/linux/" +
	                    "CollabNet_Subversion-Linux-x86_32-latest.tar.gz")
        }
    } else
    if (osName == "windows") {
        Ant.get(dest: "${csvnbinDir}/" +
                    "svn-apache-viewvc-windows-latest.zip", 
                src: "https://mgr.cubit.sp.collab.net/pbl/svnedge/pub/" +
                    "3rdPartyPkgs/windows/" +
                    "CollabNet_Subversion-Win32-latest.zip")
    } else
    if (osName == "mac") {
        System.err.println("Feature not implemented for Mac")
        System.exit(1)
    }

    Ant.get(dest: "${csvnbinDir}/appserver/webapps/integration.war",
            src: "https://mgr.cubit.sp.collab.net/pbl/svnedge/pub/" +
                 "3rdPartyPkgs/CTF/integration-latest.war")
    Ant.get(dest: "${csvnbinDir}/lib/integration-scripts.zip",
            src: "https://mgr.cubit.sp.collab.net/pbl/svnedge/pub/" +
            "3rdPartyPkgs/CTF/integration-scripts-latest.zip")

    rearrangingArtifacts()
}

target(rearrangingArtifacts: 'Moves downloaded artifacts to dist directory') {
    Ant.echo(message: "Organizing the CSVN binary directories for development")
    if (osName == "linux") {
        Ant.exec(dir:"${csvnbinDir}", executable: "tar") {
            arg(line: "-xpf")
            arg(line: "${csvnbinDir}/svn-apache-viewvc-linux-latest.tar.gz")
        }
        Ant.delete(file: "${csvnbinDir}/svn-apache-viewvc-linux-latest.tar.gz")
    } else
    if (osName == "windows") {
        Ant.unzip(src: "${csvnbinDir}/svn-apache-viewvc-windows-latest.zip",
                  dest:"${csvnbinDir}")
        Ant.delete(file: "${csvnbinDir}/svn-apache-viewvc-windows-latest.zip")
    } else
    if (osName == "mac") {

    }
    Ant.echo(message: "CSVN development binary: ${csvnbinDir}")
}

setDefaultTarget("build")
