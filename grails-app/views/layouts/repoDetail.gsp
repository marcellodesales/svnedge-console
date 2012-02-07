<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title><g:message code="repository.page.show.title" /></title>
        <g:pageProperty name="page.headSnippet" />
    </head>

<g:render template="leftNav" />

<content tag="title">
     <g:message code="repository.page.leftnav.title" />
</content>

    <body>

<g:if test="${!repositoryInstance.permissionsOk}">
<div class="instructionText">
    <i><g:message code="repository.page.show.filePermissionInfo" /></i>
    <p>
       <g:message code="repository.page.show.permission.p1" args="${['CollabNet Subversion Edge']}" />
    </p>
    <p>
      <g:message code="repository.page.show.permission.p2" />
    </p>
    <code>sudo chown -R ${svnUser}:${svnGroup} ${repoPath}</code>
    <p>
      <g:message code="repository.page.show.permission.p3" />
    </p>
 </div>
</g:if>


        <table class="Container">
    <thead>
    <tr class="ContainerHeader">
      <td colspan="5"><g:message code="repository.page.show.header" /></td>    
    </tr>
    </thead>
                    <tbody>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="repository.page.show.name" /></td>
                            <td valign="top" class="value">${fieldValue(bean:repositoryInstance, field:'name')}</td>
                            <td>&nbsp;</td>
                            <td>&nbsp;</td>
                            <td>&nbsp;</td>
                         </tr>
 
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="repository.page.show.status" /></td>
                            <td valign="top" class="value">
                              <g:if test="${repositoryInstance.permissionsOk}">
                                <span style="color:green"><g:message code="repository.page.list.instance.permission.ok" /></span>
                              </g:if>
                              <g:else>
                                <span style="color:red"><g:message code="repository.page.list.instance.permission.needFix" /></span>
                              </g:else>
                             </td>

                            <td>&nbsp;</td>

                            <td valign="top" class="name"><g:message code="repository.page.show.fsformat" /></td>
                            <td valign="top" class="value" nowrap="nowrap">
                                <g:message code="repository.page.show.fsformat.value" args="${[fsType, fsFormat]}"/>
                            </td>
                        </tr>
 
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="repository.page.show.revision" /></td>
                            <td valign="top" class="value">${headRev}</td>

                            <td>&nbsp;</td>

                            <td valign="top" class="name"><g:message code="repository.page.show.repoformat" /></td>
                            <td valign="top" class="value">${repoFormat}</td>
                        </tr>
 
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="repository.page.show.uuid" /></td>
                            <td valign="top" class="value" nowrap="nowrap">${repoUUID}</td>

                            <td>&nbsp;</td>

                            <td valign="top" class="name"><g:message code="repository.page.show.supports" /></td>
                            <td valign="top" class="value" nowrap="nowrap">${repoSupport}</td>
                        </tr>
 
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="repository.page.show.size" /></td>
                            <td valign="top" class="value">
                               <g:if test="${diskUsage}">
                                  <g:formatFileSize size="${diskUsage}"/>
                               </g:if>
                               <g:else>
                                  <g:message code="status.page.status.noData"/>
                               </g:else>
                            </td>

                            <td>&nbsp;</td>

                            <td valign="top" class="name"><g:message code="repository.page.show.sharding" /></td>
                            <td valign="top" class="value">
                              <g:if test="${sharded >= 0}">
                                <g:message code="repository.page.show.sharding.enabled" args="${[sharded]}"/>
                              </g:if>
                              <g:else>
                                <g:message code="repository.page.show.sharding.disabled"/>
                              </g:else>
                             </td>
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="repository.page.show.packed" /></td>
                            <td valign="top" class="value">
                              <g:if test="${minPackedRev > 0}">
                                <g:message code="default.boolean.true" />
                              </g:if>
                              <g:else>
                                <g:message code="default.boolean.false" />
                              </g:else>
                             </td> 

                            <td>&nbsp;</td>

                            <td valign="top" class="name"><g:message code="repository.page.show.repshare" /></td>
                            <td valign="top" class="value">
                              <g:if test="${repSharing}">
                                <g:message code="default.boolean.true" />
                              </g:if>
                              <g:else>
                                <g:message code="default.boolean.false" />
                              </g:else>
                             </td>
                         </tr>

                    </tbody>
                </table>

            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${repositoryInstance?.id}" />
                    <g:if test="${!repositoryInstance.permissionsOk}">
                    <span class="button"><g:actionSubmit class="updatePermissions" value="${message(code:'repository.page.show.button.validate') }" action="updatePermissions"/></span>
                    </g:if>
                    <%--
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                    --%>
                </g:form>
            </div>

  <g:if test="${!params.suppressTabs}">
  <g:set var="tabArray" value="${[[action:'dumpFileList', href:createLink(action: 'dumpFileList', id: params.id), label: message(code:'repository.page.show.tabs.dumpFileList')]]}" />
  <g:set var="tabArray" value="${tabArray << [action:'bkupSchedule', href:createLink(action: 'bkupSchedule', id: params.id), label: message(code:'repository.page.show.tabs.bkupSchedule')]}" />
  <g:set var="tabArray" value="${tabArray << [action:'reports', href:createLink(action: 'reports', id: params.id), label: message(code:'repository.page.show.tabs.reports')]}" />
  <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_HOOKS">
    <g:set var="tabArray" value="${tabArray << [action:'hooksList', href:createLink(action: 'hooksList', id: params.id), label: message(code:'repository.page.show.tabs.hooksList')]}" />
  </g:ifAnyGranted>
  <g:render template="/common/tabs" model="${[tabs: tabArray]}" />
  </g:if>
  
  <g:pageProperty name="page.tabContent" />
  
    </body>
</html>
  