<head>
    <meta name="layout" content="main" />
</head>


<content tag="title">
  <g:message code="user.page.create.title"/>
</content>

<g:render template="leftNav" />

<body>
  <g:form class="form-horizontal" action="save">
    <g:propTextField bean="${userInstance}" field="username" required="true" prefix="user"/>
    <g:propTextField bean="${userInstance}" field="realUserName" required="true" prefix="user"/>
    <g:propControlsBody bean="${userInstance}" field="passwd" required="true" prefix="user">
      <input type="password" id="passwd" name="passwd" value="${userInstance.passwd}"/>
    </g:propControlsBody>
    <g:propTextField bean="${userInstance}" field="email" required="true" prefix="user"/>
    <g:propTextField bean="${userInstance}" field="description" prefix="user"/>

    <g:propControlsBody bean="${userInstance}" field="authorities" prefix="user">
                          <g:each in="${roleList}" var="role">
                            <g:checkBox id="authority_${role.id}" name="authorities" value="${role.id}"
                            checked="${userInstance.authorities?.contains(role) || params.authorities?.toList()?.contains(role.id.toString())}"
                            disabled="${!authorizedRoleList.contains(role)}"/>
                            <label class="checkbox inline withFor" for="authority_${role.id}">${role.authority} - ${role.description}</label><br/>
                          </g:each>
    </g:propControlsBody>
    
    <div class="form-actions">
      <input class="btn btn-primary" type="submit" value="${message(code: 'default.button.create.label')}" />
    </div>
  </g:form>
</body>
