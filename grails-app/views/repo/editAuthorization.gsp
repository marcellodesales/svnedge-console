<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="repository.page.editAuthorization.title" /></title>
      <g:javascript library="prototype" />
    
</head>


<content tag="title">
    <g:message code="repository.page.leftnav.title" />
</content>

<g:render template="leftNav"/>

<body>
<g:hasErrors bean="${authRulesCommand?.errors}">
    <div class="errors">
        <g:renderErrors as="list"/>
    </div>
</g:hasErrors>

<g:render template="/common/fileEditor"
    model="[fileContent: fieldValue(bean:authRulesCommand, field:'fileContent'),
        fileId: 'accessRules',
        saveAction: 'saveAuthorization',
        cancelAction: 'cancelEditAuthorization',
        ajaxCancelUrl: '/csvn/repo/cancelEditAuthorization',
        heading: message(code: 'repository.page.editAuthorization.header')]" />

</body>
</html>
