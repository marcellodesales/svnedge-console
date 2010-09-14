pkg = {

    "name" : "csvn",

    "version" : "@{version}",

    "attributes" : {
        "pkg.summary" : "CollabNet Subversion Edge",
        "pkg.description" : "CollabNet Subversion Edge is a web application that manages the Apache Subversion server.",
        "info.classification" : "Web Application"
    },

    "defaults" : { "file" : {"mode" : "0755", "group" : "bin", "owner" : "root",},
                   "dir"  : {"mode" : "0755", "group" : "bin", "owner" : "root",},
    },

    "files" : {
        "README" : {"mode" : "0755", "os" : "unix"},
        "README.TXT" : {"mode" : "0755", "os" : "windows"},
        "temp-data/conf/csvn-wrapper.conf" : {"mode" : "0755"},
        "bin/csvn" : {"mode" : "0755", "os" : "unix"},
        "bin/csvn-httpd" : {"mode" : "0755", "os" : "unix"},
        "bin/wrapper-linux-x86-32" : {"mode" : "0755", "os" : "unix"},
        "bin/wrapper-linux-x86-64" : {"mode" : "0755", "os" : "unix"},
        "bin/start.ini" : {"mode" : "0755", "os" : "unix"},
        "lib/libsigar-amd64-linux.so" : {"mode" : "0755", "os" : "unix"},
        "lib/libsigar-x86-linux.so" : {"mode" : "0755", "os" : "unix"},
        "lib/libwrapper-linux-x86-32.so" : {"mode" : "0755", "os" : "unix"},
        "lib/libwrapper-linux-x86-64.so" : {"mode" : "0755", "os" : "unix"},
        "lib/wrapper.jar" : {"mode" : "0755", "os" : "unix"},
        "updates/bin/sigar-amd64-winnt.dll" : {"mode" : "0755", "os" : "windows"},
        "updates/bin/sigar-x86-winnt.dll" : {"mode" : "0755", "os" : "windows"},
    },
    
    "dirtrees" : {
        "appserver"       : {"mode" : "0755", "os" : "unix"},
        "updates/appserver" : {"mode" : "0755", "os" : "windows"},
        "updates/svcwrapper" : {"mode" : "0755", "os" : "windows"},
    },

    "depends" : {
        "pkg:/csvn-svn@1.6.12" : {"type" : "require" },
    },

    "licenses" : {
        "licenses/agpl-3.0.txt" : {"license" : "AGPLv3" },
    },
}
