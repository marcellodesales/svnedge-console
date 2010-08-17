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
import grails.util.Metadata

import com.sun.pkg.client.Image
import com.sun.pkg.client.Image.FmriState

/**
 * Gant script that creates a tar.gz package with the console app. More
 * info at http://gant.codehaus.org/Targets
 *
 * @author Marcello de Sales (mdesales@collab.net)
 *
 * @since 0.1.1
 */

includeTargets << grailsScript("_GrailsWar")

target(build: 'Builds the distribution file structure') {
    depends(war)

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

    Ant.echo(message: "Building the distribution system for $osName")
    def version = metadata.getApplicationVersion()
    if(version) {
        version = '-'+version
    } else {
        version = ''
    }
    createDistributionStructure()
}

target(createDistributionStructure: 'Creates the distribution structure') {
    Ant.echo(message: "Creating a fresh distribution structure")

    distDir = "${basedir}/dist"
    Ant.delete(dir: distDir)
    Ant.mkdir(dir: distDir)

    libDir = distDir + "/lib"
    webAppsDir = distDir + "/appserver/webapps"

    tmpDir = distDir + "/tmp"
    Ant.mkdir(dir: tmpDir )

    if (osName == "windows") {
        // On Windows, put all files in updates directory
        updatesDir = "${distDir}/updates"
        Ant.mkdir(dir: updatesDir)
        updatesLibDir = "${updatesDir}/lib"
        Ant.mkdir(dir: updatesLibDir)
        updatesBinDir = "${updatesDir}/bin"
        Ant.mkdir(dir: updatesBinDir)
        updatesWebAppsDir = updatesDir + "/appserver/webapps"
    }

    downloadArtifacts()
}

target(downloadArtifacts: 'Downloads needed artifacts') {
    Ant.echo(message: "Downloading the needed artifacts")
    //Uploading the necessary contents to the Cubit's PBL with the user's
    // personal key API to the public directory
    //pbl.py upload -k e3554e80-1463-1374-81cc-6fc24548303a -u mdesales -l 
    // https://mgr.cubit.sp.collab.net/cubit_api/1 -p svnedge -t pub -r 
    // /3rdPartyPkgs -d "CHANGE_THIS_DESCRIPTION" -v ~/local-3rdparty-packages/*

    //Generating the truststore files for downloading
    //echo | openssl s_client -connect mgr.cubit.sp.collab.net:443 | 
    //  openssl x509 -inform PEM -outform DER -trustout -out outfile.crt
    //keytool -import -storepass together -file outfile.crt -keystore 
    //  trust.keystore -alias mgrcubitsp
    //keytool -keystore trust.keystore -list >> enter password "together"
    def trustStore = "${basedir}/scripts/" +
            "cubit.keystore"
    Ant.echo(message: "Truststore File: " + trustStore)
    System.setProperty( 'javax.net.ssl.trustStore', trustStore )
    System.setProperty( 'javax.net.ssl.keyStorePassword', "together" )

    //downloading from the Cubit Project Build Library... "guest" access...
    if (osName == "linux") {
        if (Ant.project.properties."x64") {
            Ant.get(dest: "${distDir}/svn-apache-viewvc-latest.tar.gz",
                src: "https://mgr.cubit.sp.collab.net/pbl/svnedge/pub/" +
                    "3rdPartyPkgs/linux/" +
                    "CollabNet_Subversion-Linux-x86_64-latest.tar.gz")
        } else {
            Ant.get(dest: "${distDir}/svn-apache-viewvc-latest.tar.gz",
                    src: "https://mgr.cubit.sp.collab.net/pbl/svnedge/" +
                    "pub/3rdPartyPkgs/linux/" +
                    "CollabNet_Subversion-Linux-x86_32-latest.tar.gz")
        }
    } else
    if (osName == "windows") {
        Ant.get(dest: "${distDir}/svn-apache-viewvc-windows-latest.zip",
                src: "https://mgr.cubit.sp.collab.net/pbl/svnedge/pub/" +
                    "3rdPartyPkgs/windows/" +
                    "CollabNet_Subversion-Win32-latest.zip")
    } else
    if (osName == "mac") {
        System.err.println("Feature not implemented for Mac")
        System.exit(1)
    }
    rearrangingArtifacts()
}

target(rearrangingArtifacts: 'Moves downloaded artifacts to dist directory') {
    Ant.echo(message: "Building the distribution system for ${osName}")

    if (osName == "linux") {
        Ant.exec(dir: "${distDir}", executable: "tar") {
            arg(line: "-xpf")
            arg(line: "${distDir}/svn-apache-viewvc-latest.tar.gz")
        }
        Ant.delete(file: "${distDir}/svn-apache-viewvc-latest.tar.gz")

         //Copying the service wrapper artifacts
        Ant.copy(file: "${basedir}/csvn-service-wrapper" +
            "/linux/bin/csvn",
            todir: "${distDir}/bin")
        Ant.copy(file: "${basedir}/csvn-service-wrapper" +
            "/linux/bin/csvn-httpd",
            todir: "${distDir}/bin")
        Ant.copy(file: "${basedir}/csvn-service-wrapper" +
            "/linux/bin/wrapper-linux-x86-32",
            todir: "${distDir}/bin")
        Ant.copy(file: "${basedir}/csvn-service-wrapper" +
            "/linux/bin/wrapper-linux-x86-64",
            todir: "${distDir}/bin")
        Ant.copy(file: "${basedir}/csvn-service-wrapper" +
            "/linux/bin/start.ini",
            todir: "${distDir}/bin")
        Ant.chmod(dir: distDir + "/bin", perm: "a+x",
            includes: "csvn*")
        Ant.chmod(dir: distDir + "/bin", perm: "a+x",
            includes: "wrapper-linux-x86*")
        Ant.copy(file: "${basedir}/csvn-service-wrapper" +
            "/linux/conf/csvn-wrapper.conf",
            todir: "${distDir}/data/conf")
        Ant.copy(file: "${basedir}/csvn-service-wrapper" +
            "/linux/conf/csvn.conf.dist",
            todir: "${distDir}/data/conf")
        Ant.copy(file: "${basedir}/csvn-service-wrapper" +
            "/linux/lib/wrapper.jar",
            todir: "${distDir}/lib")
        Ant.copy(file: "${basedir}/csvn-service-wrapper" +
            "/linux/lib/libwrapper-linux-x86-32.so",
            todir: "${distDir}/lib")
       Ant.copy(file: "${basedir}/csvn-service-wrapper" +
            "/linux/lib/libwrapper-linux-x86-64.so",
            todir: "${distDir}/lib")
        // Copy the SIGAR libraries to lib folder which is on java.library.path
        Ant.copy(file: "${basedir}/ext" +
            "/sigar/libsigar-amd64-linux.so",
            todir: "${distDir}/lib")
        Ant.copy(file: "${basedir}/ext" +
            "/sigar/libsigar-x86-linux.so",
            todir: "${distDir}/lib")

    } else
    if (osName == "windows") {
        // On Windows, put all files in updates directory
        Ant.unzip(src: "${distDir}/" +
                    "svn-apache-viewvc-windows-latest.zip",
                dest:"${updatesDir}")
        Ant.delete(file: "${distDir}/" +
                "svn-apache-viewvc-windows-latest.zip")

        //copying the service wrapper artifacts
        Ant.copy(todir: "${updatesBinDir}") {
            fileset(dir: "${basedir}/csvn-service-wrapper" +
                "/windows-x86-32/bin",
                includes:"**/*")
        }
        Ant.copy(todir: "${updatesLibDir}") {
            fileset(dir: "${basedir}/csvn-service-wrapper" +
                "/windows-x86-32/lib",
                includes:"**/*")
        }
        Ant.copy(todir: "${updatesDir}/data/conf") {
            fileset(dir: "${basedir}/csvn-service-wrapper" +
                "/windows-x86-32/conf",
                includes:"**/*")
        }
        Ant.copy(todir: "${updatesDir}/svcwrapper") {
                fileset(dir: "${basedir}/svcwrapper",
                includes:"**/*")
        }
        // Copy SIGAR library to bin folder which is on PATH
        Ant.copy(todir: "${updatesBinDir}") {
            fileset(dir: "${basedir}/ext/sigar/",
                includes:"**/sigar-*-winnt.dll")
        }

    } else
    if (osName == "mac") {

    }

    //copying all the version-controlled artifacts (data, config, statics, etc)
    if (osName == "windows") {
        //move the console war file to the library dir
        Ant.copy(todir: updatesDir) {
            fileset(dir: "${basedir}/svn-server",
            includes:"**/*")
        }
        Ant.move(file: warName, 
                 tofile: "${updatesWebAppsDir}/csvn.war")
            //move the data directory as temp-data (artf62798) for packaging
            //The bootstrap process must move this directory back to data
        Ant.move(file: updatesDir + "/data", tofile:
                distDir + "/temp-data" )

        Ant.get(dest: "${updatesWebAppsDir}/integration.war",
                src: "https://mgr.cubit.sp.collab.net/pbl/svnedge/pub/" +
                    "3rdPartyPkgs/CTF/integration-latest.war")
        Ant.get(dest: "${updatesLibDir}/integration-scripts.zip",
            src: "https://mgr.cubit.sp.collab.net/pbl/svnedge/pub/" +
            "3rdPartyPkgs/CTF/integration-scripts-latest.zip")
    
    } else {
        //move the console war file to the library dir
        Ant.copy(todir: distDir) {
            fileset(dir: "${basedir}/svn-server",
                    includes:"**/*")
        }
        Ant.move(file: warName, tofile: "${webAppsDir}/" +
                "csvn.war")
        Ant.chmod(dir: distDir + "/bin/cgi-bin",
            perm: "a+x", includes: "*.cgi")
        //move the data directory as temp-data (artf62798) for packaging
        //The bootstrap process must move this directory back to data
        Ant.move(file: distDir + "/data", tofile:
            distDir + "/temp-data" )

        Ant.get(dest: "${webAppsDir}/integration.war",
                src: "https://mgr.cubit.sp.collab.net/pbl/svnedge/pub/" +
                    "3rdPartyPkgs/CTF/integration-latest.war")
        Ant.get(dest: "${distDir}/lib" +
            "/integration-scripts.zip",
            src: "https://mgr.cubit.sp.collab.net/pbl/svnedge/pub/" +
            "3rdPartyPkgs/CTF/integration-scripts-latest.zip")

        Ant.chmod(file: distDir + "/bin/collabnetsvn-config", perm: "+x")
        Ant.chmod(file: distDir + "/bin/svndbadmin", perm: "+x")
    }
    // Make logs directory.  App needs it to start
    Ant.mkdir(dir: "${distDir}/temp-data/logs")
    Ant.delete(dir: tmpDir)

    // create and populate the dist directory which contains our base 
    // configuration files these will be installed on users system for 
    // backup and reference purposes
    distdataDir = "${distDir}/dist"
    Ant.mkdir(dir: distdataDir)
    Ant.copy(file: "${distDir}/temp-data/conf/httpd.conf.dist",
         todir: "${distdataDir}")
    Ant.copy(file: "${distDir}/temp-data/conf/viewvc.conf.dist",
         todir: "${distdataDir}")
    Ant.copy(file: "${distDir}/temp-data/conf/teamforge.properties.dist",
         todir: "${distdataDir}")
    Ant.copy(file: "${distDir}/temp-data/conf/csvn-wrapper.conf",
         todir: "${distdataDir}")
    if (osName == "linux") {
	    Ant.copy(file: "${distDir}/temp-data/conf/csvn.conf.dist",
	         todir: "${distdataDir}")    
    }
    Ant.delete(file: "${distDir}/temp-data/conf/httpd.conf")
    if (osName == "windows") {
        Ant.copy(file: "${updatesDir}/appserver/etc/jetty.xml",
                todir: "${distdataDir}")
    } else {
        Ant.copy(file: "${distDir}/appserver/etc/jetty.xml", 
                todir: "${distdataDir}")
    }
    // Move the Windows install-updates files to the dist directory
    if (osName == "windows") {
        Ant.move(file: "${updatesBinDir}/install-updates.bat",
                toDir: distdataDir)
        Ant.move(file: "${updatesBinDir}/wait.bat",
                toDir: distdataDir)
        // Copy everything from updates folder to dist folder so local 
        // testing can be done
        Ant.copy(todir: distDir) {
            fileset(dir: updatesDir, includes:"**/*")
        }
    }

    event("StatusFinal", ["Distribution directory created successfully: " +
                  "${distDir}"])
}

setDefaultTarget("build")
