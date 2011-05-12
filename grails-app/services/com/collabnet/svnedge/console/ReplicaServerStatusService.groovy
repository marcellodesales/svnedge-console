package com.collabnet.svnedge.console

import java.util.concurrent.BlockingQueue;

import grails.converters.JSON;

import org.cometd.Client;
import org.mortbay.cometd.ChannelImpl;
import org.springframework.beans.factory.InitializingBean;

import com.collabnet.svnedge.util.CommandLineOutputListener;
import static com.collabnet.svnedge.console.CommandLineService.COMMAND_TERMINATED;

public final class ReplicaServerStatusService implements InitializingBean {

    boolean transactional = false
    def commandLineService
    /**
     * Auto-wired Cometd bayeux server
     */
    def bayeux
    /**
     * The bayeux publisher client
     */
    private Client bayeuxPublisherClient
    /**
     * The Bayeux publisher Status message progress channel
     */
    private ChannelImpl commandLineChannel
    /**
     * The command output lines of a command that is requested to be executed.
     */
    private BlockingQueue<String> commandOutputLines

    def bootStrap = { 
    }

    // just like @PostConstruct
    void afterPropertiesSet() {
        this.bayeuxPublisherClient = this.bayeux.newClient(this.class.name)
        def repoCommandLineChannel = "/csvn-repocommands/output-line"
        def create = true
        this.commandLineChannel = this.bayeux.getChannel(repoCommandLineChannel,
            create)
    }

    /**
     * An asynchronous publisher that publishes the given message in the 
     * Bayeux server with the following Json doc:
     * 
     *  {
     *      commandOutputLine: string = a line of the command.
     *  }
     *  
     */
    private void publishPercentageMessage(commandOutputLine) {
        def writer = new StringWriter();
        def response = [commandOutputLine: commandOutputLine]
        def jsonRes = (response as JSON).toString()
        log.debug("Command output line: " + jsonRes)
        this.commandLineChannel.publish(this.bayeuxPublisherClient, jsonRes,
            null)
    }

    /**
     * The implemented method from the CommandLineOutputListener.
     * @param commandOutputLine is the line of the output of the command.
     */
    void receiveCommandLineOutputLine(String ommandOutputLine) {
        if (!commandOutputLine.equals(COMMAND_TERMINATED)) {
            publishPercentageMessage(commandOutputLine)
        }
    }
}
