<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title><g:message code="repository.page.create.title" /></title>
    </head>

<content tag="title">
   <g:message code="repository.page.leftnav.title" />
</content>

<g:render template="leftNav" />

    <body>
      <table class="Container"> 
        <tr class="ContainerHeader">
            <td colspan="2"><g:message code="repository.page.leftnav.title" /></td>
        </tr>

        <g:form action="save" method="post" >
        <tr class="prop">
            <td valign="top" class="name">
                <label for="name"><g:message code="repository.page.create.name" /></label>
            </td>
            <td width="100%" valign="top" class="value errors">
                <input type="text" id="name" name="name" value="${fieldValue(bean:repo,field:'name')}"/>
                <g:hasErrors bean="${repo}" field="name">
                  <ul><g:eachError bean="${repo}" field="name">
                      <li><g:message error="${it}"/></li>
                  </g:eachError></ul>
                </g:hasErrors>
            </td>
        </tr> 
        <tr class="prop">
            <td valign="top" class="name" style="white-space: nowrap;"><label for="useTemplate"><g:message code="repository.page.create.useTemplate" /></label></td>
            <td valign="top" class="value ${hasErrors(bean:repo,field:'useTemplate','errors')}">
                <g:checkBox name="isTemplate" value="true" ></g:checkBox><g:message code="repository.page.create.defaultDirs" />
            </td>
        </tr>
        <tr class="ContainerFooter">
         <td colspan="2">
          <div class="AlignRight">
              <input class="Button save" type="submit" value="<g:message code='repository.page.create.button.create' />" />
          </div>
        </td>
      </tr>
      </tbody>
     </table>
     </g:form>
    </table>
    </body>
</html>
