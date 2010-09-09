      <tr>
        <td class="ItemDetailName">
          <label for="serverKey"><g:message code="ctfConversionBean.serverKey.label" />:</label>
        </td>
        <td valign="top" class="value ${hasErrors(bean:con,field:'serverKey','errors')}">
          <input size="60" type="text" id="serverKey" name="serverKey" 
              value="${fieldValue(bean:con,field:'serverKey')}"/>
          <g:hasErrors bean="${con}" field="serverKey">
              <g:eachError bean="${con}" field="serverKey">
                  <li><g:message error="${it}"/></li>
              </g:eachError>
          </g:hasErrors>
          <div class="errorMessage"><g:message code="setupTeamForge.page.error.additional" /></div>
        </td>
        <td class="ItemDetailValue"><em><g:message code="ctfConversionBean.serverKey.error.missing" />.</em></td>
      </tr> 
