<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title>Repository Access Rules</title>
</head>


<content tag="title">
    Repositories
</content>

<g:render template="leftNav"/>

<body>
<g:hasErrors bean="${authRulesCommand?.errors}">
    <div class="errors">
        <g:renderErrors as="list"/>
    </div>
</g:hasErrors>

<table class="Container">
<tr class="ContainerHeader">
    <td colspan="2">Repository Access Rules</td>
</tr>


<g:form action="saveAuthorization" method="post">

    <tr class="prop">

        <td width="100%" valign="top" class="value">
            <textarea id="accessRules" name="accessRules" rows="25" cols="80">${fieldValue(bean:authRulesCommand,field:'accessRules')}</textarea>
        </td>
    </tr>

    <tr class="ContainerFooter">
        <td colspan="2">
            <div class="AlignRight">
                <input class="Button save" type="submit" value="Save"/>
            </div>

        </td>
    </tr>

    </tbody>
    </table>

</g:form>
</table>

</body>
</html>
