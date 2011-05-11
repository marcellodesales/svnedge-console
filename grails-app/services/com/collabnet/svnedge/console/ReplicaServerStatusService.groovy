package com.collabnet.svnedge.console

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import grails.converters.JSON;

import org.cometd.Client;
import org.mortbay.cometd.ChannelImpl;
import org.springframework.beans.factory.InitializingBean;

import com.collabnet.svnedge.util.RealTimeCommandLineListener;
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
     * Non-blocking command executor with this instance with the instance
     * of this service.
     * @param command command to be executed.
     */
    def executeCommand(command) {
        def bool = this instanceof RealTimeCommandLineListener
        commandLineService.executeWithCommandLineListener(command, this)
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

    /**
     * Executes a command in the background and returns a listener reference to be
     * @param command
     * @return
     */
    public RealTimeCommandLineListener execute(String command) {
        def outputQueue = new LinkedBlockingQueue<String>()
        def listener = new RealTimeCommandLineListener(outputQueue)
        commandLineService.executeWithCommandLineListener(command, outputQueue)
        return listener
    }

    public static void main(String[] args) {
        def cmd = "ping www.google.com -c 5"

        def realTimeCmd = new ReplicaServerStatusService()
        realTimeCmd.commandLineService = new CommandLineService()
        def cmdListener = realTimeCmd.execute(cmd)

        println "The output... ###########################"
        def line
        while ((line = cmdListener.getNextOutputLine()) != null) {
             println line
        }
    }
}
