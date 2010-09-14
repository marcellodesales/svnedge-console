<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title><g:message code="repository.page.edit.title" /></title>
    </head>
    
    <g:render template="leftNav" />
    
    <body>
            <g:hasErrors bean="${repositoryInstance}">
            <div class="errors">
                <g:renderErrors bean="${repositoryInstance}" as="list" />
            </div>
            </g:hasErrors>
        <table class="Container"> 
        <tr class="ContainerHeader">
            <td colspan="2"><g:message code="repository.page.edit.header" /></td>        
        </tr>   
            
            <g:form method="post" >
                <input type="hidden" name="id" value="${repositoryInstance?.id}" />
                <input type="hidden" name="version" value="${repositoryInstance?.version}" />

                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><g:message code="repository.page.edit.name" /></label>
                                </td>
                                <td width="100%" valign="top" class="value ${hasErrors(bean:repositoryInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:repositoryInstance,field:'name')}"/>
                                </td>
                            </tr> 
                        <tr class="ContainerFooter">
                     <td colspan="2">
                       <div class="AlignRight">
                           <g:actionSubmit class="Button save" value="${message(code: 'repository.page.edit.button.save')}" />
                           <g:actionSubmit class="Button delete" onclick="return confirm('${message(code: 'repository.page.edit.button.save.confirm')}');" value="Delete" />
                       </div>
          
        </td>
      </tr>    
                        
                        </tbody>
                    </table>

            </g:form>

    </body>
</html>
