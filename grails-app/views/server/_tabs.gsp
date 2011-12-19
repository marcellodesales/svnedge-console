  <g:set var="events" value="onclick='return warnForUnSavedData()'" />
  <g:set var="tabArray" value="${[[action: 'edit', label: message(code:'server.page.edit.tabs.general')]]}" />
  <g:if test="${!isManagedMode}">
    <g:set var="tabArray" value="${tabArray << [action: 'editAuthentication', events: events, label: message(code:'server.page.edit.tabs.authentication')]}" />
  </g:if>
  <g:set var="tabArray"
      value="${tabArray << [action: 'editProxy', events: events, label: message(code:'server.page.edit.tabs.proxy')]}"/>
  <g:set var="tabArray"
      value="${tabArray << [action: 'editMail', events: events, label: message(code:'server.page.edit.tabs.mail')]}"/>
  <g:render template="/common/tabs" model="${[tabs: tabArray]}" />
