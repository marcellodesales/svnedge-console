/*
 * CollabNet Subversion Edge
 * Copyright (C) 2011, CollabNet Inc. All rights reserved.
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
package com.collabnet.svnedge.admin

import com.collabnet.svnedge.console.AbstractSvnEdgeService
import com.collabnet.svnedge.domain.MailConfiguration
import com.collabnet.svnedge.domain.MailAuthMethod
import com.collabnet.svnedge.domain.MailSecurityMethod

import org.springframework.beans.factory.InitializingBean
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Business logic related to setup of a mail server and sending notifications
 * of important information related to asynchronous operations
 */
class MailNotificationService extends AbstractSvnEdgeService 
        implements InitializingBean, ApplicationContextAware {

    ApplicationContext applicationContext
    def securityService

    /**
     * @see InitializingBean#afterPropertiesSet
     * initializing bean -- after injection, we need to update the configuration
     */
    void afterPropertiesSet() {
        updateConfig(MailConfiguration.getConfiguration())
    }
    
    boolean saveMailConfiguration(MailConfiguration config) {
        boolean b = false
        config.validate()
        if (!config.hasErrors() && config.save()) {
            updateConfig(config)
            b = true
        } else {
            config.discard()
        }
        return b
    }
    
    void updateConfig(MailConfiguration dynamicConfig) {
        ConfigObject config = ConfigurationHolder.config.grails.mail
        log.debug "ConfigObject in updateConfig: " + config
        config['host'] = dynamicConfig.serverName
        config['port'] = dynamicConfig.port
        config['username'] = dynamicConfig.authUsername
        config['password'] = dynamicConfig.authPassword ?
                securityService.decrypt(dynamicConfig.authPassword) : ''
        config['disabled'] = !dynamicConfig.enabled
        def props = [:]
        switch (dynamicConfig.securityMethod) {
            case MailSecurityMethod.NONE:
            break
            
            case MailSecurityMethod.SSL:
            props["mail.smtp.socketFactory.port"] = dynamicConfig.port as String
            props["mail.smtp.socketFactory.class"] =
                    "javax.net.ssl.SSLSocketFactory"
            props["mail.smtp.socketFactory.fallback"] = "false"
            break
            
            case MailSecurityMethod.STARTTLS:
            props["mail.smtp.starttls.enable"] = "true"
            props["mail.smtp.port"] = dynamicConfig.port as String
        }
        if (dynamicConfig.authMethod != MailAuthMethod.NONE) {
            props["mail.smtp.auth"] = "true"
        }
        if (dynamicConfig.authMethod != MailAuthMethod.PLAINTEXT) {
            props["mail.smtp.auth.plain.disable"] = "true"
        }
        if (dynamicConfig.authMethod == MailAuthMethod.KERBEROS) {
            props["mail.smtp.sasl.enable"] = "true"
        }

        config['props'] = props
        
        configureMailSession(config)
    }
    
    private void configureMailSession(ConfigObject config) {

        def ms = applicationContext.getBean('mailSender')
        ms.host = config.host ?: "localhost"
        ms.defaultEncoding = config.encoding ?: "utf-8"
        if (config.port)
            ms.port = config.port
        if (config.username)
            ms.username = config.username
        if (config.password)
            ms.password = config.password
        if (config.protocol)
            ms.protocol = config.protocol
        if (config.props instanceof Map && config.props)
            ms.javaMailProperties = config.props
    }
}
