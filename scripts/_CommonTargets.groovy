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

target(prepare: 'Prepares properties and fields') {

    Ant.condition(property:"windowsPrepare") {
        and() {
            os(family:"windows")
        }
    }
    Ant.condition(property:"solarisPrepare") {
        and() {
            os(family:"unix", name:"sunos")
        }
    }
    Ant.condition(property:"linuxPrepare") {
        and() {
            os(family:"unix")
            not() { isset(property:"solarisPrepare") }
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
    if (Ant.project.properties."solarisPrepare") {
        osName = "solaris"
    }
    Ant.property(name: "osName", value: osName)

    distDir = Ant.project.properties.'distDir'

    urlPrefix = "https://mgr.cubit.sp.collab.net/pbl/svnedge/pub/3rdPartyPkgs/"

    archiveFile = "${distDir}/svn-apache-viewvc-binaries" + 
        ((osName == "windows") ? ".zip" : ".tar.gz")

    webAppsDir = distDir + "/appserver/webapps"
}

target(downloadArtifacts: 'Downloads the csvn binaries') {

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

    def bits = "32"
    if (Ant.project.properties."x64") {
        bits = "64"
    }
    //Downloading from the Cubit Project Build Library... "guest" access...
    if (osName == "linux") {
        Ant.get(dest: archiveFile,
                src: urlPrefix + "linux/" +
                "CollabNet_Subversion-Linux-x86_${bits}-latest.tar.gz")

    } else if (osName == "solaris") {
        def proc = System.getProperty("os.arch").startsWith("sparc") ?
	    "SPARC" : "SPARC" // FIXME! should be "x86"
	if (!System.getProperty("os.arch").startsWith("sparc")) {
            Ant.echo("!!!!!!! x86 arch binaries don't exist yet.  " +
	        "Downloading sparc binaries, but they won't work.")
        }
        Ant.get(dest: archiveFile, 
                src: urlPrefix + "solaris/" +
                "CollabNet_Subversion-Sol10-${proc}_${bits}-latest.tar.gz")


    } else if (osName == "windows") {
        Ant.get(dest: archiveFile,
            src: urlPrefix + "windows/CollabNet_Subversion-Win32-latest.zip")
    } else
    if (osName == "mac") {
        System.err.println("Feature not implemented for Mac")
        System.exit(1)
    }

    Ant.get(dest: "${webAppsDir}/integration.war",
            src: urlPrefix + "CTF/integration-latest.war")
    Ant.get(dest: "${distDir}/lib/integration-scripts.zip",
            src: urlPrefix + "CTF/integration-scripts-latest.zip")
}
