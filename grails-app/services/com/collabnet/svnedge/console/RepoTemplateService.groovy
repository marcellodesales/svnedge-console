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
package com.collabnet.svnedge.console

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.hyperic.sigar.FileInfo

import com.collabnet.svnedge.util.ConfigUtil;
import com.collabnet.svnedge.domain.RepoTemplate

class RepoTemplateService extends AbstractSvnEdgeService {

    // service dependencies
    def operatingSystemService
    def lifecycleService
    def commandLineService

    File getTemplateDirectory() {
        File templateDir = new File(ConfigUtil.dataDirPath(), 'repoTemplates')
        if (!templateDir.exists()) {
            templateDir.mkdirs()
            new File(templateDir, 'temp').mkdir()
            //new File(templateDir, 'dumpfile').mkdir()
            //new File(templateDir, 'repoArchive').mkdir()
        }
        return templateDir
    }

    File getUploadDirectory() {
        return new File(getTemplateDirectory(), 'temp')
    }
    
    boolean saveTemplate(RepoTemplate template, File templateFile, 
        boolean isInsert) {

        boolean isArchive = templateFile.name.endsWith(".zip")
        if (isArchive) {
            def zipFile = new java.util.zip.ZipFile(templateFile)
            template.dumpFile = (zipFile.size() == 1)
    
        } else {
            template.dumpFile = true
        }
        
        def currentTemplates = RepoTemplate.list()
        if (isInsert) {
            template.displayOrder = 1
            for (RepoTemplate t in currentTemplates) {
                t.displayOrder++
                t.save()
            }
        } else {
            template.displayOrder = currentTemplates.size() + 1
        }
        
        // give a temp name to allow saving the record
        template.location = "temp/" + templateFile.name
        template.active = true
        template.save()
        
        String filename = (template.dumpFile ? "dump" : "repoArchive") +
            template.id
        if (isArchive) {
            filename += ".zip"
        }
        File finalFile = new File(getTemplateDirectory(), filename)
        if (finalFile.exists()) {
            finalFile.delete()
        }
        if (!templateFile.renameTo(finalFile)) {
            finalFile.withOutputStream { out -> 
                templateFile.withInputStream { instrm -> out << instrm } }
            templateFile.delete()
        }
        template.location = finalFile.name
        template.save()
        return true
    }
        
    /**
     * Retrieves a list of all active templates sorted by name. 
     * @return List of RepoTemplate
     */
    def retrieveActiveTemplates() {
        def templates = RepoTemplate.findAllByActive(true, [sort: 'displayOrder'])
        return templates
    }
}
