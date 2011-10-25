<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title><g:message code="repository.page.create.title"/></title>
  <g:javascript library="prototype"/>
  <script type="text/javascript" src="/csvn/js/simpletreemenu.js">
    /***********************************************
     * Simple Tree Menu- © Dynamic Drive DHTML code library (www.dynamicdrive.com)
     * This notice MUST stay intact for legal use
     * Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code
     ***********************************************/
  </script>
  <link rel="stylesheet" type="text/css" href="/csvn/css/simpletree.css"/>
  <script type="text/javascript">

    Event.observe(window, 'load', function() {
      $('name').focus();

      $$('input.repoInitOptions').each(function(item) {
        item.observe('click', function(e) {
          showSelectedOptionDetail();
        })
      })

      setInitialOptionState()
      showSelectedOptionDetail()

      // load all repo dump files into the backup chooser div
      new Ajax.Request('/csvn/repo/dumpFileListAll', {
        method:'get',
        requestHeaders: {Accept: 'text/json'},
        onSuccess: function(transport) {
          prepareBackupsTree(transport)
        }
      })

    });

    var initOptionParam = "${params.initOption ?: ''}";
    var initOptionSelectedParam = "${params.initOptionSelected ?: ''}";

    function showSelectedOptionDetail() {
      hideAllOptionDetails()
      $$('input.repoInitOptions').each(function(item) {
        if (item.checked == true) {
          var detailsClass = ".initOptionDetail." + item.id
          $$(detailsClass)[0].show()
        }
      })
    }

    function hideAllOptionDetails() {
      $$('.initOptionDetail').each(function(item) {
        item.hide()
      });
    }

    function setInitialOptionState() {
      if (initOptionParam.length > 0) {
        $$('input.repoInitOptions').each(function(item) {
          if (item.value == initOptionParam) {
            item.checked = true;
          }
        })
      }
    }

    function prepareBackupsTree(transport) {
      var responseJson = transport.responseText.evalJSON(true);
      if (responseJson.result != null) {
        backupsHtml = "<p><g:message code='repository.page.create.useBackup.instructions'/></p>";
        backupsHtml += '<ul id="backupsTree" class="treeview">';
        for (var key in responseJson.result.repoDumps) {
          // add repo name to list
          backupsHtml += "<li class='repo'>" + key
          // pre-open if item was selected in previous submit
          var initialStateOpen = initOptionSelectedParam.indexOf(key) > -1

          // add sublist with backup files for the repo
          var backups = responseJson.result.repoDumps[key]
          if (backups.length > 0) {
            if (initialStateOpen) {
              backupsHtml += "<ul class='backupList' rel='open'>"
            }
            else {
              backupsHtml += "<ul class='backupList'>"
            }
            for (i = 0; i < backups.length; i++) {
              // highlight backup file if item was selected in previous submit
              if (initOptionSelectedParam.indexOf(backups[i]) > -1) {
                backupsHtml += "<li class='backup selected'>"
              }
              else {
                backupsHtml += "<li class='backup'>"
              }
              backupsHtml += backups[i] + "</li>"
            }
            backupsHtml += "</ul>"
          }

          backupsHtml += "</li>"
        }
        backupsHtml += "</ul>"

        $('backupChooser').update(backupsHtml);

        // use simple tree menu to style list as tree
        ddtreemenu.createTree("backupsTree", false)

        // add "clickability" to the backup file names
        $$('ul.backupList li').each(function(item) {
          item.observe('click', function() {
            // unselect all items
            $$('ul.backupList li').each(function(it) {
              it.removeClassName('selected');
            });
            // select this item
            item.addClassName('selected');

            // store the selection in a hidden field for submit
            var selectedItem = item.innerHTML
            var repo = item.up('li.submenu').firstChild.data
            $('initOptionSelected').value = repo + "/" + selectedItem
          });
        });
      }
    }

  </script>
  <style>
  div.initOptionDetail {
    border: 1px;
    border-color: #CCCCCC;
    border-style: solid;
    background-color: #EEEEEE;
    margin: 3px 0 6px 0;
    padding: 5px;
    overflow: auto;
  }

  .backup.selected {
    background-color: #0066CC;
    color: white;
  }

  </style>
</head>

<content tag="title">
  <g:message code="repository.page.leftnav.title"/>
</content>

<g:render template="leftNav"/>

<body>
<table class="Container">
<tr class="ContainerHeader">
  <td colspan="2"><g:message code="repository.page.leftnav.title"/></td>
</tr>

<g:form action="save" method="post">
  <tr class="prop">
    <td valign="top" class="name">
      <label for="name"><g:message code="repository.page.create.name"/></label>
    </td>
    <td width="100%" valign="top" class="value errors">
      <input type="text" id="name" name="name" value="${fieldValue(bean: repo, field: 'name')}"/>
      <g:hasErrors bean="${repo}" field="name">
        <ul><g:eachError bean="${repo}" field="name">
          <li><g:message error="${it}" encodeAs="HTML"/></li>
        </g:eachError></ul>
      </g:hasErrors>
    </td>
  </tr>
  <tr class="prop">
    <td valign="top" class="name" style="white-space: nowrap;"><label for="useBackup"><g:message
            code="repository.page.create.initOptions"/></label></td>
    <td valign="top" class="value ${hasErrors(bean: repo, field: 'useBackup', 'errors')}">
      <g:radio name="initOption" value="useTemplate" id="useTemplate" class="repoInitOptions" checked="checked"/><label
            for="useTemplate"><g:message code="repository.page.create.useTemplate"/></label>
      <g:radio name="initOption" value="useBackup" id="useBackup" class="repoInitOptions"/><label
            for="useBackup"><g:message code="repository.page.create.useBackup"/></label>
      <g:hiddenField name="initOptionSelected" value="${params.initOptionSelected}"/>
      <div id="backupChooser" class="initOptionDetail useBackup" style="display:none">
        Loading backup files...
      </div>

      <div id="templateChooser" class="initOptionDetail useTemplate" style="display:none">
        <g:each in="${templateList}" status="i" var="template">
            <div><label><g:radio name="templateId" value="${template.id}" 
              checked="${(params.templateId == template.id as String) || (i == 0 && !params.templateId)}"/>${template.name}</label></div>
        </g:each>
      </div>
    </td>
  </tr>
  <tr class="ContainerFooter">
    <td colspan="2">
      <div class="AlignRight">
        <input class="Button save" type="submit" value="<g:message code='repository.page.create.button.create'/>"/>
      </div>
    </td>
  </tr>
  </tbody>
  </table>
</g:form>
</table>
</body>
</html>
