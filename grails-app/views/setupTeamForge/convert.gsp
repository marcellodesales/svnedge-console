<html>
  <head>
    <title>CollabNet TeamForge Integration</title>
      <meta name="layout" content="main" />
      <g:javascript library="prototype" />
  </head>
  <content tag="title">
    CollabNet TeamForge Integration
  </content>

  <g:render template="/server/leftNav" />

  <body>
  <div class="instructionText">
    <g:if test="${ctfProjectLink}">
      <p>You may browse the repositories at the link below:</p>
      <p>Project '${wizardBean.ctfProject}' Source Code: <a href="${ctfProjectLink}">${ctfProjectLink}</a></p>
    </g:if>
    <g:if test="${ctfLink}">
      <p>CollabNet TeamForge SCM Integrations: <a href="${ctfLink}">${ctfLink}</a></p>
    </g:if>
  </div>

<ul><li>CollabNet TeamForge usernames and passwords will now be used to login to the Subversion Edge console.</li> 
<li>Limited server administration capabilities are still available from this console for TeamForge Site 
Admin users.</li>
</ul>

In case you need to revert this conversion, click on the menu item "TeamForge Mode" 
for details.

<g:if test="${warnings}">
  <div class="warningText">
Some problems were noted during the conversion. These shouldn't affect the
functionality of the server, but might require some follow-up.
    <ul>
    <g:each in="${warnings}">
    <li>${it}</li>
    </g:each>
    </ul>
  </div>
</g:if>

    </body>
</html>
  
