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

    // this is needed so grails does not create a proxy resulting in 
    // registering the listener twice
    static transactional = false

    def svnRepoService
    
    void onApplicationEvent(RepositoryEvent event) {
        MailConfiguration config = MailConfiguration.getConfiguration()
        def fromAddress = config.createFromAddress()
        Server server = Server.getServer()
        def defaultAddress = server.adminEmail
        def toAddress = defaultAddress
        def ccAddress = null
        boolean sendOnSuccess = false
        User user = retrieveUserForEvent(event)
        if (user?.email) {
            toAddress = user.email
            sendOnSuccess = true
            if (toAddress != defaultAddress && !event.isSuccess) {
                ccAddress = defaultAddress
            }
        }
        
        switch (event) {
            case DumpRepositoryEvent:
                // don't send email to server admin unless an error occurs
                if (sendOnSuccess || !event.isSuccess) {
                    sendDumpMail(toAddress, ccAddress, fromAddress, event)
                }
                break
        }
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
            if (event.exception) {
                def e = event.exception
                GrailsUtil.deepSanitize(e)
                mailBody = getMessage('mail.message.dump.body.error',
                        [dumpBean.hotcopy ? 1 : 0, dumpBean.backup ? 0 : 1, 
                         repo.name, e.message,
                         e.class.name, e.getStackTrace().join('\n')], locale)
            } else {
                mailBody = getMessage('mail.message.dump.body.error',
                        [dumpBean.hotcopy ? 1 : 0, dumpBean.backup ? 0 : 1, 
                         repo.name, '', '', ''], locale)
            }
        }
        mailBody += getMessage('mail.message.footer', null, locale)
 
        try {
            sendMail {
                to toAddress
                if (ccAddress) {
                    cc ccAddress
                }
                from fromAddress
                subject mailSubject
                body mailBody
            }
        } catch (Exception e) {
            log.warn("Exception while sending mail. To: " + toAddress + 
                    "\nSubject: " + mailSubject + "\nBody:\n" + mailBody, e)
        }
    }

    private User retrieveUserForEvent(DumpRepositoryEvent event) {
        User user = null
        DumpBean dumpBean = event.dumpBean
        if (!dumpBean.isBackup()) {
            try {
                user = dumpBean.userId ? User.get(dumpBean.userId) : null
            } catch (Exception e) {
                log.warn("Error in user lookup", e)
            }
        }
        return user
    }

}

