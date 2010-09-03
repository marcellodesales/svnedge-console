package com.collabnet.svnedge.console.ui;

import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.Properties;

import grails.test.ControllerUnitTestCase;
import groovy.lang.MetaClass;

public class SvnEdgeAbstractControllerTests extends ControllerUnitTestCase {

    def props

    protected void setUp() {
        super.setUp()

        props = new Properties()
        def stream = new FileInputStream("grails-app/i18n/messages.properties")
        props.load stream
        stream.close()

        mockI18N(controller)
    }

    def mockI18N = { controller ->
        controller.metaClass.message = { Map map ->
        if (!map.code)
            return ""
        if (map.args) {
            def formatter = new MessageFormat("")
            formatter.applyPattern props.getProperty(map.code)
            return formatter.format(map.args.toArray())
        } else {
            return props.getProperty(map.code)
            }
        }
    }
}
