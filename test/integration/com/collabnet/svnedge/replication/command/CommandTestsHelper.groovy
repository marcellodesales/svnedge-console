package com.collabnet.svnedge.replication.command

/**
 * Helper class for common code used in testing the replication command subsystem
 */
class CommandTestsHelper {

    /**
     * creates a random command id in the form "cmdexecNNNN"
     * @return
     */
    public static String createCommandId() {
        def prefix = "cmdexec"
        def id = Math.round(Math.random() * 8999) + 1000
        return prefix + id
    }
}
