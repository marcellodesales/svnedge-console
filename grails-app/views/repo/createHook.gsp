%{--
  - CollabNet Subversion Edge
  - Copyright (C) 2012, CollabNet Inc. All rights reserved.
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

<g:applyLayout name="repoDetail" params="[suppressTabs: true]">
  <content tag="tabContent">
    <g:uploadForm action="uploadHook">
      <g:hiddenField name="id" value="${params.id}"/>
      <div class="dialog">
        <table class="Container">
          <thead>
          <tr class="ContainerHeader">
            <td colspan="2"><g:message code="repository.page.hookCreate.heading"/></td>
          </tr>
          </thead>
          <tbody>
          <tr class="prop">
            <td valign="top" class="name">
              <label for="fileUpload"><g:message code="repository.page.hookCreate.upload.label" /></label>
            </td>
            <td valign="top" class="value">
              <input type="file" name="fileUpload" id="fileUpload"/>
              <i><g:message code="repository.page.hookCreate.upload.description" /></i>
            </td>
          </tr>
          <tr class="ContainerFooter">
            <td colspan="2">
              <div class="AlignRight">
                <g:actionSubmit action="uploadHook" class="Button save" value="${message(code: 'default.button.create.label')}" />
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </g:uploadForm>
  </content>
</g:applyLayout>
