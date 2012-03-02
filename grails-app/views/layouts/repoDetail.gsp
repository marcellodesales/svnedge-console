<html>
    <head>
        <meta name="layout" content="main" />
        <g:pageProperty name="page.headSnippet" />
    </head>


<content tag="title"><g:message code="repository.page.show.title" args="${[repositoryInstance.name]}"/></content>

    <body>

<g:if test="${!repositoryInstance.permissionsOk}">
<div class="alert alert-block alert-info">
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

    <div class="row-fluid">
      <div class="span12">
        <div class="row-fluid">
          <div class="span10 well">
            
            <div class="row-fluid">
              <div class="span1">
                <strong><g:message code="repository.page.show.status" /></strong>
              </div>
              <div class="span3">
                <g:if test="${repositoryInstance.permissionsOk}">
                  <span style="color:green"><g:message code="repository.page.list.instance.permission.ok" /></span>
                </g:if>
                <g:else>
                  <span style="color:red"><g:message code="repository.page.list.instance.permission.needFix" /></span>
                </g:else>
              </div>
              <div class="span2">
                <strong><g:message code="repository.page.show.fsformat" /></strong>
              </div>
              <div class="span4"><g:message code="repository.page.show.fsformat.value" args="${[fsType, fsFormat]}"/></div>
            </div>

            <div class="row-fluid">
              <div class="span1">
                <strong><g:message code="repository.page.show.revision" /></strong>
              </div>
              <div class="span3">${headRev}</div>
              <div class="span2">
                <strong><g:message code="repository.page.show.repoformat" /></strong>
              </div>
              <div class="span4">${repoFormat}</div>
            </div>
            
            <div class="row-fluid">
              <div class="span1">
                <strong><g:message code="repository.page.show.uuid" /></strong>
              </div>
              <div class="span3">${repoUUID}</div>
              <div class="span2">
                <strong><g:message code="repository.page.show.supports" /></strong>
              </div>
              <div class="span4">${repoSupport}</div>
            </div>
           
            <div class="row-fluid">
              <div class="span1">
                <strong><g:message code="repository.page.show.size" /></strong>
              </div>
              <div class="span3">
                <g:if test="${diskUsage}">
                  <g:formatFileSize size="${diskUsage}"/>
                </g:if>
                <g:else>
                  <g:message code="status.page.status.noData"/>
                </g:else>
              </div>
              <div class="span2">
                <strong><g:message code="repository.page.show.sharding" /></strong>
              </div>
              <div class="span4">
                <g:if test="${sharded >= 0}">
                  <g:message code="repository.page.show.sharding.enabled" args="${[sharded]}"/>
                </g:if>
                <g:else>
                  <g:message code="repository.page.show.sharding.disabled"/>
                </g:else>
              </div>
            </div>

            <div class="row-fluid">
              <div class="span1">
                <strong><g:message code="repository.page.show.packed" /></strong>
              </div>
              <div class="span3">
                <g:if test="${minPackedRev > 0}">
                  <g:message code="default.boolean.true" />
                </g:if>
                <g:else>
                  <g:message code="default.boolean.false" />
                </g:else>
              </div>
              <div class="span2">
                <strong><g:message code="repository.page.show.repshare" /></strong>
              </div>
              <div class="span4">
                <g:if test="${repSharing}">
                  <g:message code="default.boolean.true" />
                </g:if>
                <g:else>
                  <g:message code="default.boolean.false" />
                </g:else>
              </div>
            </div>
            
          </div>
        </div>

        <div class="buttons">
          <g:form>
            <input type="hidden" name="id" value="${repositoryInstance?.id}"/>
            <g:if test="${!repositoryInstance.permissionsOk}">
              <span class="button"><g:actionSubmit class="updatePermissions"
                                                   value="${message(code:'repository.page.show.button.validate') }"
                                                   action="updatePermissions"/></span>
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
  