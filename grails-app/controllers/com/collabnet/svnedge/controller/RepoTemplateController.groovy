package com.collabnet.svnedge.controller

import org.codehaus.groovy.grails.plugins.springsecurity.Secured
import com.collabnet.svnedge.domain.RepoTemplate

class RepoTemplateController {
    def repoTemplateService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_ADMIN_REPO'])
    def list = {
        params.sort = 'displayOrder'
        [repoTemplateInstanceList: RepoTemplate.list(params), repoTemplateInstanceTotal: RepoTemplate.count()]
    }

    @Secured(['ROLE_ADMIN', 'ROLE_ADMIN_REPO'])
    def create = {
        def repoTemplateInstance = new RepoTemplate()
        repoTemplateInstance.properties = params
        return [repoTemplateInstance: repoTemplateInstance]
    }

    @Secured(['ROLE_ADMIN', 'ROLE_ADMIN_REPO'])
    def save = {
        if (params.cancelButton) {
            redirect(action: list)
            return
        }

        def repoTemplateInstance = new RepoTemplate(params)
        if (repoTemplateInstance.hasErrors()) {
            render(view: "create", model: [repoTemplateInstance: repoTemplateInstance])

        } else {
        
            handleFileUpload(repoTemplateInstance)
            if (repoTemplateInstance.save(flush: true)) {
                flash.message = "${message(code: 'repoTemplate.action.created.message', args: [repoTemplateInstance.name])}"
                redirect(action: "list")
            }
            else {
                render(view: "create", model: [repoTemplateInstance: repoTemplateInstance])
            }
        }
    }

    private boolean handleFileUpload(repoTemplateInstance) {
        def uploadedFile = request.getFile('templateUpload')
        if (uploadedFile.empty) {
            flash.error = message(code: 'repoTemplate.action.save.no.file')
            redirect(action: create)
        } else {
            File templateDir = repoTemplateService.getUploadDirectory()
            File templateFile = new File(templateDir, uploadedFile.originalFilename)
            uploadedFile.transferTo(templateFile)
            return repoTemplateService.saveTemplate(repoTemplateInstance, 
                templateFile, params.displayFirst ? true : false)
        }
        return false
    }

    @Secured(['ROLE_ADMIN', 'ROLE_ADMIN_REPO'])
    def edit = {
        def repoTemplateInstance = RepoTemplate.get(params.id)
        if (!repoTemplateInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'repoTemplate.label', default: 'RepoTemplate'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [repoTemplateInstance: repoTemplateInstance]
        }
    }

    @Secured(['ROLE_ADMIN', 'ROLE_ADMIN_REPO'])
    def update = {
        def repoTemplateInstance = RepoTemplate.get(params.id)
        if (repoTemplateInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (repoTemplateInstance.version > version) {
                    
                    repoTemplateInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'repoTemplate.label', default: 'RepoTemplate')] as Object[], "Another user has updated this RepoTemplate while you were editing")
                    render(view: "edit", model: [repoTemplateInstance: repoTemplateInstance])
                    return
                }
            }
            repoTemplateInstance.properties = params
            if (!repoTemplateInstance.hasErrors() && repoTemplateInstance.save(flush: true)) {
                flash.message = "${message(code: 'repoTemplate.action.updated.message')}"
                redirect(action: "list", id: repoTemplateInstance.id)
            }
            else {
                render(view: "edit", model: [repoTemplateInstance: repoTemplateInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'repoTemplate.label', default: 'RepoTemplate'), params.id])}"
            redirect(action: "list")
        }
    }
    
    @Secured(['ROLE_ADMIN', 'ROLE_ADMIN_REPO'])
    def updateListOrder = {
        log.debug("Updating repository template list order: " + 
            params['templates[]'])
        def order = params['templates[]']
        //for (int i = 0; i < order.length; i++)
        int i = 1
        for (String id : order) {
            RepoTemplate t = RepoTemplate.get(id)
            t.displayOrder = i++
            t.save()
        }
    }

    @Secured(['ROLE_ADMIN', 'ROLE_ADMIN_REPO'])
    def delete = {
        def repoTemplateInstance = RepoTemplate.get(params.id)
        if (repoTemplateInstance) {
            try {
                repoTemplateInstance.delete(flush: true)
                flash.message = "${message(code: 'repoTemplate.action.deleted.message')}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'repoTemplate.label', default: 'RepoTemplate'), params.id])}"
                redirect(action: "edit", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'repoTemplate.label', default: 'RepoTemplate'), params.id])}"
            redirect(action: "list")
        }
    }
}
