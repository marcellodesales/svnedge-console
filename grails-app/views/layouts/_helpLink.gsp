
              <li class="dropdown"><a href="#"
                class="dropdown-toggle" data-toggle="dropdown"><g:message code="layout.page.help" /> <b
                  class="caret"></b></a>
                <ul class="dropdown-menu">
                  <li><a href="${helpUrl}" target="_blank"><g:message code="layout.page.help.current" /></a></li>
                  <li><a href="${helpBaseUrl}/topic/csvn/faq/whatiscollabnetsubversion.html"
                      target="_blank"><g:message code="layout.page.help.contents" /></a></li>
                  <li><a href="${helpBaseUrl}/topic/csvn/releasenotes/csvnedge.html"
                      target="_blank"><g:message code="layout.page.help.releaseNotes" /></a></li>
                  <li class="divider"></li>
                  <g:set var="isShowWizards" value="${false}"/>
                  <g:each var="wizard" in="${allWizards}">
                      <g:if test="${!wizard.active && !wizard.done}">
                        <g:set var="isShowWizards" value="${true}"/>
                      </g:if>
                  </g:each>
                  <g:if test="${isShowWizards}">
                    <g:each var="wizard" in="${allWizards}">
                      <g:if test="${!wizard.active && !wizard.done}">
                        <li><g:link controller="${wizard.controller}" action="startWizard">
                        <g:message code="wizard.${wizard.label}.inactiveTitle"/></g:link></li>
                      </g:if>
                    </g:each>                  
                    <li class="divider"></li>
                  </g:if>
                  <li><a data-toggle="modal" href="#aboutModal"><g:message code="layout.page.help.about" /></a></li>
                </ul>
              </li>
