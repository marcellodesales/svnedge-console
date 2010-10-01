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
import java.util.Properties

includeTargets << grailsScript("_GrailsWar")
includeTargets << new File("scripts", "_CommonTargets.groovy")

/**
 * This script diffs ONLY THE KEYS of a chosen i18n properties with the English
 * one and creates a new file "diffI18n_LANGCODE.diffprops.
 * 
 * Run "grails diffI18n" and choose a language from the menu.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 */

target(build: 'Builds the distribution file structure') {
    setDefaultTarget("compile")

    distDir = "${basedir}/grails-app/i18n/"
    def en = "messages.properties"
    println ""
    println "####### CollabNet Subversion Edge Language Keys Diff Util ########"
    println "# Available Languages at '${distDir}'"

    def langIndex = [:]
    def localeIndex = [:]
    def counter = 0
    def langs = new File(distDir).listFiles()
    for (langFile in langs) {
        def fileName = langFile.canonicalPath
        if (fileName.contains(en) || (!fileName.startsWith("messages") &&
            !fileName.endsWith(".properties"))) {
            continue
        }

        int sep = fileName.lastIndexOf(File.separator);
        fileName = fileName.substring(sep+1, fileName.length())

        def langCode = fileName.replace("messages_", "").replace(".properties",
            "")
        def locale
        counter++
        if (langCode.contains("_")) {
            def langCountry = langCode.split("_")
            locale = new Locale(langCountry[0], langCountry[1])
            localeIndex[counter] = locale
        } else {
            locale = new Locale(langCode)
            localeIndex[counter] = locale
        }
        println "# " + counter + ") " + locale.getDisplayName() + " (${locale})"
        langIndex[counter] = langFile
    }

    def stdin = new BufferedReader(new InputStreamReader(System.in))
    print "# Choose one to diff with the English version: "
    String chosenLang = stdin.readLine()
    println "# You selected " + chosenLang + ". Diffing that language with " + 
        "English"
    println ""

    def propsEn = new Properties()
    new File(distDir + en).withReader { r ->
        propsEn.load(r)
    }

    def propsDiff = new Properties()
    def selectedLangFile = langIndex[new Integer(chosenLang)]
    selectedLangFile.withReader { r ->
        propsDiff.load(r)
    }

    def diffProps = []
    def esNames = propsDiff.propertyNames().toList() //Enumeration
    def enNames = propsEn.propertyNames().toList()
    for(prop in enNames) {
        if (!propsDiff.getProperty(prop)) {
            diffProps << prop
        }
    }

    def chosenLocale = localeIndex[new Integer(chosenLang)]
    def langName = chosenLocale.getDisplayName()
    if (diffProps.size() == 0) {
        println "# The language '${langName}' is up-to-date!"
        return
    }

    def code = localeIndex[new Integer(chosenLang)]
    def reprt = new File(distDir + "diffI18n_${code}.diffprops")
    reprt.withWriter('UTF-8') {
        int i = 0
        for (l in diffProps.sort()) {
            def entry = l + "=" + propsEn.getProperty(l)
            //println "# " + entry
            if (i == 0) {
                i++
                previousKey = l.split("=")[0].substring(0, l.indexOf("."))
                it.writeLine entry
            } else {
                if (l.contains(previousKey)) {
                    it.writeLine entry
                } else {
                    previousKey = l.split("=")[0].substring(0, l.indexOf("."))
                    it.writeLine ""
                    it.writeLine entry
                }
            }
        }
    }
    println "# The language '${langName}' needs more ${diffProps.size()} " +
        "string" + (diffProps.size() == 1 ? '' : 's')
    println "# See the diff at " + reprt.canonicalPath
    println "# Remember to edit your language '${langName}' using an editor " +
        "in the format UTF-8."
}
setDefaultTarget("build")