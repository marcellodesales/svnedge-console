<head>
    <meta name="layout" content="main" />
    <title><g:message code="user.page.create.title"/></title>
</head>


<content tag="title">
  <g:message code="user.page.header"/>
</content>

<g:render template="leftNav" />

<body>
    <table class="Container"> 
        <tr class="ContainerHeader">
            <td colspan="2"><g:message code="user.page.create.title"/></td>
        </tr>  
        <g:form action="save">
                    <tr class="prop">
                        <td valign="top" class="name"><label for="username"><g:message code="user.username.label"/>:</label></td>
                        <td width="100%" valign="top" class="value errors">
                            <input type="text" id="username" name="username" value="${userInstance.username?.encodeAsHTML()}"/>
                          <g:hasErrors bean="${userInstance}" field="username">
                              <ul><g:eachError bean="${userInstance}" field="username">
                                  <li><g:message error="${it}"/></li>
                              </g:eachError></ul>
                           </g:hasErrors>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td valign="top" class="name"><label for="realUserName"><g:message code="user.realUserName.label"/>:</label></td>
                        <td valign="top" class="value errors">
                            <input type="text" id="realUserName" name="realUserName" value="${userInstance.realUserName?.encodeAsHTML()}"/>
                          <g:hasErrors bean="${userInstance}" field="realUserName">
                              <ul><g:eachError bean="${userInstance}" field="realUserName">
                                  <li><g:message error="${it}"/></li>
                              </g:eachError></ul>
                           </g:hasErrors>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td valign="top" class="name"><label for="passwd"><g:message code="user.passwd.label"/>:</label></td>
                        <td valign="top" class="value ${hasErrors(bean:userInstance,field:'passwd','errors')}">
                            <input type="password" id="passwd" name="passwd" value="${userInstance.passwd?.encodeAsHTML()}"/>
                          <g:hasErrors bean="${userInstance}" field="passwd">
                              <ul><g:eachError bean="${userInstance}" field="passwd">
                                  <li><g:message error="${it}"/></li>
                              </g:eachError></ul>
                           </g:hasErrors>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td valign="top" class="name"><label for="description"><g:message code="user.description.label"/>:</label></td>
                        <td valign="top" class="value ${hasErrors(bean:userInstance,field:'description','errors')}">
                            <input type="text" id="description" name="description" value="${userInstance.description?.encodeAsHTML()}"/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td valign="top" class="name"><label for="email"><g:message code="user.email.label"/>:</label></td>
                        <td valign="top" class="value ${hasErrors(bean:userInstance,field:'email','errors')}">
                            <input type="text" id="email" name="email" value="${userInstance.email?.encodeAsHTML()}"/>
                            <g:hasErrors bean="${userInstance}" field="email">
                              <ul><g:eachError bean="${userInstance}" field="email">
                                  <li><g:message error="${it}"/></li>
                              </g:eachError></ul>
                           </g:hasErrors>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td valign="top" class="name" style="white-space: nowrap;">
                          <label for="authorities"><g:message code="user.authorities.label" />:</label>
                        </td>
                        <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'authorities', 'errors')}">
                          <g:each in="${roleList}" var="role">
                            <g:checkBox id="authority_${role.id}" name="authorities" value="${role.id}"
                            checked="${userInstance.authorities?.contains(role) || params.authorities?.toList()?.contains(role.id.toString())}"
                            disabled="${!authorizedRoleList.contains(role)}"/>
                            <label for="authority_${role.id}">${role.authority} - ${role.description}</label><br/>
                          </g:each>
                        </td>
                    </tr>
                                        <tr class="ContainerFooter">
                     <td colspan="2">
				          <div class="AlignRight">
				              <input class="Button save" type="submit" value="${message(code: 'default.button.create.label')}" />
				            </div>
          
        </td>
      </tr>    

                </tbody>
                </table>


        </g:form>

    </div>
</body>
