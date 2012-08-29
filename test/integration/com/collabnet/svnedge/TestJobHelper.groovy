package com.collabnet.svnedge

import org.quartz.JobListener
import org.quartz.JobExecutionContext

class TestJobHelper implements JobListener {
    def log
    String listenerName
    String jobName
    boolean jobIsFinished = false
    
    
    synchronized void waitForJob() {
        if (jobIsFinished) {
            log.info("Job is finished, no need to wait.")
        } else {
            log.info("Job triggered; waiting to finish...")
            this.wait(180000)
            log.info("Wait is over! Continuing test")
        }
    }
    
    private synchronized void notifyOnFinishedJob() {
        jobIsFinished = true
        log.info("Job is done!")
        this.notify()
    } 
    
    /** Listener methods **/
    public String getName() {
        return listenerName
    }
    
    void jobToBeExecuted(JobExecutionContext context) {}
    
    void jobExecutionVetoed(JobExecutionContext context) {
        notifyOnFinishedJob()
        throw new RuntimeException("Did not expect job to be vetoed.")
    }

    void jobWasExecuted(JobExecutionContext context,
                        org.quartz.JobExecutionException jobException) {
        if (context.getJobDetail().getName().equals(jobName)) {
            notifyOnFinishedJob()
        }
    }
}
