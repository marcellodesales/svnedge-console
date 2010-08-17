<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create Repository</title>
    </head>

<content tag="title">
        Repositories
</content>

<g:render template="leftNav" />

    <body>
      <table class="Container"> 
        <tr class="ContainerHeader">
            <td colspan="2">Create Repository</td>
        </tr>

        <g:form action="save" method="post" >
        <tr class="prop">
            <td valign="top" class="name">
                <label for="name">Name:</label>
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
            <td valign="top" class="name" style="white-space: nowrap;"><label for="useTemplate">Use template:</label></td>
            <td valign="top" class="value ${hasErrors(bean:repo,field:'useTemplate','errors')}">
                <g:checkBox name="isTemplate" value="true" ></g:checkBox>Create standard trunk/branches/tags structure
            </td>
        </tr>
        <tr class="ContainerFooter">
         <td colspan="2">
          <div class="AlignRight">
              <input class="Button save" type="submit" value="Create" />
          </div>
        </td>
      </tr>
      </tbody>
     </table>
     </g:form>
    </table>
    </body>
</html>
