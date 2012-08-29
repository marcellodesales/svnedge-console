<head>
    <meta name="layout" content="main" />
    <g:javascript>

    $(function() {

      $('#passwd').on('keyup', passwordConfirm);
      $('#passwordConfirm').on('keyup', passwordConfirm);
      $('#btnCreateUser').on('click', function(e) {
        if (!passwordConfirm(e)) {
            Event.stop(e);
            alert('<g:message code="setupCloudServices.page.signup.passwordConfirm.notEqual"/>');
        }
      });
    })

    function passwordConfirm(e) {
      var b = $('#passwd').attr("value") == $('#passwordConfirm').attr("value");
      $('#passwordConfirmMessage').css("display", b ? 'none' : 'inline');
      return b;
    }

    </g:javascript>
</head>


<content tag="title">
  <g:message code="user.page.create.title"/>
</content>

<g:render template="leftNav" />

<body>
  <g:form class="form-horizontal">
    <g:propTextField bean="${userInstance}" field="username" required="true" prefix="user"/>
    <g:propTextField bean="${userInstance}" field="realUserName" required="true" prefix="user"/>
    <g:propControlsBody bean="${userInstance}" field="passwd" required="true" prefix="user">
      <input type="password" id="passwd" name="passwd"
         value="${fieldValue(bean: userInstance, field: 'passwd')}"/>
    </g:propControlsBody>
    <g:propControlsBody bean="${userInstance}" field="passwordConfirm" required="true" prefix="user">
      <input type="password" id="passwordConfirm" name="passwordConfirm"
         value="${fieldValue(bean: userInstance, field: 'passwordConfirm')}"/>

      <span id="passwordConfirmMessage" class="TextRequired" style="display: none;">
      <img width="15" height="15" alt="Warning" align="bottom"
                src="${resource(dir: 'images/icons', file: 'icon_warning_sml.gif')}" border="0"/>
      <g:message code="setupCloudServices.page.signup.passwordConfirm.notEqual"/>
      </span>
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
       <g:actionSubmit id="btnCreateUser"
                      value="${message(code:'default.button.create.label')}"
                      controller="user" action="save" class="btn btn-primary"/>
    </div>
  </g:form>
</body>
