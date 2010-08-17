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
         [active: true, label:'3. TeamForge Project'],
         [label:'4. TeamForge Users'],
         [label:'5. Convert to TeamForge mode']
         ]]" />

  <g:form method="post">
      <table class="ItemDetailContainer">
      <tr>
        <td class="ItemDetailContainerCell">

          <g:if test="${con.errors.hasGlobalErrors()}">
            <div class="errorMessage">
              <ul>
              <g:each in="${con.errors.globalErrors}">
                  <li><g:message error="${it}" /></li>
              </g:each>
              </ul>
            </div>
          </g:if>

          <p>
          CollabNet TeamForge is organized into project work spaces.  A project might have one or more of
          several tools including trackers, discussions, wiki, and more in addition to source code repositories.
          A project is also a convenient way to introduce role-based access of users to the tools and
          repositories. 
          </p>

          <p>
          As part of integrating this Subversion Edge server into CollabNet 
          TeamForge, the repositories contained on the server will be added to a
          project.  You may use a project which already exists in
          TeamForge; or if the chosen project name is new, the project will be
          created during the conversion.
          </p>

      <table class="ItemDetailTable">
        <tr>
             <td class="ItemDetailName">
               <label for="ctfProject">TeamForge project name:</label>
             </td>
             <td class="value ${hasErrors(bean:con,field:'ctfProject','errors')}">
             <g:hiddenField name="projectType" id="projectTypeSingle" value="single"/>
               <input size="30" type="text" id="ctfProject" name="ctfProject" 
                  value="${fieldValue(bean:con, field:'ctfProject')}"/>
             </td>
             <td class="ItemDetailValue"><em>Project where repositories will be registered</em></td>
        </tr>
        <tr>
         <td></td>
          <td class="errors" colspan="2">
             <g:hasErrors bean="${con}" field="ctfProject">
               <ul><g:eachError bean="${con}" field="ctfProject">
                  <li><g:message error="${it}"/></li>
               </g:eachError></ul>
             </g:hasErrors>
          </td>
        </tr>
        <g:if test="${invalidRepoNames.containsUpperCaseRepos}">
        <tr>
             <td class="ItemDetailName">
               <label for="lowercaseRepos">Convert to lowercase:</label>
             </td>
             <td class="value">
               <g:checkBox id="lowercaseRepos" name="lowercaseRepos"
                   value="${con.lowercaseRepos}"/>
             </td>
             <td class="ItemDetailValue"><em>Some repositories contain capital letters in their name. Select the checkbox to automatically convert them to lowercase.</em></td>
        </tr>
        </g:if>
        <g:if test="${invalidRepoNames.containsReposWithInvalidFirstChar}">
        <tr>
             <td class="ItemDetailName">
               <label for="repoPrefix">Repository name prefix:</label>
             </td>
             <td class="value ${hasErrors(bean:con,field:'repoPrefix','errors')}">
               <input size="10" type="text" id="repoPrefix" name="repoPrefix" 
                  value="${fieldValue(bean:con, field:'repoPrefix')}"/>
             </td>
             <td class="ItemDetailValue"><em>A lowercase alphabetical prefix to be prepended to repository names which don't meet Teamforge's constraint on the first character in repository names.</em></td>
        </tr>
        <tr>
         <td></td>
          <td class="errors" colspan="2">
             <g:hasErrors bean="${con}" field="repoPrefix">
               <ul><g:eachError bean="${con}" field="repoPrefix">
                  <li><g:message error="${it}"/></li>
               </g:eachError></ul>
             </g:hasErrors>
          </td>
        </tr>
        </g:if>
      </table>
      </td>
      </tr>
      <tr class="ContainerFooter">
        <td>
          <div class="AlignRight">
                <g:actionSubmit action="updateProject" value="Continue" class="Button"/>
          </div>
        </td>
      </tr>
    </table>
    </g:form>

    </body>
</html>
  
