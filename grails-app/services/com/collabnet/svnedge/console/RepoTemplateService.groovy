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

import java.util.zip.ZipFile;

import com.collabnet.svnedge.util.ConfigUtil;
import com.collabnet.svnedge.domain.RepoTemplate

class RepoTemplateService extends AbstractSvnEdgeService {

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
            def zipFile = new ZipFile(templateFile)
            template.dumpFile = (zipFile.size() == 1)
    
        } else {
            template.dumpFile = true
        }
        
        def currentTemplates = RepoTemplate.list()
        if (isInsert) {
            template.displayOrder = 1
            int i = 2
            for (RepoTemplate t in currentTemplates) {
                t.displayOrder = i++
                t.save()
            }
        } else {
            template.displayOrder = currentTemplates.size() + 1
        }
        
        // give a temp name to allow saving the record
        template.location = "temp/" + templateFile.name
        template.active = true
        boolean success = template.save()
        if (success) {
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
            success = template.save()
        }
        return success
    }
        
    /**
     * Retrieves a list of all active templates sorted by name. 
     * @return List of RepoTemplate
     */
    def retrieveActiveTemplates() {
        def templates = RepoTemplate.findAllByActive(true, [sort: 'displayOrder'])
        return templates
    }

    /**
     * Updates the templates to match the order given by the list of IDs.
     * Templates not referenced will be put at the end.
     * 
     * @param templateIdList List of String IDs
     */
    void reorderTemplates(def templateIdList) {
        def currentOrder = RepoTemplate.list(sort: 'displayOrder')
        int i = 1
        for (String id : templateIdList) {
            RepoTemplate t = RepoTemplate.get(id)
            t.displayOrder = i++
            t.save()
            currentOrder.remove(t)
        }
        for (RepoTemplate t : currentOrder) {
            t.displayOrder = i++
            t.save()
        }
    }
    
    /**
     * Deletes the template from the database and filesystem
     * @param templateId DB PK
     * @return false, if no template is associated with the given templateId
     */
    boolean deleteTemplate(String templateId) {        
        def template = RepoTemplate.get(templateId)
        if (template) {
            File f = new File(getTemplateDirectory(), template.location)
            if (!f.delete()) {
                log.warn("Template '" + template.name + "'is being deleted," +
                        " but the associated file " + f.canonicalPath +
                        " could not be deleted.")
            }
            template.delete(flush: true)
        } else {
            return false
        }
        return true
    }
}
