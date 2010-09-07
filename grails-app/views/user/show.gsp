<head>
    <meta name="layout" content="main" />
    <title>Show User</title>
</head>

<content tag="title">
    Users
</content>

<g:render template="leftNav" />

<body>

    <div class="body">
        <h1>Show User</h1>
        <div class="dialog">
            <table>
            <tbody>

                <tr class="prop">
                    <td valign="top" class="name">ID:</td>
                    <td valign="top" class="value">${userInstance.id}</td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">Login Name:</td>
                    <td valign="top" class="value">${userInstance.username?.encodeAsHTML()}</td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">Full Name:</td>
                    <td valign="top" class="value">${userInstance.realUserName?.encodeAsHTML()}</td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">Description:</td>
                    <td valign="top" class="value">${userInstance.description?.encodeAsHTML()}</td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">Email:</td>
                    <td valign="top" class="value">${userInstance.email?.encodeAsHTML()}</td>
                </tr>


                <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_USERS">


                  <tr class="prop">
                      <td valign="top" class="name"><g:message code="user.authorities.label" default="Roles Granted" />:</td>

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
                <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_USERS">
                <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:ifAnyGranted>
            </g:form>
        </div>

    </div>
</body>
