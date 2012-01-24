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

import javax.mail.Message
import javax.mail.internet.MimeMultipart

import grails.test.GrailsUnitTestCase

import com.collabnet.svnedge.TestUtil
import com.collabnet.svnedge.console.CommandLineService
import com.collabnet.svnedge.console.LifecycleService
import com.collabnet.svnedge.console.OperatingSystemService
import com.collabnet.svnedge.console.SvnRepoService
import com.collabnet.svnedge.console.DumpBean
import com.collabnet.svnedge.domain.MailConfiguration;
import com.collabnet.svnedge.domain.Repository
import com.collabnet.svnedge.domain.Server
import com.collabnet.svnedge.domain.User;
import com.collabnet.svnedge.event.DumpRepositoryEvent;
import com.collabnet.svnedge.event.LoadRepositoryEvent;
import com.collabnet.svnedge.util.ConfigUtil
import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetupTest

class MailListenerServiceIntegrationTests extends GrailsUnitTestCase {

    SvnRepoService svnRepoService
    def jobsAdminService
    def quartzScheduler
    def mailConfigurationService
    def mailListenerService
    def grailsApplication
    def greenMail
    
    def repoParentDir
    def repo

    protected void setUp() {
        super.setUp()

        // Setup a test repository parent
        repoParentDir = TestUtil.createTestDir("repo")
        Server server = Server.getServer()
        server.repoParentDir = repoParentDir.getCanonicalPath()
        server.save()
        
        def testRepoName = "test-repo"
        repo = new Repository(name: testRepoName)
        assertEquals "Failed to create repository.", 0,
                svnRepoService.createRepository(repo, true)

        ConfigurationHolder.config = grailsApplication.config
        MailConfiguration mailConfig = new MailConfiguration(
                serverName: 'localhost',
                port: ServerSetupTest.SMTP.port,
                enabled: true)
        mailConfigurationService.saveMailConfiguration(mailConfig)
    }

    protected void tearDown() {
        super.tearDown()
        repoParentDir.deleteDir()
        greenMail.deleteAllMessages()
    }

    public void testDumpSuccess() {        
        DumpBean params = new DumpBean()
        DumpRepositoryEvent event = new DumpRepositoryEvent(
                this, params, repo, DumpRepositoryEvent.SUCCESS, ADMIN_USER_ID)
        
        mailListenerService.onApplicationEvent(event)
        
        assertEquals("Expected one mail message", 1, 
                greenMail.getReceivedMessages().length)
        def message = greenMail.getReceivedMessages()[0]
        assertEquals("Message Subject did not match", 
                "[Success][Adhoc dump]Repository: test-repo", message.subject)
        assertTrue("Message Body did not match", GreenMailUtil.getBody(message)
                .startsWith("The dump of repository '" + repo.name + 
                "' completed."))
        assertEquals("Message From did not match", "SubversionEdge@localhost", 
                GreenMailUtil.getAddressList(message.from))
        assertEquals("Message To did not match", User.get(1).email, 
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.TO)))
        assertNull("Message not expected to have CC recipients", 
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.CC)))
    }

    public void testDumpFail() {
        assertEquals("Expected 0 mail messages to start", 0, 
                greenMail.getReceivedMessages().length)
        
        DumpBean params = new DumpBean()
        DumpRepositoryEvent event = new DumpRepositoryEvent(
                this, params, repo, DumpRepositoryEvent.FAILED, ADMIN_USER_ID,
                null, null, new Exception("testDumpFail"))
        
        mailListenerService.onApplicationEvent(event)

        // Two identical messages (as email is sent to two users)
        assertEquals("Expected two mail message", 2, 
                greenMail.getReceivedMessages().length)
        def message = greenMail.getReceivedMessages()[0]
        def dupeMessage = greenMail.getReceivedMessages()[1]
        
        assertEquals("Message Subject did not match",
                "[Error][Adhoc dump]Repository: test-repo", message.subject)
        assertEquals("Duplicate message subject did not match original", 
                message.subject, dupeMessage.subject)
        
        assertTrue("Message Body did not match ", GreenMailUtil.getBody(message)
                .startsWith("The dump of repository '" + repo.name +
                "' failed."))
        assertEquals("Duplicate message body did not match original", 
                GreenMailUtil.getBody(message), 
                GreenMailUtil.getBody(dupeMessage))
        assertEquals("Message 'From' did not match", "SubversionEdge@localhost",
                GreenMailUtil.getAddressList(message.from))
        assertEquals("Duplicate message 'From' did not match original",
            GreenMailUtil.getAddressList(message.from),
            GreenMailUtil.getAddressList(dupeMessage.from))
        assertEquals("Message 'To' did not match", User.get(1).email, 
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.TO)))
        assertEquals("Duplicate message 'To' did not match original",
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.TO)),
                GreenMailUtil.getAddressList(
                dupeMessage.getRecipients(Message.RecipientType.TO)))
        assertEquals("Message 'CC' did not match", Server.getServer().adminEmail,
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.CC)))
        assertEquals("Duplicate message 'CC' did not match original",
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.CC)),
                GreenMailUtil.getAddressList(
                dupeMessage.getRecipients(Message.RecipientType.CC)))
    }

    private static final int ADMIN_USER_ID = 1
    
    public void testBackupSuccess() {
        DumpBean params = new DumpBean(backup: true)
        DumpRepositoryEvent event = new DumpRepositoryEvent(
                this, params, repo, DumpRepositoryEvent.SUCCESS, ADMIN_USER_ID)
        
        mailListenerService.onApplicationEvent(event)
        
        assertEquals("Mail should not be sent for successful backup", 0, 
                greenMail.getReceivedMessages().length)
    }

    public void testBackupFail() {
        assertEquals("Expected 0 mail messages to start", 0, 
                greenMail.getReceivedMessages().length)
        
        DumpBean params = new DumpBean(backup: true)
        DumpRepositoryEvent event = new DumpRepositoryEvent(
                this, params, repo, DumpRepositoryEvent.FAILED, ADMIN_USER_ID, 
                null, null, new Exception("testBackupFail"))
        
        mailListenerService.onApplicationEvent(event)

        assertEquals("Expected one mail message", 1, 
                greenMail.getReceivedMessages().length)
        def message = greenMail.getReceivedMessages()[0]
        
        assertEquals("Message Subject did not match",
                "[Error][Backup]Repository: test-repo", message.subject)
        assertTrue("Message Body did not match ", GreenMailUtil.getBody(message)
                .startsWith("The dump backup of repository '" + repo.name +
                "' failed."))
        assertEquals("Message 'From' did not match", "SubversionEdge@localhost",
                GreenMailUtil.getAddressList(message.from))
        assertEquals("Message 'To' did not match", Server.getServer().adminEmail, 
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.TO)))
        assertNull("Message should not have 'CC'",
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.CC)))
    }

    public void testLoadSuccess() {
        LoadRepositoryEvent event = new LoadRepositoryEvent(
                this, repo, LoadRepositoryEvent.SUCCESS, ADMIN_USER_ID)
        
        mailListenerService.onApplicationEvent(event)
        
        assertEquals("Expected one mail message", 1,
                greenMail.getReceivedMessages().length)
        def message = greenMail.getReceivedMessages()[0]
        assertEquals("Message Subject did not match",
                "[Success][Load]Repository: test-repo", message.subject)
        assertTrue("Message Body did not match", GreenMailUtil.getBody(message)
                .startsWith("The loading of the dump file or archive into repository '" + 
                repo.name + "' completed."))
        assertEquals("Message From did not match", "SubversionEdge@localhost",
                GreenMailUtil.getAddressList(message.from))
        assertEquals("Message To did not match", User.get(ADMIN_USER_ID).email,
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.TO)))
        assertNull("Message not expected to have CC recipients",
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.CC)))
    }

    public void testLoadFail() {
        assertEquals("Expected 0 mail messages to start", 0,
                greenMail.getReceivedMessages().length)
        
        LoadRepositoryEvent event = new LoadRepositoryEvent(
                this, repo, DumpRepositoryEvent.FAILED, ADMIN_USER_ID,
                null, null, new Exception("testLoadFail"))
        
        mailListenerService.onApplicationEvent(event)

        // Two identical messages (as email is sent to two users)
        assertEquals("Expected two mail message", 2,
                greenMail.getReceivedMessages().length)
        def message = greenMail.getReceivedMessages()[0]
        def dupeMessage = greenMail.getReceivedMessages()[1]
        
        assertEquals("Message Subject did not match",
                "[Error][Load]Repository: test-repo", message.subject)
        assertEquals("Duplicate message subject did not match original",
                message.subject, dupeMessage.subject)
        
        assertTrue("Message Body did not match ", GreenMailUtil.getBody(message)
                .startsWith("The loading of the dump file or archive into repository '" + 
                repo.name + "' failed."))
        assertEquals("Duplicate message body did not match original",
                GreenMailUtil.getBody(message),
                GreenMailUtil.getBody(dupeMessage))
        assertEquals("Message 'From' did not match", "SubversionEdge@localhost",
                GreenMailUtil.getAddressList(message.from))
        assertEquals("Duplicate message 'From' did not match original",
            GreenMailUtil.getAddressList(message.from),
            GreenMailUtil.getAddressList(dupeMessage.from))
        assertEquals("Message 'To' did not match", User.get(1).email,
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.TO)))
        assertEquals("Duplicate message 'To' did not match original",
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.TO)),
                GreenMailUtil.getAddressList(
                dupeMessage.getRecipients(Message.RecipientType.TO)))
        assertEquals("Message 'CC' did not match", Server.getServer().adminEmail,
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.CC)))
        assertEquals("Duplicate message 'CC' did not match original",
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.CC)),
                GreenMailUtil.getAddressList(
                dupeMessage.getRecipients(Message.RecipientType.CC)))
    }

    public void testLoadFailWithFile() {
        assertEquals("Expected 0 mail messages to start", 0,
                greenMail.getReceivedMessages().length)
        
        String testContent = 'qwerty foo bar baz '
        while (testContent.length() < 200000) {
            testContent += testContent
        }
        testContent = testContent.trim()
        
        File testFile = File.createTempFile("load-progress-test-file", ".txt")
        testFile.text = testContent
        
        LoadRepositoryEvent event = new LoadRepositoryEvent(
                this, repo, DumpRepositoryEvent.FAILED, ADMIN_USER_ID,
                null, testFile, new Exception("testLoadFailWithFile"))
        
        mailListenerService.onApplicationEvent(event)

        // Two identical messages (as email is sent to two users)
        assertEquals("Expected two mail message", 2,
                greenMail.getReceivedMessages().length)
        def message = greenMail.getReceivedMessages()[0]

        testFile.delete()
        
        assertTrue("Message Body did not contain attachment text ", 
                GreenMailUtil.getBody(message)
                .contains("Load process output is attached."))
        assertTrue("Message Body did not contain file message ",
            GreenMailUtil.getBody(message)
            .contains("Full output is available in the server logs temp " +
                      "directory with name " + testFile.name))

        assertTrue(message.content instanceof MimeMultipart)
        MimeMultipart mp = message.content
        assertEquals("Expected an attachment", 2, mp.count)
        assertEquals("Unexpected attachment content", 
                testContent[-104858..-1], mp.getBodyPart(1).content)
                
        // Tested elsewhere, but might as well confirm it here too
        assertEquals("Message Subject did not match",
                "[Error][Load]Repository: test-repo", message.subject)        
        assertEquals("Message 'From' did not match", "SubversionEdge@localhost",
                GreenMailUtil.getAddressList(message.from))
        assertEquals("Message 'To' did not match", User.get(1).email,
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.TO)))
        assertEquals("Message 'CC' did not match", Server.getServer().adminEmail,
                GreenMailUtil.getAddressList(
                message.getRecipients(Message.RecipientType.CC)))
    }
}
