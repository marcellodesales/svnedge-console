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
  
   <g:render template="/common/tabs"
       model="[tabs:[
         [action:'index', label:'1. Introduction'],
         [action:'ctfInfo', label:'2. TeamForge Credentials'],
         [action:'ctfProject',label:'3. TeamForge Project'],
         [action:'ctfUsers', label:'4. TeamForge Users'],
         [active: true, label:'5. Convert to TeamForge mode']
         ]]" />

   <g:form method="post">
   <table class="ItemDetailContainer">
     <tr>
      <td class="ContainerBodyWithPaddedBorder">

        <g:if test="${flash.errors}">
          <g:render template="errorList"/>
        </g:if>
        <g:else>
          <p><strong>You are ready!</strong> Click the convert button to switch to TeamForge mode.</p>
        </g:else>

      <table class="ItemDetailTable">
      <tr>
        <td class="ItemDetailName">CollabNet TeamForge server:
        </td>
        <td colspan="2" class="ItemDetailValue">${wizardBean.ctfURL}
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">CollabNet TeamForge version:
        </td>
        <td colspan="2" class="ItemDetailValue">
          <g:if test="${wizardBean.appVersion == wizardBean.apiVersion}">
            ${wizardBean.apiVersion}
          </g:if>
          <g:else>
            ${wizardBean.appVersion} (API: ${wizardBean.apiVersion})
          </g:else>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">Project:
        </td>
        <td colspan="2" class="ItemDetailValue">
          <g:if test="${wizardBean.isProjectPerRepo}">
            Each repository will be imported into a project of the same name.
          </g:if>
          <g:else>
          ${wizardBean.ctfProject}
          </g:else>
        </td>
      </tr>
    <g:if test="${wizardBean.lowercaseRepos || wizardBean.repoPrefix}">
      <tr>
        <td class="ItemDetailName">Repositories:
        </td>
        <td colspan="2" class="ItemDetailValue">
          <g:if test="${wizardBean.lowercaseRepos}">
            <div>Repository directory names containing capital letters will
            be converted to lowercase.</div>
          </g:if>
          <g:if test="${wizardBean.repoPrefix}">
            <div>Repository directory names starting with an invalid
              character will be prepended with '${wizardBean.repoPrefix}'.
            </div>
          </g:if>
        </td>
      </tr>
    </g:if>
      <tr>
        <td class="ItemDetailName">Users:
        </td>
        <td colspan="2" class="ItemDetailValue">
          <g:if test="${wizardBean.importUsers}">
          CollabNet Subversion users will be imported into CollabNet TeamForge.
          </g:if>
          <g:else>
	      No users will be imported into CollabNet TeamForge
          </g:else>
        </td>
      </tr>
    <g:if test="${wizardBean.importUsers}">
      <tr>
        <td class="ItemDetailName">Project membership:
        </td>
        <td colspan="2" class="ItemDetailValue">
          <g:if test="${wizardBean.assignMembership}">
          Imported users will be given membership in the project holding the imported repositories.
          </g:if>
          <g:else>
	      Membership decisions will be made later, within TeamForge.
          </g:else>
        </td>
      </tr>
    </g:if>
    <g:if test="${wizardBean && wizardBean.requiresServerKey}">
       <g:render template="serverKeyField" model="${[con:wizardBean]}"/>
    </g:if>

          </table>
        </td>
      </tr>
      <tr class="ContainerFooter">
        <td >
          <div class="AlignRight">
                <g:actionSubmit action="convert" value="Convert" class="Button"/>
          </div>
        </td>
      </tr>
      </table>
      </g:form>

    </body>
</html>
  
