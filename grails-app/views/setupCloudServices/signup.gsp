%{--
  - CollabNet Subversion Edge
  - Copyright (C) 2011, CollabNet Inc. All rights reserved.
  -
  - This program is free software: you can redistribute it and/or modify
  - it under the terms of the GNU Affero General Public License as published by
  - the Free Software Foundation, either version 3 of the License, or
  - (at your option) any later version.
  -
  - This program is distributed in the hope that it will be useful,
  - but WITHOUT ANY WARRANTY; without even the implied warranty of
  - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  - GNU Affero General Public License for more details.
  -
  - You should have received a copy of the GNU Affero General Public License
  - along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --}%
<html>
<head>
  <title>CollabNet Subversion Edge <g:message code="setupCloudServices.page.signup.title"/></title>
  <meta name="layout" content="main"/>
  <g:javascript library="prototype"/>
  <g:javascript library="application"/>

  <script type="text/javascript">

    var messages = {
      prompt: '<g:message code="setupCloudServices.login.available.prompt"/>',
      checking: '<g:message code="setupCloudServices.login.available.checking"/>',
    }

    Event.observe(window, 'load', function() {
      var usernameField = $('username')
      var usernameMsgElement = $('usernameUniquenessMessage')
      var loginChecker = new CloudTokenAvailabilityChecker(usernameField, usernameMsgElement, '/csvn/setupCloudServices/checkLoginAvailability', messages.checking, messages.prompt)
      Event.observe(usernameField, 'keydown', function(e) {
        loginChecker.keypressHandler()
      })

      var domainField = $('domain')
      var domainMsgElement = $('domainUniquenessMessage')
      var domainChecker = new CloudTokenAvailabilityChecker(domainField, domainMsgElement, '/csvn/setupCloudServices/checkDomainAvailability', messages.checking, messages.prompt)
      Event.observe(domainField, 'keydown', function(e) {
        domainChecker.keypressHandler()
      })
    })

  </script>
</head>
<content tag="title">
  <g:message code="setupCloudServices.page.leftNav.header"/>
</content>

<g:render template="/server/leftNav"/>
<body>
<g:form>
<table class="ItemDetailContainer">
<tr class="ContainerHeader">
  <td><g:message code="setupCloudServices.page.signup.title"/></td>
</tr>
<tr>
<td class="ContainerBodyWithPaddedBorder">
<p><g:message code="setupCloudServices.page.signup.p1"/></p>
<table class="ItemDetailContainer">
<tr>
  <td class="ItemDetailName">
    <label for="firstName"><g:message code="setupCloudServices.page.signup.firstName.label"/></label>
  </td>
  <td valign="top">
    <input size="40" type="text" id="firstName" name="firstName"
           value="${fieldValue(bean: cmd, field: 'firstName')}"/>
  </td>
  <td></td>
</tr>
<tr>
  <td></td>
  <td class="errors" colspan="2">
    <g:hasErrors bean="${cmd}" field="firstName">
      <ul><g:eachError bean="${cmd}" field="firstName">
        <li><g:message error="${it}" encodeAs="HTML"/></li>
      </g:eachError></ul>
    </g:hasErrors>
  </td>
</tr>
<tr>
  <td class="ItemDetailName">
    <label for="lastName"><g:message code="setupCloudServices.page.signup.lastName.label"/></label>
  </td>
  <td valign="top">
    <input size="40" type="text" id="lastName" name="lastName"
           value="${fieldValue(bean: cmd, field: 'lastName')}"/>
  </td>
  <td></td>
</tr>
<tr>
  <td></td>
  <td class="errors" colspan="2">
    <g:hasErrors bean="${cmd}" field="lastName">
      <ul><g:eachError bean="${cmd}" field="lastName">
        <li><g:message error="${it}" encodeAs="HTML"/></li>
      </g:eachError></ul>
    </g:hasErrors>
  </td>
</tr>
<tr>
  <td class="ItemDetailName">
    <label for="emailAddress"><g:message code="setupCloudServices.page.signup.emailAddress.label"/></label>
  </td>
  <td valign="top">
    <input size="40" type="text" id="emailAddress" name="emailAddress"
           value="${fieldValue(bean: cmd, field: 'emailAddress')}"/>
  </td>
  <td>
    <em><g:message code="setupCloudServices.page.signup.emailAddress.label.tip"/></em>
  </td>
</tr>
<tr>
  <td></td>
  <td class="errors" colspan="2">
    <g:hasErrors bean="${cmd}" field="emailAddress">
      <ul><g:eachError bean="${cmd}" field="emailAddress">
        <li><g:message error="${it}" encodeAs="HTML"/></li>
      </g:eachError></ul>
    </g:hasErrors>
  </td>
</tr>
<tr>
  <td class="ItemDetailName">
    <label for="phoneNumber"><g:message code="setupCloudServices.page.signup.phone.label"/></label>
  </td>
  <td valign="top">
    <input size="40" type="text" id="phoneNumber" name="phoneNumber"
           value="${fieldValue(bean: cmd, field: 'phoneNumber')}"/>
  </td>
  <td></td>
</tr>
<tr>
  <td></td>
  <td class="errors" colspan="2">
    <g:hasErrors bean="${cmd}" field="phoneNumber">
      <ul><g:eachError bean="${cmd}" field="phoneNumber">
        <li><g:message error="${it}" encodeAs="HTML"/></li>
      </g:eachError></ul>
    </g:hasErrors>
  </td>
</tr>
<tr>
  <td class="ItemDetailName">
    <label for="username"><g:message code="setupCloudServices.page.signup.username.label"/></label>
  </td>
  <td valign="top">
    <input size="40" type="text" id="username" name="username"
           value="${fieldValue(bean: cmd, field: 'username')}"/>
  </td>
  <td>
    <span id="usernameUniquenessMessage"></span>
  </td>
</tr>
<tr>
  <td></td>
  <td class="errors" colspan="2">
    <g:hasErrors bean="${cmd}" field="username">
      <ul><g:eachError bean="${cmd}" field="username">
        <li><g:message error="${it}" encodeAs="HTML"/></li>
      </g:eachError></ul>
    </g:hasErrors>
  </td>
</tr>
<tr>
  <td class="ItemDetailName">
    <label for="password"><g:message code="setupCloudServices.page.signup.password.label"/></label>
  </td>
  <td valign="top">
    <input size="40" type="password" id="password" name="password"
           value="${fieldValue(bean: cmd, field: 'password')}"/>
  </td>
  <td></td>
</tr>
<tr>
  <td></td>
  <td class="errors" colspan="2">
    <g:hasErrors bean="${cmd}" field="password">
      <ul><g:eachError bean="${cmd}" field="password">
        <li><g:message error="${it}" encodeAs="HTML"/></li>
      </g:eachError></ul>
    </g:hasErrors>
  </td>
</tr>
<tr>
  <td class="ItemDetailName">
    <label for="passwordConfirm"><g:message
            code="setupCloudServices.page.signup.passwordConfirm.label"/></label>
  </td>
  <td valign="top">
    <input size="40" type="password" id="passwordConfirm" name="passwordConfirm"
           value="${fieldValue(bean: cmd, field: 'passwordConfirm')}"/>
  </td>
  <td></td>
</tr>
<tr>
  <td></td>
  <td class="errors" colspan="2">
    <g:hasErrors bean="${cmd}" field="passwordConfirm">
      <ul><g:eachError bean="${cmd}" field="passwordConfirm">
        <li><g:message error="${it}" encodeAs="HTML"/></li>
      </g:eachError></ul>
    </g:hasErrors>
  </td>
</tr>
<tr>
  <td class="ItemDetailName">
    <label for="organization"><g:message code="setupCloudServices.page.signup.organization.label"/></label>
  </td>
  <td valign="top">
    <input size="40" type="text" id="organization" name="organization"
           value="${fieldValue(bean: cmd, field: 'organization')}"/>
  </td>
  <td>
    <em><g:message code="setupCloudServices.page.signup.organization.label.tip"/></em>
  </td>
</tr>
<tr>
  <td></td>
  <td class="errors" colspan="2">
    <g:hasErrors bean="${cmd}" field="organization">
      <ul><g:eachError bean="${cmd}" field="organization">
        <li><g:message error="${it}" encodeAs="HTML"/></li>
      </g:eachError></ul>
    </g:hasErrors>
  </td>
</tr>
<tr>
  <td class="ItemDetailName">
    <label for="domain"><g:message code="setupCloudServices.page.signup.domain.label"/></label>
  </td>
  <td valign="top">
    <input size="40" type="text" id="domain" name="domain"
           value="${fieldValue(bean: cmd, field: 'domain')}"/>
  </td>
  <td>
    <em><g:message code="setupCloudServices.page.signup.domain.label.tip"/></em> <br/>
    <span id="domainUniquenessMessage"></span>
  </td>
</tr>
<tr>
  <td></td>
  <td class="errors" colspan="2">
    <g:hasErrors bean="${cmd}" field="domain">
      <ul><g:eachError bean="${cmd}" field="domain">
        <li><g:message error="${it}" encodeAs="HTML"/></li>
      </g:eachError></ul>
    </g:hasErrors>
  </td>
</tr>
<tr>
  <td class="ItemDetailName">
  </td>
  <td valign="top" colspan="2">
    <g:checkBox name="acceptTerms" checked="${cmd.acceptTerms}"/>
    <label for="acceptTerms"><g:message code="setupCloudServices.page.signup.terms.label"/></label>
  </td>
</tr>
<tr>
  <td></td>
  <td class="errors" colspan="2">
    <g:hasErrors bean="${cmd}" field="acceptTerms">
      <ul><g:eachError bean="${cmd}" field="acceptTerms">
        <li><g:message error="${it}" encodeAs="HTML"/></li>
      </g:eachError></ul>
    </g:hasErrors>
  </td>
</tr>
<tr>
</table>
</td>
</tr>
<tr class="ContainerFooter">
  <td>
    <div class="AlignRight">
      <g:actionSubmit id="btnCloudServicesExistingLogin"
                      value="${message(code:'setupCloudServices.page.signup.button.existingLogin')}"
                      controller="setupCloudServices" action="credentials" class="Button"/>

      <g:actionSubmit id="btnCloudServicesCreateAccout"
                      value="${message(code:'setupCloudServices.page.signup.button.continue')}"
                      controller="setupCloudServices" action="createAccount" class="Button"/>
    </div>
  </td>
</tr>
</table>
</g:form>

</body>
</html>
