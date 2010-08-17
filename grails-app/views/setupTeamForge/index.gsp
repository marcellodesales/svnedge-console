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

    <g:if test="${isFreshInstall}">
      <g:render template="/common/tabs"
          model="[tabs:[
            [active:true, label:'1. Introduction'],
            [action:'ctfInfo', label:'2. Convert to TeamForge mode']
            ]]" />
    </g:if>
    <g:else>
      <g:render template="/common/tabs"
          model="[tabs:[
            [active:true, label:'1. Introduction'],
            [action:'ctfInfo', label:'2. TeamForge Credentials'],
            [label:'3. TeamForge Project'],
            [label:'4. TeamForge Users'],
            [label:'5. Convert to TeamForge mode']
            ]]" />
    </g:else>

 <table class="ItemDetailContainer">
  <tr>
   <td class="ContainerBodyWithPaddedBorder">

    <a href="http://www.open.collab.net/products/ctf/">
     <img style="float:right; padding: 10px" width="520" height="367" alt="" 
          src="${resource(dir:'images/about',file:'ctf.gif')}" border="0"/>
    </a>
    <br/><br/>
    <p>
    <strong>Agile ALM for Distributed Development</strong>
    </p>
    <br/>
    <p>
    <a href="http://www.open.collab.net/products/ctf/">CollabNet TeamForge</a> is an 
    Application Lifecycle Management platform designed for distributed software 
    development teams. It's optimized around Subversion for source code management and covers the full 
    development lifecycle from requirements through release.
    </p>
    <p>CollabNet Subversion Edge can be setup to function as an SCM integration server for CollabNet TeamForge.
    In this mode, the svn server administration will be entirely controlled by TeamForge.
    </p>

    <g:if test="${isFreshInstall}">
        <p>As this CollabNet Subversion Edge installation does not yet have any repositories in use, there will be
       nothing to import into CollabNet TeamForge.  You will just need to supply the URL to the TeamForge 
       server along with administrator credentials.
       </p>
    </g:if>
    <g:else>
        <p>
        As part of the conversion to using TeamForge to administer the repositories and users, this process
        will register the repositories with one or more TeamForge projects.  Users will also be imported.  
        Please follow the short wizard to complete the conversion process.
        </p>
    </g:else>
   </td>
   </tr>
     <tr class="ContainerFooter">
       <td >
         <g:form method="post">
         <div class="AlignRight">
           <g:actionSubmit action="ctfInfo" value="Continue" class="Button"/>
         </div>
         </g:form>
       </td>
     </tr>
</table>

  </body>
</html>
