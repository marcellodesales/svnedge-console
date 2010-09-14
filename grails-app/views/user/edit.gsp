<head>
    <meta name="layout" content="main" />
    <title>CollabNet Subversion Edge <g:message code="user.page.edit.title"/></title>
</head>


<content tag="title">
  <g:message code="user.page.header"/>
</content>

<g:render template="leftNav" />

<body>
        <g:hasErrors bean="${userInstance}" field="version">
        <div class="errors">
            <g:renderErrors bean="${userInstance}" field="version" as="list" />
        </div>
        </g:hasErrors>
        <g:form>
            <input type="hidden" name="id" value="${userInstance.id}" />
            <input type="hidden" name="version" value="${userInstance.version}" />
    <table class="Container"> 
        <tr class="ContainerHeader">
            <td colspan="3"><g:message code="user.page.edit.title"/></td>        
        </tr> 
                    <tr class="prop">
                        <td valign="top" class="name"><g:message code="user.username.label"/></td>
                        <td class="value">
                            ${userInstance.username?.encodeAsHTML()}
            </td>
            <td width="100%" >
                           <div id="passwd_change_link"><a href="#" onclick="showPasswdFields()"><g:message code="user.page.edit.passwdchange"/></a></div>
                        </td>
                    </tr>

                    <tr class="prop" id="passwd_row">
                        <td class="name"><label for="passwd"><g:message code="user.page.edit.passwd"/></label></td>
                        <td class="value ${hasErrors(bean:userInstance,field:'passwd','errors')}">
                            <input type="password" id="passwd" name="passwd" value=""/>
                        </td>
            <td width="100%" >
                           <div id="cancel_passwd_link"><a href="#" onclick="cancelPasswordChange()"><g:message code="user.page.edit.passwdchange.cancel"/></a></div>
                        </td>
                    </tr>
    <g:hasErrors bean="${userInstance}" field="passwd">
      <tr id="passwd_errors_row">
        <td>&nbsp;</td>
        <td colspan="2" width="100%" class="errors">
          <ul><g:eachError bean="${userInstance}" field="passwd">
              <li><g:message error="${it}"/></li>
          </g:eachError></ul>
        </td>
      </tr>
    </g:hasErrors>

                    <tr class="prop" id="passwd_confirm_row">
                        <td class="name"><label for="confirmPasswd"><g:message code="user.page.edit.passwd.confirm"/></label></td>
                        <td class="value ${hasErrors(bean:userInstance,field:'passwd','errors')}">
                            <input type="password" id="confirmPasswd" name="confirmPasswd" value=""/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td valign="top" class="name"><label for="realUserName"><g:message code="user.realUserName.label"/></label></td>
                        <td class="value ${hasErrors(bean:userInstance,field:'realUserName','errors')}">
                            <input type="text" id="realUserName" name="realUserName" value="${userInstance.realUserName?.encodeAsHTML()}"/>
                        </td>
                    </tr>
    <g:hasErrors bean="${userInstance}" field="realUserName">
      <tr>
        <td>&nbsp;</td>
        <td colspan="2" width="100%" class="errors">
          <ul><g:eachError bean="${userInstance}" field="realUserName">
              <li><g:message error="${it}"/></li>
          </g:eachError></ul>
        </td>
      </tr>
    </g:hasErrors>

                    <tr class="prop">
                        <td valign="top" class="name"><label for="description"><g:message code="user.description.label"/></label></td>
                        <td class="value ${hasErrors(bean:userInstance,field:'description','errors')}">
                            <input type="text" id="description" name="description" value="${userInstance.description?.encodeAsHTML()}"/>
                        </td>
                    </tr>
    <g:hasErrors bean="${userInstance}" field="description">
      <tr>
        <td>&nbsp;</td>
        <td colspan="2" width="100%" class="errors">
          <ul><g:eachError bean="${userInstance}" field="description">
              <li><g:message error="${it}"/></li>
          </g:eachError></ul>
        </td>
      </tr>
    </g:hasErrors>

                    <tr class="prop">
                        <td valign="top" class="name"><label for="email"><g:message code="user.email.label"/></label></td>
                        <td class="value ${hasErrors(bean:userInstance,field:'email','errors')}">
                            <input type="text" id="email" name="email" value="${userInstance?.email?.encodeAsHTML()}"/>
                        </td>
                    </tr>
    <g:hasErrors bean="${userInstance}" field="email">
      <tr>
        <td>&nbsp;</td>
        <td colspan="2" width="100%" class="errors">
          <ul><g:eachError bean="${userInstance}" field="email">
              <li><g:message error="${it}"/></li>
          </g:eachError></ul>
        </td>
      </tr>
    </g:hasErrors>

                  <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_USERS">
                   <g:if test="${allowEditingRoles}">
              
                    <tr class="prop">
                        <td valign="top" class="name" style="white-space: nowrap;">
                          <label for="authorities"><g:message code="user.authorities.label" /></label>
                        </td>
                        <td  colspan="2" class="value ${hasErrors(bean: userInstance, field: 'authorities', 'errors')}">
                          <g:each in="${roleList}" var="role">
                            <g:checkBox id="authority_${role.id}" name="authorities" value="${role.id}"
                            checked="${userInstance.authorities.contains(role)}"
                            disabled="${!authorizedRoleList.contains(role)}"/>
                            <label for="authority_${role.id}">${role.authority} - ${role.description}</label><br/>
                          </g:each>
                        </td>
                    </tr>
                     </g:if>
                   </g:ifAnyGranted>
    <g:hasErrors bean="${userInstance}" field="authorities">
      <tr>
        <td>&nbsp;</td>
        <td colspan="2" width="100%" class="errors">
          <ul><g:eachError bean="${userInstance}" field="authorities">
              <li><g:message error="${it}"/></li>
          </g:eachError></ul>
        </td>
      </tr>
    </g:hasErrors>
                   <tr class="ContainerFooter">
                     <td colspan="3">
                        <div class="AlignRight">
                           <g:actionSubmit class="Button save" value="${message(code:'user.page.edit.button.save')}" />
                           <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_USERS">
                              <g:set var="question" value="${message(code:'user.page.edit.button.delete.confirm')}" />
                              <g:actionSubmit class="Button delete" onclick="return confirm('${question}');" value="${message(code:'user.page.edit.button.delete')}" />
                           </g:ifAnyGranted>
                        </div>
                     </td>
                  </tr>

                </tbody>
                </table>

        </g:form>

<g:javascript>
<!--
function el(id) {
    return document.getElementById(id);
}

function showPasswdFields() {
    parentOfRows.insertBefore(passwdRow, whereToInsert);
    if (passwdErrorsRow) {
        parentOfRows.insertBefore(passwdErrorsRow, whereToInsert);
    }
    parentOfRows.insertBefore(passwdConfirmRow, whereToInsert);
    el("passwd_change_link").style.display="none";
}

function cancelPasswordChange() {
    parentOfRows.removeChild(passwdRow);
    if (passwdErrorsRow) {
        parentOfRows.removeChild(passwdErrorsRow);
    }
    parentOfRows.removeChild(passwdConfirmRow);
    el("passwd_change_link").style.display="block";
}

var passwdRow = el("passwd_row");
var passwdConfirmRow = el("passwd_confirm_row");
var passwdErrorsRow = el("passwd_errors_row");
var parentOfRows = passwdRow.parentNode;
var whereToInsert = passwdConfirmRow.nextSibling;
if (passwdErrorsRow) {
    el("passwd_change_link").style.display="none";
} else {
    //passwdRow.style.display="none";
    //passwdConfirmRow.style.display="none";
    parentOfRows.removeChild(passwdRow);
    parentOfRows.removeChild(passwdConfirmRow);
}
//-->
</g:javascript>
</body>
