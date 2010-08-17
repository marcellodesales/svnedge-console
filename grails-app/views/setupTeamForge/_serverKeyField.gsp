      <tr>
        <td class="ItemDetailName">
          <label for="serverKey">Integration API Key:</label>
        </td>
        <td valign="top" class="value ${hasErrors(bean:con,field:'serverKey','errors')}">
          <input size="60" type="text" id="serverKey" name="serverKey" 
              value="${fieldValue(bean:con,field:'serverKey')}"/>
          <g:hasErrors bean="${con}" field="serverKey">
              <g:eachError bean="${con}" field="serverKey">
                  <li><g:message error="${it}"/></li>
              </g:eachError>
          </g:hasErrors>
          <div class="errorMessage">Additional required field</div>
        </td>
        <td class="ItemDetailValue"><em>Shared secret for TeamForge/Subversion Edge communication.</em></td>
      </tr> 
