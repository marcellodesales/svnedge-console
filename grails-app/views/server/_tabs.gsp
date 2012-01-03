  <g:set var="events" value="onclick='return warnForUnSavedData()'" />
  <g:set var="tabArray" value="${[[action: 'edit', label: message(code:'server.page.edit.tabs.general'), active: view == 'edit']]}" />
  <g:if test="${!isManagedMode}">
    <g:set var="tabArray" value="${tabArray << [action: 'editAuthentication', events: events, label: message(code:'server.page.edit.tabs.authentication'), active: view == 'editAuthentication']}" />
  </g:if>
  <g:set var="tabArray"
      value="${tabArray << [action: 'editProxy', events: events, label: message(code:'server.page.edit.tabs.proxy'), active: view == 'editProxy']}"/>
  <g:set var="tabArray"
      value="${tabArray << [action: 'editMail', events: events, label: message(code:'server.page.edit.tabs.mail'), active: view == 'editMail']}"/>
  <g:render template="/common/tabs" model="${[tabs: tabArray]}" />
