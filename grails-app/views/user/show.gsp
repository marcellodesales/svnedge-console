<head>
    <meta name="layout" content="main" />
    <title>CollabNet Subversion Edge <g:message code="user.page.show.title"/></title>
</head>

<content tag="title">
    <g:message code="user.page.header"/>
</content>

<g:render template="leftNav" />

<body>

    <div class="body">
        <h1><g:message code="user.page.show.details.header"/></h1>
        <div class="dialog">
            <table>
            <tbody>

                <tr class="prop">
                    <td valign="top" class="name"><g:message code="user.username.label"/></td>
                    <td valign="top" class="value">${userInstance.username}</td>
                </tr>

                <%-- only showing fullname and email for editable (local db) users --%>
                <g:if test="${editable}">
                <tr class="prop">
                    <td valign="top" class="name"><g:message code="user.realUserName.label"/></td>
                    <td valign="top" class="value">${userInstance.realUserName}</td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name"><g:message code="user.email.label"/></td>
                    <td valign="top" class="value">${userInstance.email}</td>
                </tr>
                </g:if>

                <tr class="prop">
                    <td valign="top" class="name"><g:message code="user.description.label"/></td>
                    <td valign="top" class="value">${userInstance.description}</td>
                </tr>

                <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_USERS">


                  <tr class="prop">
                      <td valign="top" class="name"><g:message code="user.authorities.label" /></td>

                      <td valign="top" class="value">
                          <ul>
                          <g:each in="${userInstance.authorities}" var="r">
                              <li><g:link controller="role" action="show" id="${r.id}">${r.authority}</g:link> (${r.description})</li>
                          </g:each>
                          </ul>
                      </td>
                   </tr>
                 </g:ifAnyGranted>

            </tbody>
            </table>
        </div>

      <div class="form-actions">
        <g:form>
          <input type="hidden" name="id" value="${userInstance.id}" />
          <g:if test="${editable}">
            <g:actionSubmit action="edit" class="btn edit" value="${message(code:'user.page.edit.button.edit')}" />
          </g:if> 
            <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_USERS">
              <g:actionSubmit action="btn delete" class="delete" id="deleteButton" value="${message(code:'user.page.edit.button.delete')}"
                              data-toggle="modal" data-target="#confirmDelete" />
            </g:ifAnyGranted>
        </g:form>
      </div>

      <div id="confirmDelete" class="modal hide fade" style="display: none">
        <div class="modal-header">
          <a class="close" data-dismiss="modal">&times;</a>
          <h3>${message(code: 'default.confirmation.title')}</h3>
        </div>
        <div class="modal-body">
          <p>${message(code:'user.page.edit.button.delete.confirm')}</p>
        </div>
        <div class="modal-footer">
          <a href="#" class="btn btn-primary ok" onclick="formSubmit($('#deleteButton').closest('form'), '/csvn/user/delete')">${message(code: 'default.confirmation.ok')}</a>
          <a href="#" class="btn cancel" data-dismiss="modal">${message(code: 'default.confirmation.cancel')}</a>
        </div>
      </div>

    </div>
</body>
