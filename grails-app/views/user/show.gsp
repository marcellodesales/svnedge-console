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
                    <td valign="top" class="value">${userInstance.username?.encodeAsHTML()}</td>
                </tr>

                <%-- only showing fullname and email for editable (local db) users --%>
                <g:if test="${editable}">
                <tr class="prop">
                    <td valign="top" class="name"><g:message code="user.realUserName.label"/></td>
                    <td valign="top" class="value">${userInstance.realUserName?.encodeAsHTML()}</td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name"><g:message code="user.email.label"/></td>
                    <td valign="top" class="value">${userInstance.email?.encodeAsHTML()}</td>
                </tr>
                </g:if>

                <tr class="prop">
                    <td valign="top" class="name"><g:message code="user.description.label"/></td>
                    <td valign="top" class="value">${userInstance.description?.encodeAsHTML()}</td>
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

        <div class="buttons">
            <g:form>
                <input type="hidden" name="id" value="${userInstance.id}" />
                <g:if test="${editable}">
                  <span class="button"><g:actionSubmit action="edit" class="edit" value="${message(code:'user.page.edit.button.edit')}" /></span>
                  <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_USERS">
                    <g:set var="question" value="${message(code:'user.page.edit.button.delete.confirm')}" />
                    <span class="button"><g:actionSubmit action="delete" class="delete" onclick="return confirm('${question}');" value="${message(code:'user.page.edit.button.delete')}" /></span>
                  </g:ifAnyGranted>
                </g:if> 
            </g:form>
        </div>

    </div>
</body>
