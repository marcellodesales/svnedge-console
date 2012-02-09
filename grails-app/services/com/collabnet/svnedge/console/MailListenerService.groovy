/*
 * CollabNet Subversion Edge
 * Copyright (C) 2012, CollabNet Inc. All rights reserved.
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
package com.collabnet.svnedge.console

import com.collabnet.svnedge.console.AbstractSvnEdgeService
import com.collabnet.svnedge.domain.MailConfiguration
import com.collabnet.svnedge.domain.MailAuthMethod
import com.collabnet.svnedge.domain.MailSecurityMethod
import com.collabnet.svnedge.domain.Server
import com.collabnet.svnedge.domain.User
import com.collabnet.svnedge.event.LoadRepositoryEvent;
import com.collabnet.svnedge.event.RepositoryEvent
import com.collabnet.svnedge.event.DumpRepositoryEvent

import org.springframework.beans.factory.InitializingBean
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.context.ApplicationListener

import grails.util.GrailsUtil;

import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import javax.mail.AuthenticationFailedException
import javax.mail.MessagingException

/**
 * The listener for events which can result in a mail notification. 
 * 
 * Implemented as a separate class from MailConfigurationService as that service
 * requires use of the afterPropertiesSet method which throws an error related
 * to the domain object not having its grails methods attached yet,
 * if transactional = false, but the proxy which is created when 
 * transactional = true, causes 2 listener registrations
 */
class MailListenerService extends AbstractSvnEdgeService
    implements ApplicationListener<RepositoryEvent> {

    // Default sample addresses shipped with the product
    private def INVALID_ADDRESSES = ['admin@example.com', 'devnull@collab.net']
        
    // this is needed so grails does not create a proxy resulting in 
    // registering the listener twice
    static transactional = false

    def svnRepoService
    
    void onApplicationEvent(RepositoryEvent event) {
        MailConfiguration config = MailConfiguration.getConfiguration()
        if (config.enabled) {
            onEnabledMail(event, config)
        } else {
            log.debug "Email notifications are disabled."
        }
    }
        
    private void onEnabledMail(RepositoryEvent event, MailConfiguration config) {
        def fromAddress = config.createFromAddress()
        Server server = Server.getServer()
        def defaultAddress = server.adminEmail
        def toAddress = null 
        if (!INVALID_ADDRESSES.contains(defaultAddress)) {
            toAddress = defaultAddress
        }
        def ccAddress = null
        boolean sendOnSuccess = false
        User user = retrieveUserForEvent(event)
        if (user?.email && !INVALID_ADDRESSES.contains(defaultAddress)) {
            toAddress = user.email
            sendOnSuccess = true
            if (toAddress != defaultAddress && !event.isSuccess &&
                    !INVALID_ADDRESSES.contains(defaultAddress)) {
                ccAddress = defaultAddress
            }
        }
        
        // don't send email to server admin unless an error occurs
        if (toAddress && (sendOnSuccess || !event.isSuccess)) {
            switch (event) {
                case DumpRepositoryEvent:
                    sendDumpMail(toAddress, ccAddress, fromAddress, event)
                break
                case LoadRepositoryEvent:
                    sendLoadMail(toAddress, ccAddress, fromAddress, event)
                break
            }
        }
    }

    private static final long MAX_ATTACHMENT_SIZE = 104858
    
    private byte[] getProcessOutput(RepositoryEvent event) {
        File f = event.processOutput
        byte[] s = null
        if (f?.exists()) {
            if (f.length() < MAX_ATTACHMENT_SIZE) {
                s = f.bytes
            } else {
                long skipBytes = f.length() - MAX_ATTACHMENT_SIZE
                s = new byte[MAX_ATTACHMENT_SIZE]
                f.withInputStream {
                    it.skip(skipBytes)
                    it.read(s)
                }
            }
        }
        return s
    }
        
    private void sendDumpMail(toAddress, ccAddress, fromAddress, event) {
        def repo = event.repo
        def dumpBean = event.dumpBean
        Locale locale = dumpBean.userLocale
        def mailSubject = getMessage(event.isSuccess ? 
                'mail.message.subject.success' : 'mail.message.subject.error',
                null, locale)
        mailSubject += getMessage(dumpBean.isBackup() ? 
                'mail.message.dump.subject.backup' : 
                'mail.message.dump.subject.adhoc', null, locale)
        mailSubject += getMessage('mail.message.repository', [repo.name], locale)
       
        def mailBody
        byte[] processOutput
        if (event.isSuccess) {
            def filename = svnRepoService.dumpFilename(dumpBean, repo)
            Server server = Server.getServer()
            def urlPrefix = server.consoleUrlPrefix()
            def repoLink = urlPrefix + '/repo/dumpFileList/' + repo.id
            def downloadLink = urlPrefix + '/repo/downloadDumpFile/' + repo.id +
                    "?filename=" + filename
            mailBody = getMessage('mail.message.dump.body.success', 
                [(dumpBean.hotcopy ? 1 : 0), repo.name, 
                 filename, repoLink, downloadLink], locale)
        } else {
            processOutput = getProcessOutput(event)
            if (event.exception) {
                def e = event.exception
                GrailsUtil.deepSanitize(e)
                mailBody = getMessage('mail.message.dump.body.error',
                        [dumpBean.hotcopy ? 1 : 0, dumpBean.backup ? 0 : 1, 
                         repo.name, e.message,
                         e.class.name, e.getStackTrace().join('\n'),
                         processOutput ? 1 : 0,
                         isPartial(processOutput) ? 1 : 0,
                         event.processOutput?.name], locale)
            } else {
                mailBody = getMessage('mail.message.dump.body.error',
                        [dumpBean.hotcopy ? 1 : 0, dumpBean.backup ? 0 : 1, 
                         repo.name, '', '', '', processOutput ? 1 : 0,
                         isPartial(processOutput) ? 1 : 0,
                         event.processOutput?.name], locale)
            }
        }
        mailBody += getMessage('mail.message.footer', null, locale)
        sendMail(toAddress, ccAddress, fromAddress, 
                 mailSubject, mailBody, processOutput)
    }
    
    private boolean isPartial(byte[] progressContent) {
        return progressContent && progressContent.length == MAX_ATTACHMENT_SIZE
    }

    private void sendLoadMail(toAddress, ccAddress, fromAddress, event) {
        def repo = event.repo
        Locale locale = event.locale ?: Locale.getDefault()
        def mailSubject = getMessage(event.isSuccess ?
                'mail.message.subject.success' : 'mail.message.subject.error',
                null, locale)
        mailSubject += getMessage('mail.message.load.subject', null, locale)
        mailSubject += getMessage('mail.message.repository', [repo.name], locale)
       
        def mailBody
        byte[] processOutput
        if (event.isSuccess) {
            mailBody = getMessage('mail.message.load.body.success',
                    [repo.name], locale)
        } else {
            processOutput = getProcessOutput(event)
            if (event.exception) {
                def e = event.exception
                GrailsUtil.deepSanitize(e)
                mailBody = getMessage('mail.message.load.body.error',
                        [repo.name, e.message,
                         e.class.name, e.stackTrace.join('\n'),
                         processOutput ? 1 : 0,
                         isPartial(processOutput) ? 1 : 0,
                         event.processOutput?.name], locale)
            } else {
                mailBody = getMessage('mail.message.load.body.error',
                        [repo.name, '', '', '', processOutput ? 1 : 0,
                         isPartial(processOutput) ? 1 : 0,
                         event.processOutput?.name], locale)
            }
        }
        mailBody += getMessage('mail.message.footer', null, locale)
        sendMail(toAddress, ccAddress, fromAddress, 
                 mailSubject, mailBody, processOutput)
    }
    
    private void sendMail(toAddress, ccAddress, fromAddress, 
                          mailSubject, mailBody, processOutput) {
        try {
            sendMail {
                if (processOutput) {
                    multipart true
                }
                to toAddress
                if (ccAddress) {
                    cc ccAddress
                }
                from fromAddress
                subject mailSubject
                body mailBody
                if (processOutput) {
                    attachBytes "ProcessOutput.txt", "text/plain", processOutput
                }
            }
            
        } catch (Exception e) {
            log.warn("Exception while sending mail. To: " + toAddress + 
                    "\nSubject: " + mailSubject + "\nBody:\n" + mailBody, e)
        }
    }

    private User retrieveUserForEvent(RepositoryEvent event) {
        Integer userId = (event instanceof DumpRepositoryEvent && 
                          event.dumpBean.isBackup()) ? 
                null : event.userId
        User user = null
        try {
            user = userId ? User.get(event.userId) : null
        } catch (Exception e) {
            log.warn("Error in user lookup", e)
        }
        return user
    }

}

