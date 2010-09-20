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
package com.collabnet.svnedge.console.services

import java.util.regex.Pattern;

import com.collabnet.svnedge.console.ConfigUtil

class CommandLineService {

    boolean transactional = false

    def operatingSystemService

    int executeWithStatus(String...command, Map<String, String> env=null, String input=null) {
        return Integer.parseInt(execute(command, (Map<String, String>) env, input)[0])
    }

    String executeWithOutput(String...command, Map<String, String> env=null, String input=null) {
        return execute(command, (Map<String, String>) env, input)[1]
    }

    /**
     * this method will launch the input command and return immediately without waiting for result or output
     */
    void executeDetached(String command)  {

        Runtime.getRuntime().exec(command)
        return
    }

    String[] execute(String... command, Map<String, String> env=null, String input=null) {
        Process p = startProcess(command, env)
        def output = new StringBuffer(512)
        def error = new StringBuffer(512)
        if (input) {
            p.out.write(input.getBytes())
        }
        p.out.close()
        p.waitForProcessOutput(output, error)
        def exitStatus = p.waitFor()
        log.debug("Command: " + command + " result=" + exitStatus)
        if (output.length() > 0) {
            log.debug("Process output: " + output)
        }
        if (error.length() > 0) {
            if (exitStatus == 0) {
                // Some apps write to stderr even though they start normally, 
                // e.g. httpd
                log.debug("Process err output: " + error)
            } else {
                log.error("Exit status=" + exitStatus + 
                         " Process err output: " + error)                
            }
        }
        return [String.valueOf(exitStatus), output.toString(), error.toString()]
    }

    private static final Collection<String> INTERESTING_ENVVARS = 
        new HashSet(["JAVA_HOME", "LD_PRELOAD", "PWD", "HOSTNAME", 
        "SSH_TTY", "LOGNAME", "LD_LIBRARY_PATH", "SSH_CONNECTION", 
        "SHELL", "PATH", "USER", "HOME", "PYTHONPATH"]) 

    private Process startProcess(String... command, Map<String, String> addEnv) {
        ProcessBuilder pb = new ProcessBuilder(command)
        Map<String, String> env = pb.environment();
        if (null != addEnv) {
            env.putAll(addEnv)
        }
        if (log.isDebugEnabled()) {
            log.debug(env.findAll({key, value -> 
                INTERESTING_ENVVARS.contains(key)})
                .collect({key, value -> key + ":" + value}))
        }
        pb.directory(new File(ConfigUtil.appHome()))
        log.debug("Calling pb.start() for command=" + command)
        return pb.start()
    }
    
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("sorry|password", Pattern.CASE_INSENSITIVE)

    /**
     * Executes the command with the assumption that it will either request a password or return
     * fairly quickly.  Returns whether process requests a password.
     */
    boolean testForPassword(String... command, Map<String, String> env=null) {
        Process p = startProcess(command, env)
        def output = new StringBuffer(512)
        def error = new StringBuffer(512)
        p.consumeProcessOutput(output, error)
        p.out.close()
        Integer exitStatus = null
        boolean isPassword = false
        while (null == exitStatus && !isPassword) {
            try {
                String match = error.toString().find(PASSWORD_PATTERN)
                if (null == match) {
                    match = output.toString().find(PASSWORD_PATTERN)
                }
                isPassword = (null != match)
                exitStatus = p.exitValue()
            } catch (IllegalThreadStateException e) {
                Thread.sleep(100)
                log.debug("testForPassword error=" + error + " out=" + output +
                          " isPassword? " + isPassword)
            }
        }
        if (null == exitStatus) {
            p.destroy()
        }
        log.debug("testForPassword command: " + command + " result=" + exitStatus)
        if (output.length() > 0) {
            log.debug("Process output: " + output)
        }
        if (error.length() > 0) {
            log.debug("Process error: " + error)
        }
        isPassword
    }
    
    /**
     * Creates a file: scheme URI which is usable by svn command line clients
     */
    def createSvnFileURI(File f) {
        // toURI returns a "file:/Drive:/... URI in windows, but 
        // svn expects 3 preceding slashes e.g. file:///Drive:/...
        def uri = f.toURI().toString()
        if (uri.startsWith("file:/") && uri.charAt(6) != '/') {
            uri = "file:///" + uri.substring(6)    
        }
        return uri
    }
}
